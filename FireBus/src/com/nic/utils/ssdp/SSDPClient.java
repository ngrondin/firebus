package com.nic.utils.ssdp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Logger;


public class SSDPClient extends Thread
{
	protected MulticastSocket socket;
	protected SSDPClientListener clientListener;
	protected String upnpService;
	protected String upnpDomain;
	protected boolean run;
	
	private static final Logger logger = Logger.getLogger(SSDPClient.class.getName());
	
	public SSDPClient(String dom, String srv, SSDPClientListener cl)
	{
		run = true;
		clientListener = cl;
		upnpService = srv;
		upnpDomain = dom;
		try
		{
			//InetSocketAddress socketAddress =  new InetSocketAddress("239.255.255.250", 1900);
			socket = new MulticastSocket();
			socket.joinGroup(InetAddress.getByName("239.255.255.250"));

			/*
			Enumeration<NetworkInterface> ifs =  NetworkInterface.getNetworkInterfaces();
	        while (ifs.hasMoreElements()) 
	        {
	            NetworkInterface xface = ifs.nextElement();
	            if(!xface.isLoopback()  &&  xface.isUp())
	            {
                	socket.joinGroup(socketAddress, xface);
                }
	        }
	        */
			logger.info("listening for ssdp notifications");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		setName("SSDP Client");
		start();
	}
	
	public void run()
	{
		sendDiscoveryRequest();
		try
		{
			while(run)
			{
				byte[] recvBuf = new byte[1000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

				try
				{
					socket.receive(packet);
					SSDPResponse response = new SSDPResponse(new String(packet.getData()).trim());

					//String message = new String(packet.getData()).trim();
					logger.info("received ssdp response");
				    if (response.getCode() == 200) 
				    {
				    	String host = packet.getAddress().getHostAddress();
				    	int port = packet.getPort();
				    	clientListener.registerServer(host, port, response);
				    }				
				}
				catch(SocketException e)
				{
					
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		logger.info("discovery listener exiting");
	}
	
	public void sendDiscoveryRequest()
	{
		try 
		{
			SSDPSearch message = new SSDPSearch("urn:" + upnpDomain + ":service:" + upnpService + ":1");
			byte[] sendData = message.toString().getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("239.255.255.250"), 1900);
			
			Enumeration<NetworkInterface> ifs =  NetworkInterface.getNetworkInterfaces();
	        while (ifs.hasMoreElements()) 
	        {
	            NetworkInterface xface = ifs.nextElement();
	            if(xface.isUp())
	            {
	            	socket.setNetworkInterface(xface);
	            	socket.send(sendPacket);
	            }
	        }
	        
			logger.info("Sent ssdp search request");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}		
	}
	
	public MulticastSocket getSocket()
	{
		return socket;
	}

	public void close()
	{
		run = false;
		socket.close();
	}
	
	public static void main(String[] args)
	{
		new SSDPClient("nicnet", "qnetservice", null);
	}
}
