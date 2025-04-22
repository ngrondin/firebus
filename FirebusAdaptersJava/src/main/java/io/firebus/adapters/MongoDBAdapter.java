package io.firebus.adapters;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;

import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.data.DataEntity;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;
import io.firebus.data.ZonedTime;
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
			write(request);
		}
		catch(Exception e)
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
			DataMap request = payload.getDataMap();
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
			else if(request.containsKey("count")) 
			{
				long count = count(request);
				responseJSON.put("result", count);
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
							write(session, request);
						} else if(request.containsKey("multi")) {
							DataList multi = request.getList("multi");
							session.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.W1).build());
							for(int i = 0; i < multi.size(); i++) 
								write(session, multi.getObject(i));
							session.commitTransaction();						
						}
						success = true;
						responseJSON.put("result", "ok");	
					} catch(Exception e) {
						if(session != null) {
							try{session.abortTransaction();} catch(Exception e2) {}
						}
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
			DataMap request = payload.getDataMap();
			Logger.finer("fb.adapter.monfo.request", request);
			int chunkSize = request.containsKey("chunksize") ? request.getNumber("chunksize").intValue() : 50;
			//int advance = request.containsKey("advance") ? request.getNumber("advance").intValue() : 0;
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
				public void receiveStreamData(Payload payload) {
					if(payload.getString().equals("next")) {
						try {
							sendToStream(streamEndpoint, it, chunkSize, pp);
						} catch(Exception e) {
							streamEndpoint.close();
						}
					} else { 
						streamEndpoint.close();
						Logger.warning("fb.adapter.mongo.stream.close", "Bad flow control");
					}
				}

				public void streamClosed() {
					it.close();
					long duration = System.currentTimeMillis() - start;
					Logger.fine("fb.adapter.mongo.stream.close", new DataMap("ms", duration));
				}

				public void streamError(FunctionErrorException error) { }
			});
			sendToStream(streamEndpoint, it, chunkSize, pp);
			//if(it.hasNext()) //Ignoring the advance functionality for now as it creates correlation not found warnings
			//	for(int i = 0; i < advance; i++)
			//		sendToStream(streamEndpoint, it, chunkSize, pp);
			return null;			
		} catch(Exception e) {
			streamEndpoint.close();
			Logger.severe("fb.adapter.mongo.stream", "Error accepting stream", e);
			throw new FunctionErrorException("Error in db stream", e);	
		}
	}
	
	private void sendToStream(StreamEndpoint streamEndpoint, MongoCursor<Document> it, int chunkSize, DataProcessor postProcessor) 
	{
		if(!streamEndpoint.isClosed()) {
			DataList list = retieveDocuments(it, chunkSize, postProcessor);
			if(list.size() > 0) 
				streamEndpoint.send(new Payload(new DataMap("result", list)));				
			else 
				streamEndpoint.close();
		}
	}

	private DataList get(DataMap request) throws FunctionErrorException, DataException
	{
		DataList responseList = null;
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
		MongoCursor<Document> it = getFindIterable(request).skip(page * pageSize).limit(pageSize).iterator();
		responseList = retieveDocuments(it, pageSize, null);
		it.close();
		return responseList;
	}

	private DataList aggregate(DataMap request) throws FunctionErrorException, DataException
	{
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
		MongoCursor<Document> it = getAggregateIterable(request).iterator();
		for(int i = 0; i < (page * pageSize) && it.hasNext(); i++) it.next(); //Note: this is because the aggregatorIterable doesn't have the skip and limit functions
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
					//recordQuery(objectName, filter);
					filterDoc = (Document)convertToDocument(filter);					
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
					pipeline.add((Document)convertToDocument(match));
					//pipeline.add(Document.parse(match.toString()));
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
				pipeline.add((Document)convertToDocument(groupContainer));
				//pipeline.add(Document.parse(groupContainer.toString()));
				
				DataMap sortContainer = new DataMap("$sort", new DataMap("_id", 1));
				pipeline.add((Document)convertToDocument(sortContainer));
				//pipeline.add(Document.parse(sortContainer.toString()));
				
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
			while(it.hasNext() && count > -1 && responseList.size() < count) {
				DataEntity entity = convertToDataEntity(it.next());
				if(entity instanceof DataMap) {
					DataMap dataMap = (DataMap)entity;
					if(postProcessor != null)
						postProcessor.run(dataMap);
					responseList.add(dataMap);
				}
			}
		}
		return responseList;
	}
	
	private DataEntity convertToDataEntity(Object value) {
		if(value instanceof List) {
			DataList list = new DataList();
			for(Object o: (List<?>)value) 
				list.add(convertToDataEntity(o));
			return list;
		} else if(value instanceof Document) {
			DataMap map = new DataMap();
			for(String key: ((Document)value).keySet())
				map.put(key, convertToDataEntity(((Document)value).get(key)));
			return map;			
		} else if(value instanceof String){
			return new DataLiteral(StringDecoder.decodeQuotedString((String)value));
		} else {
			return new DataLiteral(value);
		}
	}
	
	private Object convertToDocument(DataEntity entity) {
		if(entity instanceof DataLiteral) {
			Object o = ((DataLiteral)entity).getObject();
			if(o instanceof Date)
				o = ((Date)o).toInstant().toString();
			else if(o instanceof ZonedTime)
				o = ((ZonedTime)o).toString();
			return o;
		} else if(entity instanceof DataList) {
			DataList inList = (DataList)entity;
			List<Object> outList = new ArrayList<Object>();
			for(int i = 0; i < inList.size(); i++) 
				outList.add(convertToDocument(inList.get(i)));
			return outList;
		} else if(entity instanceof DataMap) {
			DataMap map = (DataMap)entity;
			Document doc = new Document();
			for(String key: map.keySet()) 
				doc.append(key, convertToDocument(map.get(key)));
			return doc;
		} else {
			return null;
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
	
	
	private void write(DataMap packet) throws FunctionErrorException
	{
		write(null, packet);
	}

	private void write(ClientSession session, DataMap packet) throws FunctionErrorException
	{
		String objectName = packet.getString("object");
		String operation = packet.getString("operation");
		DataMap data = packet.getObject("data");
		DataMap key = packet.getObject("key");
		if(operation == null) operation = "upsert";

		if(database != null) {
			MongoCollection<Document> collection = database.getCollection(objectName);
			if(collection != null) {
				if(key != null) {
					Document keyDoc = (Document)convertToDocument(key);
					Document dataDoc = (Document)convertToDocument(data);
					if(operation.equals("upsert")) {
						UpdateOptions options = new UpdateOptions().upsert(true);
						collection.updateOne(session, keyDoc, new Document("$set", dataDoc), options);
					} else if(operation.equals("update")) {
						UpdateOptions options = new UpdateOptions().upsert(false);
						collection.updateOne(session, keyDoc, new Document("$set", dataDoc), options);
					} else if(operation.equals("replace")) {
						ReplaceOptions options = new ReplaceOptions().upsert(true);
						collection.replaceOne(session, keyDoc, dataDoc, options);
					} else if(operation.equals("insert")) {
						collection.insertOne(session, (Document)convertToDocument(key.merge(data)));
					} else if(operation.equals("delete")) {
						collection.deleteOne(session, keyDoc);
					}
				}
			} else {
				throw new FunctionErrorException("No collection exists by this name");
			}
		} else {
			throw new FunctionErrorException("Database as not been specificied in the configuration");
		}
	}
	
	private long count(DataMap request) throws FunctionErrorException, DataException
	{
		String objectName = request.getString("object");
		if(database != null)
		{
			MongoCollection<Document> collection = database.getCollection(objectName);
			if(collection != null)
			{
				Document filterDoc = null;
				if(request.containsKey("count")) {
					DataMap filter = request.getObject("count");
					filterDoc = (Document)convertToDocument(filter);					
				} else {
					filterDoc = new Document();
				}
				long ret = collection.countDocuments(filterDoc);
				return ret;
			}
			else
			{
				throw new FunctionErrorException("No collection exists by this name");
			}
		}
		else
		{
			throw new FunctionErrorException("Database as not been specified in the configuration");
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
