package io.firebus.adapters;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.json.Converter;
import org.bson.json.JsonWriterSettings;
import org.bson.json.StrictJsonWriter;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.firebus.Payload;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataException;
import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

public class MongoDBAdapter extends Adapter  implements ServiceProvider, Consumer
{
	private Logger logger = Logger.getLogger("io.firebus.adapters");
	protected MongoClient client;
	protected MongoDatabase database;
	protected DataMap queryColumns;	
	protected long lastWriteOfQueryColumns;
	protected JsonWriterSettings jsonWritterSettings;
	protected int waitTimeout = 9000; 
	protected String databaseName;
	protected String connectionString;
	
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
		MongoClientOptions mongoClientOptions = MongoClientOptions.builder()
                .connectTimeout(2000)
                .maxWaitTime(waitTimeout)
                .serverSelectionTimeout(waitTimeout)
                .build();
		client =  new MongoClient(connectionString, mongoClientOptions);
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
			logger.severe("Error consuming data : " + e.getMessage());
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
			logger.finer("Starting mongo request : " + request.toString(0, true));
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
			else if(request.containsKey("key"))
			{
				upsert(request);
				responseJSON.put("result", "ok");
			}
			response = new Payload(null, responseJSON.toString().getBytes());
			long duration = System.currentTimeMillis() - start;
			if(duration > 2000) 
				logger.warning("Long running mongo request (" + duration + "ms): " + request.toString(0, true));
			logger.finer("Returning mongo response in " + duration + "ms");
		}
		catch(Exception e)
		{
			logger.severe("Error processing data request: " + e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}		
		return response;
	}
	
	private DataList get(DataMap request) throws FunctionErrorException, DataException
	{
		DataList responseList = null;
		String objectName = request.getString("object");
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
		if(database != null)
		{
			MongoCollection<Document> collection = database.getCollection(objectName);
			if(collection != null)
			{
				Iterator<Document> it = null;
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
				it = collection.find(filterDoc).maxAwaitTime(waitTimeout, TimeUnit.MILLISECONDS).sort(sortDoc).skip(page * pageSize).iterator();		
				responseList = retieveDocuments(it, pageSize);
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
		return responseList;
	}
	
	private DataList aggregate(DataMap request) throws FunctionErrorException, DataException
	{
		DataList responseList = null;
		String objectName = request.getString("object");
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
		if(database != null)
		{
			MongoCollection<Document> collection = database.getCollection(objectName);
			if(collection != null)
			{
				Iterator<Document> it = null;
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
						interval = tuple.getObject(i).getNumber("interval");
						source = new DataMap("{$dateToString:{date:{$toDate:{$subtract:[{$toLong:{$toDate:\"$" + key + "\"}},{$mod:[{$toLong:{$toDate:\"$" + key + "\"}}," + interval + "]}]}}}}");
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
				
				it = collection.aggregate(pipeline).maxTime(waitTimeout, TimeUnit.MILLISECONDS).iterator();
				for(int i = 0; i < (page * pageSize) && it.hasNext(); i++)
					it.next();
				responseList = retieveDocuments(it, pageSize);
				for(int i = 0; i < responseList.size(); i++)
				{
					DataMap item = responseList.getObject(i);
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
			else
			{
				throw new FunctionErrorException("No collection exists by this name");
			}
		}
		else
		{
			throw new FunctionErrorException("Database as not been specificied in the configuration");
		}	
		return responseList;
	}
	
	private DataList retieveDocuments(Iterator<Document> it, int count) throws DataException
	{
		DataList responseList = null;
		if(it != null)
		{
			responseList = new DataList();
			while(it.hasNext() && responseList.size() < count)
			{
				Document doc = it.next();
				String str = doc.toJson(jsonWritterSettings);
				DataMap obj = new DataMap(str);
				responseList.add(obj);
			}
		}
		return responseList;
	}
	
	private void upsert(DataMap packet)
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
								collection.updateOne(existingDoc, new Document("$set", incomingDoc));
							else if(operation != null  && operation.equals("replace"))
								collection.replaceOne(existingDoc, incomingDoc);
							else if(operation != null  && operation.equals("delete"))
								collection.deleteOne(existingDoc);
						}
						else
						{
							if(operation != null  && operation.equals("delete"))
								collection.deleteOne(existingDoc);
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
							collection.insertOne(incomingDoc);
					}
				}
			}
			else
			{
				logger.severe("No collection exists by this namel");
			}
		}
		else
		{
			logger.severe("Database as not been specificied in the configuration");
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
					logger.severe(e.getMessage());
				}
				lastWriteOfQueryColumns = now;
			}
		}
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}
	
}
