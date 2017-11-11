package com.nic.firebus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Logger;

public class DiscoveryManager extends Thread
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected boolean quit;
	protected int nodeId;
	protected String networkName;
	protected int connectionsPort;
	//protected Address address;
	protected long lastRequestSent;
	protected NodeCore nodeCore;
	protected MulticastSocket socket;
	//protected InetAddress multicastGroup;
	protected InetAddress discoveryAddress;
	protected int discoveryPort;

	
	public DiscoveryManager(NodeCore nc, int id, String n, int p)
	{
		nodeCore = nc;
		nodeId = id;
		networkName = n;
		connectionsPort = p;
		quit = false;
		try
		{
			discoveryAddress = InetAddress.getByName("239.255.255.250");
			discoveryPort = 1900;
			socket = new MulticastSocket(discoveryPort);
			
			Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
	        while (ifs.hasMoreElements()) 
	        {
	            NetworkInterface xface = ifs.nextElement();
	            if(!xface.isLoopback()  &&  xface.isUp())
	            {
	            	try
	            	{
	            		socket.joinGroup(new InetSocketAddress(discoveryAddress, discoveryPort), xface);
	            	}
	            	catch(Exception e)
	            	{
	            		logger.info(xface.getName() + " could not join discovery address group");
	            	}
                }
	        }	
	        
			//socket.joinGroup(discoveryAddress);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		setName("Firebus Discovery Manager");
		start();
	}
	
	public void close()
	{
		try
		{
			quit = true;
			socket.close();
		}
		catch(Exception e)
		{
			logger.severe(e.getMessage());
		}
	}
	
	public void run()
	{
		sendDiscoveryRequest();
		while(!quit)
		{
			try 
			{
				byte[] buf = new byte[256];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
			    socket.receive(packet);
			    String received = new String(packet.getData()).trim();
			    
			    if(received.startsWith("Firebus discovery request from"))
			    {
			    	String[] parts = received.split(" ");
			    	int id = Integer.parseInt(parts[4]);
			    	String net = parts[5];
			    	if(id != nodeId  &&  net.equals(networkName))
			    	{
				    	logger.fine("Received a discovery request from " + id + " on network \"" + net + "\"");
			    		sendAdvertisement(packet.getAddress());
			    	}
			    }
			    if(received.startsWith("Firebus anouncement from"))
			    {
			    	String[] parts = received.split(" ");
			    	int id = Integer.parseInt(parts[3]);
			    	String net = parts[4];
			    	String ad = parts[5];
			    	int port = Integer.parseInt(parts[6]);
			    	Address address = new Address(ad, port);
			    	if(id != nodeId  &&  net.equals(networkName))
			    	{
				    	logger.fine("Received a anouncement from " + id + " on network \"" + net + "\" with address " + address);
			    		nodeCore.nodeDiscovered(id, address);
			    	}
			    }
			}
			catch (IOException e) 
			{
				logger.severe(e.getMessage());
			}
		}
		socket.close();
	}
	
	public void sendDiscoveryRequest()
	{
		long currentTime = System.currentTimeMillis();
		if(currentTime > (lastRequestSent + 10000))
		{
			try 
			{
				String val = "Firebus discovery request from " + nodeId + " " + networkName;
				Enumeration<NetworkInterface> ifs =  NetworkInterface.getNetworkInterfaces();
		        while (ifs.hasMoreElements()) 
		        {
		            NetworkInterface xface = ifs.nextElement();
		            if(!xface.isLoopback()  &&  xface.isUp())
						multicastSend(val, xface);
		        }
		    	logger.fine("Sent a discovery request");
	        }
	        catch (IOException e) 
			{
	            e.printStackTrace();
	        }
			
			lastRequestSent = currentTime;
		}
	}
	
	public void sendAdvertisement(InetAddress addr)
	{
		try 
		{
			InetAddress localAddress = getLocalAddressForRemoteConnection(addr);
			NetworkInterface iface = NetworkInterface.getByInetAddress(localAddress);
			String val = "Firebus anouncement from " + nodeId + " " + networkName + " " + localAddress.getHostAddress()+ " " + connectionsPort;
			multicastSend(val, iface);
	    	logger.fine("Sent a discovery anouncement for node " + nodeId + " on network \"" + networkName + "\" at address " + localAddress.getHostAddress()+ " " + connectionsPort + " via NIC " + iface.getName());
		}
        catch (IOException e) 
		{
            e.printStackTrace();
        }
		
	}	
	
	protected void multicastSend(String value, NetworkInterface xface) throws IOException
	{
		byte[] buf = value.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, discoveryAddress, discoveryPort);
    	socket.setNetworkInterface(xface);
    	socket.send(packet);
		
		/*
		Enumeration<NetworkInterface> ifs =  NetworkInterface.getNetworkInterfaces();
        while (ifs.hasMoreElements()) 
        {
            NetworkInterface xface = ifs.nextElement();
            if(!xface.isLoopback()  &&  xface.isUp())
            {
            	socket.setNetworkInterface(xface);
            	socket.send(packet);
            }
        }
        */
	}
	
    private InetAddress getLocalAddressForRemoteConnection(InetAddress remoteAddress) 
    {      
    	try
    	{
	    	DatagramSocket sock = new DatagramSocket();
	        sock.connect(remoteAddress, discoveryPort);
	        InetAddress localAddress = sock.getLocalAddress();
	        sock.disconnect();
	        sock.close();
	        sock = null;
	        return localAddress;
    	}
    	catch(SocketException e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    }
}
