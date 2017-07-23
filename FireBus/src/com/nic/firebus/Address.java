package com.nic.firebus;


public class Address
{
	protected String address;
	protected int port;
	//protected Connection connection;
	//protected NodeInformation nodeInformation;
	
	public Address(String a, int p)
	{
		address = a;
		port = p;
	}
	
	/*
	public void setConnection(Connection c)
	{
		if((connection == null  && c != null) || (connection != null && c == null) || (connection != null && c != null && connection != c))
		{
			Connection oldConn = connection;
			connection = c;
			if(oldConn != null)
				oldConn.setAddress(null);
			if(connection != null)
				connection.setAddress(this);
		}
	}
	
	public void setNodeInformation(NodeInformation ni)
	{
		if((nodeInformation != null && ni == null) || (nodeInformation == null && ni != null) || (nodeInformation != null && ni != null && ni != nodeInformation))
		{
			NodeInformation oldNI = nodeInformation;
			nodeInformation = ni;
			if(oldNI != null)
				oldNI.setAddress(null);
			if(nodeInformation != null)
				nodeInformation.setAddress(this);
		}
	}
	
	
	public Connection getConnection()
	{
		return connection;
	}
	
	public NodeInformation getNodeInformation()
	{
		return nodeInformation;
	}
	*/
	public String getAddress()
	{
		return address;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public boolean equals(Address a)
	{
		if(a.getAddress().equals(address)  &&  a.getPort() == port)
			return true;
		else
			return false;
	}
	
	public String toString()
	{
		return address + ":" + port;
	}

}
