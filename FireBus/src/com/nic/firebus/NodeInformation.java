package com.nic.firebus;

import java.util.ArrayList;
import java.util.Random;

public class NodeInformation 
{
	protected int nodeId;
	//protected Address address;
	protected Connection connection;
	protected ArrayList<Address> addresses;
	protected ArrayList<Integer> repeaters;
	protected ArrayList<ServiceInformation> services;
	
	public NodeInformation(int ni)
	{
		nodeId = ni;
		initialise();
	}
	
	public NodeInformation(Address a)
	{
		initialise();
		addAddress(a);
	}
	
	protected void initialise()
	{
		services = new ArrayList<ServiceInformation>();
		repeaters = new ArrayList<Integer>();
		addresses = new ArrayList<Address>();
	}
	
	public void setNodeId(int i)
	{
		nodeId = i;
	}
	
	public void setConnection(Connection c)
	{
		connection = c;
		/*
		if((connection == null  && c != null) || (connection != null && c == null) || (connection != null && c != null && connection != c))
		{
			Connection oldConn = connection;
			connection = c;
			if(oldConn != null)
				oldConn.setNodeInformation(null);
			if(connection != null)
				connection.setNodeInformation(this);
			if(connection != null && connection.getAddress() != null)
				setAddress(connection.getAddress());
		}
		*/
	}
	
	public void addAddress(Address a)
	{
		addresses.add(a);
		/*
		if((address != null && a == null) || (address == null && a != null) || (address != null && a != null && a != address))
		{
			Address oldAddress = address;
			address = a;
			if(oldAddress != null)
				oldAddress.setNodeInformation(null);
			if(address != null)
				address.setNodeInformation(this);
			if(address != null && address.getConnection() != null)
				setConnection(address.getConnection());
		}
		*/
	}
	
	public void addRepeater(int id)
	{
		if(!repeaters.contains(id))
			repeaters.add(id);
	}
	
	public void addServiceInformation(ServiceInformation si)
	{
		services.add(si);
	}
	
	public int getNodeId()
	{
		return nodeId;
	}
	
	public int getAddressCount()
	{
		return addresses.size();
	}
	
	public Address getAddress(int i)
	{
		return addresses.get(i);
	}
		
	public boolean containsAddress(Address a)
	{
		for(int i = 0; i < addresses.size(); i++)
			if(addresses.get(i).equals(a))
				return true;
		return false;
	}
	
	public Connection getConnection()
	{
		return connection;
	}
	
	public int getRandomRepeater()
	{
		Random r = new Random();
		if(repeaters.size() > 0)
			return repeaters.get(r.nextInt(repeaters.size()));
		return 0;
	}
	
	public ServiceInformation getServiceInformation(String sn)
	{
		for(int i = 0; i < services.size(); i++)
			if(services.get(i).getServiceName().equals(sn))
				return services.get(i);
		return null;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Id        : " + nodeId + "\r\n");
		for(int i = 0; i < addresses.size(); i++)
			sb.append("Address   : " + addresses.get(i) + "\r\n");
		for(int i = 0; i < repeaters.size(); i++)
			sb.append("Repeater  : " + repeaters.get(i) + "\r\n");
		for(int i = 0; i < services.size(); i++)
			sb.append("Service   : " + services.get(i) + "\r\n");
		sb.append("Connection: " + connection + "\r\n");
		return sb.toString();
	}

}
