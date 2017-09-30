package com.nic.firebus.adapters;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

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
import com.nic.firebus.utils.JSONObject;

public class MongoDBAdatper extends Adapter  implements ServiceProvider, Consumer
{
	protected MongoClient client;
	protected MongoDatabase database;
	
	public MongoDBAdatper(Firebus n, JSONObject c)
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
		// TODO Auto-generated method stub
		
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		Payload response = new Payload();
		try
		{
			JSONObject request = new JSONObject(payload.getString());
			if(database != null)
			{
				MongoCollection<Document> collection = database.getCollection("po");
				Iterator<Document> it = collection.find(Document.parse("{vendor:\"NicCo\"}")).iterator();
				while(it.hasNext())
				{
					Document doc = it.next();
					System.out.println(doc.toJson());
				}
			}
			else
			{
				throw new FunctionErrorException("database object is null");
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
		// TODO Auto-generated method stub
		return null;
	}
}
