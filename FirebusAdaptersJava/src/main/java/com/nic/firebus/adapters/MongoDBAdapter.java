package com.nic.firebus.adapters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataMap;

public class MongoDBAdapter extends Adapter  implements ServiceProvider, Consumer
{
	private Logger logger = Logger.getLogger("com.nic.firebus.adapters");
	protected MongoClient client;
	protected MongoDatabase database;
	
	public MongoDBAdapter(Firebus n, DataMap c)
	{
		super(n, c);
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
			DataMap packet = new DataMap(payload.getString());
			String objectName = packet.getString("object");
			String operation = packet.getString("operation");
			DataMap data = packet.getObject("data");
			if(database != null)
			{
				MongoCollection<Document> collection = database.getCollection(objectName);
				if(collection != null)
				{
					if(data != null)
					{
						Document incomingDoc = Document.parse(data.toString());
						Document existingDoc = null;
						MongoCursor<Document> it = collection.listIndexes().iterator();
						while(it.hasNext()  &&  existingDoc == null)
						{
							Document indexDoc = it.next();
							Document keyDoc = (Document)indexDoc.get("key");
							boolean missingField = false;
							Document findDoc = new Document();
							Iterator<Entry<String, Object>> it2 = keyDoc.entrySet().iterator();
							while(it2.hasNext())
							{
								Entry<String, Object> indexField = it2.next();
								String indexFieldName = indexField.getKey();
								String indexFieldValue = data.getString(indexFieldName);
								if(indexFieldValue != null)
									findDoc.append(indexFieldName, indexFieldValue);
								else
									missingField = true;
							}
							if(!missingField)
							{
								existingDoc = collection.find(findDoc).first();
							}
							else
							{
								findDoc = null;
							}
						}
						
						if(existingDoc != null)
						{
							if(operation == null || (operation !=null && operation.equals("insert")) || (operation !=null && operation.equals("update")))
								collection.updateOne(existingDoc, new Document("$set", incomingDoc));
							else if(operation != null  && operation.equals("replace"))
								collection.replaceOne(existingDoc, incomingDoc);
							else if(operation != null  && operation.equals("delete"))
								collection.deleteOne(existingDoc);
						}
						else
						{
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
		catch(Exception e)
		{
			logger.severe(e.getMessage());
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
			String objectName = request.getString("object");
			if(database != null)
			{
				MongoCollection<Document> collection = database.getCollection(objectName);
				if(collection != null)
				{
					Iterator<Document> it = null;
					if(request.containsKey("filter"))
					{
						DataMap filter = request.getObject("filter");
						Document filterDoc = Document.parse(filter.toString()); 
						it = collection.find(filterDoc).iterator();		
					}
					else if(request.containsKey("aggregation"))
					{
						DataList aggregation = request.getList("aggregation");
						ArrayList<Document> list = new ArrayList<Document>();
						for(int i = 0; i < aggregation.size(); i++)
							list.add(Document.parse(aggregation.getObject(i).toString()));
						it = collection.aggregate(list).iterator();
					}
					else
					{
						it = collection.find(new Document()).iterator();		
					}
					
					if(it != null)
					{
						DataList list = new DataList();
						while(it.hasNext())
						{
							Document doc = it.next();
							String str = doc.toJson();
							DataMap obj = new DataMap(str);
							list.add(obj);
						}
						responseJSON.put("result", list);
						response.setData(responseJSON.toString());
					}
				}
				else
				{
					throw new FunctionErrorException("No collection exists by this namel");
				}
			}
			else
			{
				throw new FunctionErrorException("Database as not been specificied in the configuration");
			}			
		}
		catch(Exception e)
		{
			throw new FunctionErrorException(e.getMessage());
		}		
		return response;
	}

	public ServiceInformation getServiceInformation()
	{
		return null;
	}
	
}