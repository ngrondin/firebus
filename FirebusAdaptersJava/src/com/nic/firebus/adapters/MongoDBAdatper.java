package com.nic.firebus.adapters;

import java.util.Iterator;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDBAdatper
{

	public static void main(String[] args)
	{
		MongoClient mongoClient = new MongoClient();
		MongoDatabase database = mongoClient.getDatabase("local");
		for (String name : database.listCollectionNames()) 
		{
		    System.out.println(name);
		}
		MongoCollection<Document> collection = database.getCollection("restapourants");
		Iterator<Document> it = collection.find(new Document()).iterator();
		while(it.hasNext())
		{
			Document doc = it.next();
			System.out.println(doc);
		}
		mongoClient.close();
	}
}
