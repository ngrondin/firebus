package com.nic.firebus.adapters;

import java.util.Iterator;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONList;
import com.nic.firebus.utils.JSONObject;

public class MongoDBAdapter extends Adapter  implements ServiceProvider, Consumer
{
	private Logger logger = Logger.getLogger("com.nic.firebus.adapters");
	protected MongoClient client;
	protected MongoDatabase database;
	
	public MongoDBAdapter(Firebus n, JSONObject c)
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
			JSONObject packet = new JSONObject(payload.getString());
			String objectName = packet.getString("object");
			if(database != null)
			{
				MongoCollection<Document> collection = database.getCollection(objectName);
				if(collection != null)
				{
					JSONObject data = packet.getObject("data");
					if(data != null)
					{
						Document doc = Document.parse(data.toString());
						collection.insertOne(doc);
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
		JSONObject responseJSON = new JSONObject();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			String objectName = request.getString("object");
			if(database != null)
			{
				MongoCollection<Document> collection = database.getCollection(objectName);
				if(collection != null)
				{
					JSONObject filter = request.getObject("filter");
					if(filter != null)
					{
						Iterator<Document> it = collection.find(Document.parse(filter.toString())).iterator();		
						JSONList list = new JSONList();
						while(it.hasNext())
						{
							Document doc = it.next();
							list.add(new JSONObject(doc.toJson()));
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
