package com.nic.firebus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DiscoveryManager extends Thread
{
	protected boolean quit;
	protected long lastRequestSent;
	
	public DiscoveryManager()
	{
		quit = false;
		start();
	}
	
	public void run()
	{
		try
		{
			MulticastSocket socket = new MulticastSocket(4446);
			InetAddress group = InetAddress.getByName("239.255.255.255");
			socket.joinGroup(group);
			
			while(!quit)
			{
				try 
				{
					byte[] buf = new byte[256];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
				    socket.receive(packet);

				    String received = new String(packet.getData());
				    System.out.println(received);
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			socket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void sendDiscoveryRequest()
	{
		long currentTime = System.currentTimeMillis();
		if(currentTime > (lastRequestSent + 10000))
		{
			try 
			{
				String val = "This is a test";
				byte[] buf = val.getBytes();
							
				InetAddress group = InetAddress.getByName("239.255.255.255");
				DatagramSocket socket = new DatagramSocket();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, group, 4446);
				socket.send(packet);
				socket.close();
	        }
	        catch (IOException e) 
			{
	            e.printStackTrace();
	        }
			
			lastRequestSent = currentTime;
		}
	}
}
