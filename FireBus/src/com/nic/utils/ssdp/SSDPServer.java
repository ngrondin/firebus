package com.nic.utils.ssdp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SSDPServer extends Thread implements HttpHandler
{
	protected MulticastSocket socket;
	protected HttpServer httpServer;
	protected boolean run;
	protected int discoveryPort;
	protected int httpPort;
	protected InetAddress discoveryAddress;
	protected UUID uniqueId;
	protected String name;
	protected String upnpDomain;
	protected String upnpDevice;
	protected String upnpManufacturer;
	protected String upnpDeviceWebpage;
	protected ArrayList<String> upnpServices;
	protected HashMap<String, String> upnpHeaderFields;
	
	private static final Logger logger = Logger.getLogger(SSDPServer.class.getName());
	
	public SSDPServer(String n, String dom, String dev)
	{
		discoveryPort = 1900;
		httpPort = 8000;
		run = true;
		uniqueId = UUID.randomUUID();
		name = n;
		upnpDomain = dom;
		upnpDevice = dev;
		upnpServices = new ArrayList<String>();
		upnpManufacturer = "";
		upnpDeviceWebpage = "";
		upnpHeaderFields = new HashMap<String, String>();

		boolean bound = false;
		while(!bound)
		{
			try
			{
				httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
				bound = true;
			}
			catch(IOException e)
			{
				httpPort++;
			}
		}
	
		try
		{
			httpServer.createContext("/upnp", this);
			httpServer.setExecutor(null); 
			httpServer.start();
			logger.info("listening for http requests on " + httpPort);

			discoveryAddress = InetAddress.getByName("239.255.255.250");
			socket = new MulticastSocket(discoveryPort); 

			Enumeration<NetworkInterface> ifs =  NetworkInterface.getNetworkInterfaces();
	        while (ifs.hasMoreElements()) 
	        {
	            NetworkInterface xface = ifs.nextElement();
	            if(!xface.isLoopback()  &&  xface.isUp())
	            {
                	socket.joinGroup(new InetSocketAddress(discoveryAddress, discoveryPort), xface);
                }
	        }
			logger.info("listening for ssdp requests on " + discoveryPort);
			
			setName("SSDP Discovery Server");
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
				
		start();
	}
	
	public void addService(String service)
	{
		upnpServices.add(service);
	}
	
	public void setManufacturer(String m)
	{
		upnpManufacturer = m;
	}
	
	public void setDeviceWebPage(String w)
	{
		upnpDeviceWebpage = w;
	}
	
	public void addHeaderField(String k, String v)
	{
		upnpHeaderFields.put(k.toUpperCase(), v);
	}
	
	public void run()
	{
		byte[] recvBuf = new byte[1000];
		DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
		try
		{
			while(run)
			{
				try
				{
					socket.receive(packet);
					SSDPRequest request = new SSDPRequest(new String(packet.getData()).trim());
					logger.info("heard request from " + packet.getAddress() + ":" + packet.getPort());
					//System.out.println( request + "\r\n");
				    if (request.getOperation().equals("M-SEARCH")  &&  request.getValue("MAN").equals("\"ssdp:discover\"")) 
				    {
				    	if(request.getValue("ST").equals("upnp:all"))
				    	{
					    	ssdpRespondRootDevice(packet.getAddress(), packet.getPort());
					    	ssdpRespondService(null, packet.getAddress(), packet.getPort());
					    	logger.info("sent back notification of presence to " + packet.getAddress() + ":" + packet.getPort());
				    	}
				    	else if(request.getValue("ST").equals("upnp:rootdevice"))
				    	{
					    	ssdpRespondRootDevice(packet.getAddress(), packet.getPort());
				    	}
				    	else if(request.getValue("ST").equals("uuid:" + uniqueId))
				    	{
					    	ssdpRespondRootDevice(packet.getAddress(), packet.getPort());
					    	ssdpRespondService(null, packet.getAddress(), packet.getPort());
					    	logger.info("sent back notification of presence to " + packet.getAddress() + ":" + packet.getPort());
				    	}
				    	else if(request.getValue("ST").startsWith("urn:" + upnpDomain + ":service:"))
				    	{
				    		for(int i = 0; i < upnpServices.size(); i++)
				    		{
				    			if(request.getValue("ST").equals("urn:" + upnpDomain + ":service:" + upnpServices.get(i) + ":1"))
				    			{
							    	ssdpRespondService(upnpServices.get(i), packet.getAddress(), packet.getPort());
							    	logger.info("sent back notification of presence to " + packet.getAddress() + ":" + packet.getPort());
				    			}
				    		}
				    	}
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
	
	public void ssdpRespondRootDevice(InetAddress addr, int port)
	{
		InetAddress localAddress = getLocalAddressForRemoteConnection(addr, port);
		
		SSDPOKResponse resp = new SSDPOKResponse("http:/" + localAddress + ":" + httpPort + "/upnp", "Custom", "upnp:rootdevice", "uuid:" + uniqueId + "::upnp:rootdevice");
		multicastSend(resp.toString(), addr, port);
		
		resp = new SSDPOKResponse("http:/" + localAddress + ":" + httpPort + "/upnp", "Custom", "uuid:" + uniqueId, "uuid:" + uniqueId);
		multicastSend(resp.toString(), addr, port);

		resp = new SSDPOKResponse("http:/" + localAddress + ":" + httpPort + "/upnp", "Custom", "urn:" + upnpDomain + ":device:" + upnpDevice + ":1", "uuid:" + uniqueId + "::urn:" + upnpDomain + ":device:" + upnpDevice + ":1");
		multicastSend(resp.toString(), addr, port);
		
    	logger.info("sent ssdp root device response back to " + addr + ":" + port);
	}
	
	public void ssdpRespondService(String service, InetAddress addr, int port)
	{
		InetAddress localAddress = getLocalAddressForRemoteConnection(addr, port);

		for(int i = 0; i < upnpServices.size(); i++)
		{
			if(service == null  ||  (service != null  &&  upnpServices.get(i).equals(service)))
			{
				SSDPOKResponse resp = new SSDPOKResponse("http:/" + localAddress + ":" + httpPort + "/info", "Custom", "urn:" + upnpDomain + ":service:" + upnpServices.get(i) + ":1", "uuid:" + uniqueId + "::urn:" + upnpDomain + ":service:" + upnpServices.get(i) + ":1");
				Iterator<String> it = upnpHeaderFields.keySet().iterator();
				while(it.hasNext())
				{
					String key = it.next();
					resp.putValue(key, upnpHeaderFields.get(key));
				}
				multicastSend(resp.toString(), addr, port);
			}
		}

		logger.info("sent ssdp services response back to " + addr + ":" + port);
	}


		/*
	public void  ssdpNotify()
	{
    	String respStringStart = "NOTIFY * HTTP/1.1\r\n";
    	respStringStart += "HOST: 239.255.255.250:1900\r\n";
    	respStringStart += "CACHE-CONTROL: max-age = 3600\r\n";
    	respStringStart += "LOCATION: http://192.168.0.3:" + server.getListeningPort() + "\r\n";
    	respStringStart += "SERVER: Custom\r\n";
    	respStringStart += "NTS: ssdp:alive\r\n";
    	
    	String respString = respStringStart;
    	respString += "NT: ST: upnp:rootdevice\r\n";
    	respString += "USN: uuid:" + server.getUniqueId() + "::upnp:rootdevice\r\n";
    	multicastSend(respString, discoveryAddress, discoveryPort);

    	respString = respStringStart;
    	respString += "NT: uuid:" + server.getUniqueId() + "\r\n";
    	respString += "USN: uuid:" + server.getUniqueId() + "\r\n";
    	multicastSend(respString, discoveryAddress, discoveryPort);

    	respString = respStringStart;
    	respString += "NT: urn:nicnet:device:qnetserver:1\r\n";
    	respString += "USN: uuid:" + server.getUniqueId() + "::urn:nicnet:device:qnetserver:1\r\n";
    	multicastSend(respString, discoveryAddress, discoveryPort);

    	respString = respStringStart;
    	respString += "NT: urn:nicnet:service:qnetserver:1\r\n";
    	respString += "USN: uuid:" + server.getUniqueId() + "::urn:nicnet:service:qnetserver:1\r\n";
    	multicastSend(respString, discoveryAddress, discoveryPort);
}
	*/
	public void multicastSend(String str, InetAddress addr, int port)
	{
		try
		{
	    	byte[] sendData = str.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, addr, port);
			socket.send(sendPacket);
			/*
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
	        */
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}				
	}
	
	public void handle(HttpExchange t) throws IOException
	{
		String response = "<?xml version=\"1.0\"?><root xmlns=\"urn:schemas-upnp-org:device-1-0\"><specVersion><major>1</major><minor>0</minor></specVersion><URLBase>http:/" + t.getLocalAddress() + "</URLBase><device><deviceType>urn:" + upnpDomain + ":device:Basic:1</deviceType><friendlyName>" + name + "</friendlyName><manufacturer>NGI</manufacturer><modelName> </modelName><modelNumber> </modelNumber><modelDescription></modelDescription><serialNumber></serialNumber><modelURL></modelURL><manufacturerURL></manufacturerURL><UDN>uuid:" + uniqueId + "</UDN><serviceList></serviceList></device></root>";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();		
	}
	
    private InetAddress getLocalAddressForRemoteConnection(InetAddress remoteAddress, int port) 
    {      
    	try
    	{
	    	DatagramSocket sock = new DatagramSocket();
	        sock.connect(remoteAddress, port);
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
    
	public void close()
	{
		run = false;
		socket.close();
		httpServer.stop(0);
	}
	
	/*
	public static void main(String[] args)
	{
		Logger.getLogger("com.nic").addHandler(new NicConsoleHandler());
		Logger.getLogger("com.nic").setLevel(Level.ALL);
		new SSDPServer("qNet Server", "nicnet", "qnetserver").addService("qnetservice");
	}
*/
}
