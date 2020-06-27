package io.firebus.adapters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.MongoClient;
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
	protected int pageSize;
	
	public MongoDBAdapter(DataMap c)
	{
		super(c);
		pageSize = config.containsKey("pagesize") ? config.getNumber("pagesize").intValue() : 50;
		connectMongo();
	}

	protected void connectMongo()
	{
		if(client != null)
		{
			client.close();
			client = null;
		}
		client =  new MongoClient(config.getString("connectionstring"));
		database = client.getDatabase(config.getString("database"));
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
		Payload response = new Payload();
		DataMap responseJSON = new DataMap();
		try
		{
			logger.finer("Starting mongo request");
			DataMap request = new DataMap(payload.getString());
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

		}
		catch(Exception e)
		{
			logger.severe("Error processing data request : " + e.getMessage());
			throw new FunctionErrorException(e.getMessage());
		}		
		return response;
	}
	
	private DataList get(DataMap request) throws FunctionErrorException, DataException
	{
		DataList responseList = null;
		String objectName = request.getString("object");
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		if(database != null)
		{
			MongoCollection<Document> collection = database.getCollection(objectName);
			if(collection != null)
			{
				Iterator<Document> it = null;
				Document filterDoc = null;
				if(request.containsKey("filter"))
					filterDoc = Document.parse(request.getObject("filter").toString()); 
				else
					filterDoc = new Document();
				it = collection.find(filterDoc).iterator();		
				responseList = retieveDocuments(it, page);
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
					if(source != null)
						groupKeys.put(key, source);
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
						group.put(metric.getString("name"), new DataMap("$" + function, "$" + metric.getString("field")));
				}
				DataMap groupContainer = new DataMap("$group", group);
				pipeline.add(Document.parse(groupContainer.toString()));
				
				it = collection.aggregate(pipeline).iterator();
				responseList = retieveDocuments(it, page);
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
						item.put(key, item.getString("_id." + key));
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
	
	private DataList retieveDocuments(Iterator<Document> it, int page) throws DataException
	{
		DataList responseList = null;
		if(it != null)
		{
			responseList = new DataList();
			for(int i = 0; it.hasNext() && i < (page * pageSize); i++)
				it.next();
			while(it.hasNext() && responseList.size() < pageSize)
			{
				Document doc = it.next();
				String str = doc.toJson();
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
					Document existingDoc = collection.find(findDoc).first();
											
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

	public ServiceInformation getServiceInformation()
	{
		return null;
	}
	
}
