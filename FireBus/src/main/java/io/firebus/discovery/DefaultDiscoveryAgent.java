package io.firebus.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Logger;

import io.firebus.Address;
import io.firebus.DiscoveryAgent;
import io.firebus.NodeCore;

public class DefaultDiscoveryAgent extends DiscoveryAgent
{
	private Logger logger;
	protected boolean quit;
	protected int nodeId;
	protected String networkName;
	protected int connectionsPort;
	protected long lastRequestSent;
	protected MulticastSocket socket;
	protected InetAddress discoveryAddress;
	protected int discoveryPort;

	public DefaultDiscoveryAgent(NodeCore nc)
	{
		super(nc);
	}
	
	public void init()
	{
		logger = Logger.getLogger("io.firebus");
		nodeId = nodeCore.getNodeId();
		networkName = nodeCore.getNetworkName();
		connectionsPort = nodeCore.getConnectionManager().getPort();
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
	            		logger.fine(xface.getName() + " joined the discovery address group");
	            	}
	            	catch(Exception e)
	            	{
	            		logger.severe(xface.getName() + " could not join discovery address group");
	            	}
                }
	        }	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		setName("fbDefaultDiscoveryAgent");
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
				    	logger.fine("Received an anouncement from " + id + " on network \"" + net + "\" with address " + address);
						nodeCore.getDirectory().processDiscoveredNode(id,  address);
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
	
	protected void sendDiscoveryRequest()
	{
		long currentTime = System.currentTimeMillis();
		if(currentTime > (lastRequestSent + 10000))
		{
			try 
			{
				String message = "Firebus discovery request from " + nodeId + " " + networkName;
				Enumeration<NetworkInterface> ifs =  NetworkInterface.getNetworkInterfaces();
		        while (ifs.hasMoreElements()) 
		        {
		            NetworkInterface xface = ifs.nextElement();
		            if(!xface.isLoopback()  &&  xface.isUp())
		            {
						multicastSend(message, xface);
				    	logger.fine("Sent a discovery request on " + xface.getName());
		            }
		        }
	        }
	        catch (IOException e) 
			{
	            e.printStackTrace();
	        }
			
			lastRequestSent = currentTime;
		}
	}
	
	protected void sendAdvertisement(InetAddress remoteAddress)
	{
		try 
		{
	    	DatagramSocket sock = new DatagramSocket();
	        sock.connect(remoteAddress, discoveryPort);
	        InetAddress localAddress = sock.getLocalAddress();
	        sock.disconnect();
	        sock.close();
	        
			NetworkInterface iface = NetworkInterface.getByInetAddress(localAddress);
			String message = "Firebus anouncement from " + nodeId + " " + networkName + " " + localAddress.getHostAddress()+ " " + connectionsPort;
			multicastSend(message, iface);

	    	logger.fine("Sent a discovery anouncement for node " + nodeId + " on network \"" + networkName + "\" at address " + localAddress.getHostAddress() + " " + connectionsPort + " via NIC " + iface.getName());
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
	}
	
}
