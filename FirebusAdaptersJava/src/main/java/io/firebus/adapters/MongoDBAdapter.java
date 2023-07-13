package io.firebus.adapters;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.json.Converter;
import org.bson.json.JsonWriterSettings;
import org.bson.json.StrictJsonWriter;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCommandException;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataEntity;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.data.parse.StringDecoder;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.information.StreamInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.interfaces.StreamHandler;
import io.firebus.interfaces.StreamProvider;
import io.firebus.logging.Logger;

public class MongoDBAdapter extends Adapter  implements ServiceProvider, StreamProvider, Consumer
{
	protected MongoClient client;
	protected MongoDatabase database;
	protected DataMap queryColumns;	
	protected long lastWriteOfQueryColumns;
	protected JsonWriterSettings jsonWritterSettings;
	protected int waitTimeout = 9000; 
	protected String databaseName;
	protected String connectionString;
	
	public interface DataProcessor {
		public void run(DataMap item);
	}

	
	public MongoDBAdapter(DataMap c)
	{
		super(c);
		jsonWritterSettings = JsonWriterSettings.builder()
		         .int64Converter(new Converter<Long>() {
					public void convert(Long value, StrictJsonWriter writer) {
						writer.writeNumber(value.toString());
					}})
		         .build();
		connectionString = config.getString("connectionstring");
		databaseName = config.getString("database");
		if(config.getBoolean("writecolumns")) {
			queryColumns = new DataMap();
			lastWriteOfQueryColumns = 0;
		}
		if(config.containsKey("waittimeout")) {
			waitTimeout = config.getNumber("waittimeout").intValue();
		}
		connectMongo();
	}

	protected void connectMongo()
	{
		if(client != null)
		{
			client.close();
			client = null;
		}
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(connectionString))
				.applyToClusterSettings(builder -> builder.serverSelectionTimeout(waitTimeout, TimeUnit.MILLISECONDS))
				.applyToSocketSettings(builder -> builder.connectTimeout(2000, TimeUnit.MILLISECONDS))
				.build();
		client = MongoClients.create(settings);
		database = client.getDatabase(databaseName);
	}
	 
	public void consume(Payload payload)
	{
		try
		{
			DataMap request = new DataMap(payload.getString());
			upsert(request);
		}
		catch(DataException e)
		{
			Logger.severe("fb.adapter.mongo.consume", "Error consuming data", e);
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		long start = System.currentTimeMillis();
		Payload response = new Payload();
		DataMap responseJSON = new DataMap();
		try
		{
			DataMap request = new DataMap(payload.getString());
			Logger.finer("fb.adapter.monfo.request", request);
			if(request.containsKey("tuple")) 
			{
				DataList list = aggregate(request);
				responseJSON.put("result", list);
			}
			else if(request.containsKey("filter")) 
			{
				DataList list = get(request);
				responseJSON.put("result", list);
			}
			else if(request.containsKey("key") || request.containsKey("multi"))
			{
				ClientSession session = null;
				int tries = 0;
				boolean success = false;
				while(!success) {
					try {
						session = client.startSession();
						if(request.containsKey("key")) {
							upsert(session, request);
						} else if(request.containsKey("multi")) {
							DataList multi = request.getList("multi");
							session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.W1).build());
							for(int i = 0; i < multi.size(); i++) 
								upsert(session, multi.getObject(i));
							session.commitTransaction();						
						}
						success = true;
						responseJSON.put("result", "ok");	
					} catch(Exception e) {
						if(session != null) session.abortTransaction();
						if(e instanceof MongoCommandException && ((MongoCommandException) e).getCode() == 112 && tries < 3) {
							Logger.warning("fb.adapter.mongo.multitx", e);
							tries++;
						} else {
							throw new FunctionErrorException("Error in db multi transaction", e);
						}
					} finally {
						if(session != null) session.close();
					}
				}
			}
			response = new Payload(responseJSON);
			long duration = System.currentTimeMillis() - start;
			if(duration > 2000) 
				Logger.warning("fb.adapter.mongo.longtx", new DataMap("ms", duration, "req", request));
			Logger.fine("fb.adapter.mongo.resp", new DataMap("ms", duration));
		}
		catch(Exception e)
		{
			Logger.severe("fb.adapter.mongo.request", "Error processing data", e);
			throw new FunctionErrorException("Error in db service", e);
		}		
		return response;
	}
	
	public Payload acceptStream(Payload payload, StreamEndpoint streamEndpoint) throws FunctionErrorException {
		try {
			DataMap request = new DataMap(payload.getString());
			Logger.finer("fb.adapter.monfo.request", request);
			int chunkSize = request.containsKey("chunksize") ? request.getNumber("chunksize").intValue() : 20;
			int advance = request.containsKey("advance") ? request.getNumber("advance").intValue() : 0;
			if(chunkSize <= 0) throw new FunctionErrorException("Stream chunk size cannot be 0");
			long start = System.currentTimeMillis();
			MongoCursor<Document> iterator = null;
			DataProcessor postProcessor = null;
			if(request.containsKey("filter")) {
				iterator = getFindIterable(request).iterator();
			} else if(request.containsKey("tuple")) {
				iterator = getAggregateIterable(request).iterator();
				postProcessor = new AggregatePostProcessor(request.getList("tuple"));
			} else {
				throw new FunctionErrorException("Invalid request");
			}
			final MongoCursor<Document> it = iterator;
			final DataProcessor pp = postProcessor;
			streamEndpoint.setHandler(new StreamHandler() {
				public void receiveStreamData(Payload payload, StreamEndpoint streamEndpoint) {
					if(payload.getString().equals("next")) {
						try {
							if(it.hasNext())
								sendToStream(streamEndpoint, it, chunkSize, pp);
							else if(streamEndpoint.isActive())
								streamEndpoint.close();
							
						} catch(Exception e) {
							streamEndpoint.close();
						}
					} else { 
						streamEndpoint.close();
						Logger.warning("fb.adapter.mongo.stream.close", "Bad flow control");
					}
				}

				public void streamClosed(StreamEndpoint streamEndpoint) {
					it.close();
					long duration = System.currentTimeMillis() - start;
					Logger.fine("fb.adapter.mongo.stream.close", new DataMap("ms", duration));
				}
			});
			sendToStream(streamEndpoint, it, chunkSize, pp);
			if(it.hasNext())
				for(int i = 0; i < advance; i++)
					sendToStream(streamEndpoint, it, chunkSize, pp);
			return null;			
		} catch(Exception e) {
			Logger.severe("fb.adapter.mongo.stream", "Error accepting stream", e);
			throw new FunctionErrorException("Error in db stream", e);	
		}
	}
	
	private void sendToStream(StreamEndpoint streamEndpoint, MongoCursor<Document> it, int chunkSize, DataProcessor postProcessor) 
	{
		DataList list = retieveDocuments(it, chunkSize, postProcessor);
		streamEndpoint.send(new Payload(new DataMap("result", list)));
	}

	private DataList get(DataMap request) throws FunctionErrorException, DataException
	{
		DataList responseList = null;
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
		MongoCursor<Document> it = getFindIterable(request).skip(page * pageSize).iterator();
		responseList = retieveDocuments(it, pageSize, null);
		it.close();
		return responseList;
	}

	private DataList aggregate(DataMap request) throws FunctionErrorException, DataException
	{
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
		MongoCursor<Document> it = getAggregateIterable(request).iterator();
		for(int i = 0; i < (page * pageSize) && it.hasNext(); i++) it.next();
		DataList responseList = retieveDocuments(it, pageSize, new AggregatePostProcessor(request.getList("tuple")));
		it.close();
		return responseList;
	}
	
	private FindIterable<Document> getFindIterable(DataMap request) throws FunctionErrorException, DataException
	{
		String objectName = request.getString("object");
		if(database != null)
		{
			MongoCollection<Document> collection = database.getCollection(objectName);
			if(collection != null)
			{
				Document filterDoc = null;
				if(request.containsKey("filter")) {
					DataMap filter = request.getObject("filter");
					recordQuery(objectName, filter);
					filterDoc = Document.parse(filter.toString());					
				} else {
					filterDoc = new Document();
				}
				Document sortDoc = new Document();
				if(request.containsKey("sort")) {
					DataMap sortItem = null;
					int i = 0;
					while((sortItem = request.getObject("sort").getObject("" + i++)) != null) {
						sortDoc.append(sortItem.getString("attribute"), sortItem.getNumber("dir").intValue());
					}
				} 
				FindIterable<Document> it = collection.find(filterDoc).maxAwaitTime(waitTimeout, TimeUnit.MILLISECONDS).sort(sortDoc);	
				return it;
			}
			else
			{
				throw new FunctionErrorException("No collection exists by this name");
			}
		}
		else
		{
			throw new FunctionErrorException("Database as not been specificied in the configuration");
		}	
	}
	
	private AggregateIterable<Document> getAggregateIterable(DataMap request) throws FunctionErrorException, DataException
	{
		String objectName = request.getString("object");
		if(database != null)
		{
			MongoCollection<Document> collection = database.getCollection(objectName);
			if(collection != null)
			{
				ArrayList<Document> pipeline = new ArrayList<Document>();

				if(request.containsKey("filter"))
				{
					DataMap match = new DataMap("$match", request.getObject("filter"));
					pipeline.add(Document.parse(match.toString()));
				}
					
				DataMap group = new DataMap();
				DataMap groupKeys = new DataMap();
				DataList tuple = request.getList("tuple");
				for(int i = 0; i < tuple.size(); i++) 
				{
					String key = null;
					Number interval = null;
					Object source = null;
					if(tuple.get(i) instanceof DataMap) {
						key = tuple.getObject(i).getString("attribute");
						interval = tuple.getObject(i).getNumber("interval").longValue();
						source = new DataMap("{$dateToString:{date:{$toDate:{$subtract:[{$toLong:{$toDate:\"$" + key + "\"}},{$mod:[{$toLong:{$toDate:\"$" + key + "\"}}," + interval.toString() + "]}]}}}}");
					} else {
						key = tuple.getString(i);
						source = "$" + key;
					}
					if(source != null) {
						groupKeys.put(key, source);
					}
				}
				group.put("_id", groupKeys);
				DataList metrics = request.getList("metrics");
				for(int i = 0; i < metrics.size(); i++) 
				{
					DataMap metric = metrics.getObject(i);
					String function = metric.getString("function");
					if(function.equals("count")) 
						group.put(metric.getString("name"), new DataMap("$sum", 1));
					else
						group.put(metric.getString("name"), new DataMap("$" + function, "$" + metric.getString("attribute")));
				}
				DataMap groupContainer = new DataMap("$group", group);
				pipeline.add(Document.parse(groupContainer.toString()));
				
				DataMap sortContainer = new DataMap("$sort", new DataMap("_id", 1));
				pipeline.add(Document.parse(sortContainer.toString()));
				
				AggregateIterable<Document> it = collection.aggregate(pipeline).maxTime(waitTimeout, TimeUnit.MILLISECONDS);
				return it;
			}
			else
			{
				throw new FunctionErrorException("No collection exists by this name");
			}
		}
		else
		{
			throw new FunctionErrorException("Database as not been specificied in the configuration");
		}	
	}
	
	private DataList retieveDocuments(Iterator<Document> it, int count, DataProcessor postProcessor) 
	{
		DataList responseList = new DataList();
		if(it != null) {
			while(it.hasNext() && responseList.size() < count) {
				DataMap item = (DataMap)convertValue(it.next());
				if(postProcessor != null)
					postProcessor.run(item);
				responseList.add(item);
			}
		}
		return responseList;
	}
	
	private DataEntity convertValue(Object value) {
		if(value instanceof List) {
			DataList list = new DataList();
			for(Object o: (List<?>)value) 
				list.add(convertValue(o));
			return list;
		} else if(value instanceof Document) {
			DataMap map = new DataMap();
			for(String key: ((Document)value).keySet())
				map.put(key, convertValue(((Document)value).get(key)));
			return map;			
		} else if(value instanceof String){
			return new DataLiteral(StringDecoder.decodeQuotedString((String)value));
		} else {
			return new DataLiteral(value);
		}
	}
	
	
	
	public class AggregatePostProcessor implements DataProcessor {
		public DataList tuple;
		
		public AggregatePostProcessor(DataList t) {
			tuple = t;
		}
		
		public void run(DataMap item) {
			for(int j = 0; j < tuple.size(); j++)
			{
				String key = null;
				if(tuple.get(j) instanceof DataMap) {
					key = tuple.getObject(j).getString("attribute");
				} else {
					key = tuple.getString(j);
				}
				item.put(key, item.get("_id." + key));
			}
			item.remove("_id");
		}
	}
	
	
	private void upsert(DataMap packet)
	{
		upsert(null, packet);
	}
	
	private void upsert(ClientSession session, DataMap packet)
	{
		String objectName = packet.getString("object");
		String operation = packet.getString("operation");
		DataMap data = packet.getObject("data");
		DataMap key = packet.getObject("key");
		if(database != null)
		{
			MongoCollection<Document> collection = database.getCollection(objectName);
			if(collection != null)
			{
				if(key != null)
				{
					Document findDoc = Document.parse(key.toString());
					Document existingDoc = collection.find(findDoc).maxTime(waitTimeout, TimeUnit.MILLISECONDS).first();
											
					if(existingDoc != null)
					{
						if(data != null)
						{
							Document incomingDoc = Document.parse(data.toString());
							if(operation == null || (operation != null && operation.equals("insert")) || (operation !=null && operation.equals("update")))
								collection.updateOne(session, existingDoc, new Document("$set", incomingDoc));
							else if(operation != null  && operation.equals("replace"))
								collection.replaceOne(session, existingDoc, incomingDoc);
							else if(operation != null  && operation.equals("delete"))
								collection.deleteOne(session, existingDoc);
						}
						else
						{
							if(operation != null  && operation.equals("delete"))
								collection.deleteOne(session, existingDoc);
						}
					}
					else
					{
						if(data != null)
							data.merge(key);
						else
							data = key;
						if(!data.containsKey("_id")) {
							data.put("_id", UUID.randomUUID().toString());
						}
						Document incomingDoc = Document.parse(data.toString());
						if(operation == null || (operation !=null && operation.equals("insert")) || (operation !=null && operation.equals("update")) || (operation !=null && operation.equals("replace")))
							collection.insertOne(session, incomingDoc);
					}
				}
			}
			else
			{
				Logger.severe("fb.adapter.mongo.nocollection", new DataMap("collection", objectName));
			}
		}
		else
		{
			Logger.severe("fb.adapter.mongo.nodb", "Database as not been specificied in the configuration");
		}
	}

	protected void recordQuery(String object, DataMap filter) {
		if(queryColumns != null) {
			DataMap cols = queryColumns.getObject(object);
			if(cols == null) {
				cols = new DataMap();
				queryColumns.put(object, cols);
			}
			Iterator<String> ki = filter.keySet().iterator();
			while(ki.hasNext()) {
				String col = ki.next();
				if(col.equals("$or")) {
					DataList list = filter.getList("$or");
					for(int i = 0; i < list.size(); i++) 
						recordQuery(object, list.getObject(i));
				} else {
					if(!cols.containsKey(col)) {
						cols.put(col, 1);
					} else {
						int cnt = cols.getNumber(col).intValue() + 1;
						cols.put(col, cnt);
					}
				}
			}	
			long now = System.currentTimeMillis();
			if(now > lastWriteOfQueryColumns + 60000) {
				try {
					FileOutputStream fos = new FileOutputStream("mongo_columns.json");
					fos.write(queryColumns.toString().getBytes());
					fos.close();
				} catch(Exception e) {
					Logger.severe("fb.adapter.mongo.recordquery", e);
				}
				lastWriteOfQueryColumns = now;
			}
		}
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}

	public int getStreamIdleTimeout() {
		return 10000;
	}

	public StreamInformation getStreamInformation() {
		return null;
	}
	
}
