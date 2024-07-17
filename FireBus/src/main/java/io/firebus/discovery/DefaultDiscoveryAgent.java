package io.firebus.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

import io.firebus.Address;
import io.firebus.DiscoveryAgent;
import io.firebus.NodeCore;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

public class DefaultDiscoveryAgent extends DiscoveryAgent
{
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
	            		Logger.fine("fb.discovery.default.interface", new DataMap("interface", xface.getName()));
	            	}
	            	catch(Exception e)
	            	{
	            		Logger.severe("fb.discovery.default.cannotjoin", new DataMap("interface", xface.getName()), e);
	            	}
                }
	        }	
		}
		catch(Exception e)
		{
			Logger.severe("fb.discovery.defaul", e);
			//e.printStackTrace();
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
			Logger.severe("fb.discovery.default.close", e);
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
						Logger.fine("fb.discovery.default.request", new DataMap("id", id, "network", net));
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
						Logger.fine("fb.discovery.default.announcement", new DataMap("id", id, "network", net, "address", address));
						nodeCore.getDirectory().processDiscoveredNode(id,  address);
			    	}
			    }
			}
			catch (IOException e) 
			{
				if(!quit)
					Logger.severe("fb.discovery.default.run", e);
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
						Logger.fine("fb.discovery.default.sent", new DataMap("interface", xface.getName()));
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

			Logger.fine("fb.discovery.default.sent", new DataMap("node", nodeId, "network", networkName, "address", localAddress.getHostAddress(), "port", connectionsPort, "interface",  iface.getName()));
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
		if(!socket.isClosed()) 
		{
	    	socket.setNetworkInterface(xface);
	    	socket.send(packet);
		}
	}
	
}
