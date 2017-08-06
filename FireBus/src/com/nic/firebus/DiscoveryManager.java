package com.nic.firebus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.nic.firebus.interfaces.DiscoveryListener;

public class DiscoveryManager extends Thread
{
	protected boolean quit;
	protected int nodeId;
	protected Address address;
	protected long lastRequestSent;
	protected DiscoveryListener discoveryListener;
	protected MulticastSocket socket;
	protected InetAddress multicastGroup;
	
	public DiscoveryManager(DiscoveryListener dl, int id, Address a)
	{
		discoveryListener = dl;
		nodeId = id;
		address = a;
		quit = false;
		try
		{
			socket = new MulticastSocket(1900);
			multicastGroup = InetAddress.getByName("239.255.255.255");
			socket.joinGroup(multicastGroup);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		setName("Firebus Discovery Manager");
		start();
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
			    	if(id != nodeId)
			    		sendAdvertisement();
			    }
			    if(received.startsWith("Firebus anouncement from"))
			    {
			    	String[] parts = received.split(" ");
			    	int id = Integer.parseInt(parts[3]);
			    	String ad = parts[4];
			    	int port = Integer.parseInt(parts[5]);
			    	if(id != nodeId)
			    	{
			    		discoveryListener.nodeDiscovered(id, ad, port);
					    //System.out.println(received);
			    	}
			    }
			}
			catch (IOException e) 
			{
				e.printStackTrace();
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
				String val = "Firebus discovery request from " + nodeId;
				multicastSend(val);
	        }
	        catch (IOException e) 
			{
	            e.printStackTrace();
	        }
			
			lastRequestSent = currentTime;
		}
	}
	
	public void sendAdvertisement()
	{
		try 
		{
			String val = "Firebus anouncement from " + nodeId + " " + address.getIPAddress() + " " + address.getPort();
			multicastSend(val);
		}
        catch (IOException e) 
		{
            e.printStackTrace();
        }
		
	}	
	
	protected void multicastSend(String value) throws IOException
	{
		byte[] buf = value.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, multicastGroup, 1900);
		socket.send(packet);
	}
}
