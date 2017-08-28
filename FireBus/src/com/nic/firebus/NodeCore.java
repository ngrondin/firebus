package com.nic.firebus;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.exceptions.FunctionUnavailableException;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.FunctionInformation;
import com.nic.firebus.information.NodeInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.DiscoveryListener;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.interfaces.ServiceRequestor;
import com.nic.firebus.logging.FirebusSimpleFormatter;

public class NodeCore extends Thread implements DiscoveryListener
{
	private Logger logger = Logger.getLogger("com.nic.firebus");
	protected int nodeId;
	protected boolean quit;
	protected String networkName;
	protected long lastConnectionMaintenance;
	protected MessageQueue inboundQueue;
	protected MessageQueue outboundQueue;
	protected ConnectionManager connectionManager;
	protected FunctionManager functionManager;
	protected Directory directory;
	protected DiscoveryManager discoveryManager;
	protected CorrelationManager correlationManager;
	protected ArrayList<Address> knownAddresses;
	protected Cipher cipher;
	
	public NodeCore()
	{
		initialise(0, "firebus", "firebuspassword0");
	}
	
	public NodeCore(int p)
	{
		initialise(p, "firebus", "firebuspassword0");
	}

	public NodeCore(String network, String password)
	{
		initialise(0, network, password);
	}

	public NodeCore(int p, String network, String password)
	{
		initialise(p, network, password);
	}
	
	protected void initialise(int port, String n, String pw)
	{
		try
		{
			Random rnd = new Random();
			nodeId = rnd.nextInt();
			quit = false;
			networkName = n;	

			FileHandler fh = new FileHandler("FirebusNode_" + nodeId + ".log");
			fh.setFormatter(new FirebusSimpleFormatter());
			fh.setLevel(Level.FINE);
			logger.addHandler(fh);
			logger.setLevel(Level.FINE);
			
			inboundQueue = new MessageQueue();
			outboundQueue = new MessageQueue();
			directory = new Directory();
			connectionManager = new ConnectionManager(this, nodeId, networkName,  new SecretKeySpec(pw.getBytes(), "AES"), port);
			discoveryManager = new DiscoveryManager(this, nodeId, networkName, connectionManager.getPort());
			functionManager = new FunctionManager(this);
			correlationManager = new CorrelationManager(this);
			knownAddresses = new ArrayList<Address>();
			setName("Firebus Node");
			
			start();			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getNodeId()
	{
		return nodeId;
	}
	
	public String getNetworkName()
	{
		return networkName;
	}
	
	public void addKnownNodeAddress(String a, int p)
	{
		Address address = new Address(a, p);
		knownAddresses.add(address);
	}
	
	public void registerServiceProvider(ServiceInformation serviceInformation, ServiceProvider serviceProvider, int maxConcurrent)
	{
		functionManager.addFunction(serviceInformation, serviceProvider, maxConcurrent);
	}
	
	public void registerConsumer(ConsumerInformation consumerInfomation, Consumer consumer, int maxConcurrent)
	{
		functionManager.addFunction(consumerInfomation, consumer, maxConcurrent);
	}
	
	public ServiceInformation getServiceInformation(String functionName, int timeout)
	{
		InformationRequest ir = new InformationRequest(functionName, timeout, null, correlationManager, directory, nodeId);
		return ir.waitForResponse();
	}
		
	public Payload requestService(String serviceName, Payload payload, int timeout)  throws FunctionErrorException
	{
		ServiceRequest request = new ServiceRequest(serviceName, payload, timeout, null, correlationManager, directory, nodeId);
		return request.waitForResponse();
	}		

	public void requestService(String serviceName, Payload payload, int timeout, ServiceRequestor requestor)
	{
		new ServiceRequest(serviceName, payload, timeout, requestor, correlationManager, directory, nodeId);
	}	
	
	public void publish(String dataname, Payload payload)
	{
		logger.info("Publishing");
		Message msg = new Message(0, nodeId, Message.MSGTYPE_PUBLISH, dataname, payload);
		outboundQueue.addMessage(msg);
	}
	
	public void sendMessage(Message msg)
	{
		outboundQueue.addMessage(msg);
	}
	
	public void messageReceived(Message m, Connection c) 
	{
		logger.fine("Received Message");
		m.decode();
		int originatorId = m.getOriginatorId();
		int connectedId = c.getRemoteNodeId();
		if(connectedId != originatorId)
		{
			NodeInformation originatorNode = directory.getOrCreateNodeInformation(originatorId);
			originatorNode.addRepeater(connectedId);
		}
		inboundQueue.addMessage(m);
	}

	public void nodeDiscovered(int id, Address address)
	{
		directory.processDiscoveredNode(id,  address);
	}

	protected void maintainConnectionCount()
	{
		int i1 = 0;
		int i2 = 0;
		while(connectionManager.getConnectionCount() < 3  &&  !(i1 >= knownAddresses.size()  &&  i2 >= directory.getNodeCount()))
		{
			if(i1 < knownAddresses.size())
			{
				Address a = knownAddresses.get(i1);
				if(connectionManager.getConnectionByAddress(a) == null)
				{
					try
					{
						connectionManager.createConnection(a);
					}
					catch(Exception e)
					{
						logger.fine(e.getMessage());
					}
				}
				i1++;
			}
			else if(i2 < directory.getNodeCount())
			{
				NodeInformation ni = directory.getNode(i2);
				if(connectionManager.getConnectionByNodeId(ni.getNodeId()) == null  &&  !ni.isUnconnectable())
					connectionManager.obtainConnectionForNode(ni);
				i2++;
			}
		}
		lastConnectionMaintenance = System.currentTimeMillis();
	}
	
	protected void processNextInboundMessage()
	{
		logger.fine("Processing Inbound Message");
		Message msg = inboundQueue.getNextMessage();
		msg.decode();
		logger.finest("****Inbound****************\r\n" + msg);
		
		if(msg.getOriginatorId() != nodeId)
		{
			if(msg.getDestinationId() == 0  ||  msg.getDestinationId() == nodeId)
			{
				switch(msg.getType())
				{
					/*case Message.MSGTYPE_CONNECT:
						directory.processStateMessage(new String(msg.getPayload()));
						advertiseTo(msg.getOriginatorId());
						break;*/
					case Message.MSGTYPE_QUERYNODE:
						processNodeInformationRequest(msg.getOriginatorId());
						break;
					case Message.MSGTYPE_NODEINFORMATION:
						directory.processNodeInformation(new String(msg.getPayload().data));
						if(msg.getCorrelation() != 0)
							correlationManager.receiveResponse(msg);
						break;
					/*case Message.MSGTYPE_FINDSERVICE:
						processFindService(msg);
						break;*/
					case Message.MSGTYPE_REQUESTSERVICE:
						processServiceRequest(msg);
						break;
					case Message.MSGTYPE_GETFUNCTIONINFORMATION:
						processServiceInformationRequest(msg);
						break;
					case Message.MSGTYPE_SERVICERESPONSE:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_SERVICEERROR:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_SERVICEUNAVAILABLE:
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_SERVICEINFORMATION:
						directory.processServiceInformation(msg.getOriginatorId(), msg.getSubject(), msg.getPayload().data);
						correlationManager.receiveResponse(msg);
						break;
					case Message.MSGTYPE_PUBLISH:
						processPublish(msg);
				}
			}
			
			if(msg.getDestinationId() == 0  ||  msg.getDestinationId() != nodeId)
			{
				if(msg.getRepeatCount() > 0)
					outboundQueue.addMessage(msg.repeat());
			}			
		}

		inboundQueue.deleteNextMessage();
		logger.fine("Finished Processing Inbound Message");
	}

	protected void processNextOutboundMessage()
	{
		logger.fine("Processing Outbound Message");
		Message msg = outboundQueue.getNextMessage();
		logger.finest("****Oubound**************\r\n" + msg);
		msg.encode();
		Connection c = null;//msg.getConnection();
		int destinationNodeId = msg.getDestinationId();
		if(destinationNodeId != 0)
		{
			NodeInformation ni = directory.getNodeById(destinationNodeId);
			if(ni != null)
			{
				c = connectionManager.obtainConnectionForNode(ni);
				if(c == null)
				{
					int rpt = ni.getRandomRepeater();
					if(rpt != 0)
					{
						ni = directory.getNodeById(rpt);
						c = connectionManager.obtainConnectionForNode(ni);
					}
				}							
			}
		}

		if(c == null)
			connectionManager.broadcastToAllConnections(msg);
		else
			c.sendMessage(msg);
		
		outboundQueue.deleteNextMessage();
		logger.fine("Finished Processing Outbound Message");
	}
	
	/*
	protected void processFindService(Message msg)
	{
		if(functionManager.hasFunction(msg.getSubject()))
		{
			Message resp = new Message(msg.getOriginatorId(), nodeId, Message.MSGTYPE_NODEINFORMATION, msg.getSubject(), getNodeStateString().getBytes());
			resp.setCorrelation(msg.getCorrelation());
			outboundQueue.addMessage(resp);
		}
	}
	*/
	protected void processServiceRequest(Message msg)
	{
		try
		{
			functionManager.executeFunction(msg);
		}
		catch (FunctionUnavailableException e)
		{
			Message outMsg = new Message(msg.getOriginatorId(), nodeId, Message.MSGTYPE_SERVICEUNAVAILABLE, msg.getSubject(),new Payload(null,  e.getMessage().getBytes()));
			outMsg.setCorrelation(msg.getCorrelation());
			outboundQueue.addMessage(outMsg);
		}
	}

	protected void processServiceInformationRequest(Message msg)
	{
		FunctionInformation fi = functionManager.getFunctionInformation(msg.getSubject());
		if(fi instanceof ServiceInformation)
		{
			ServiceInformation si = (ServiceInformation)fi;
			Message outMsg = new Message(msg.getOriginatorId(), nodeId, Message.MSGTYPE_SERVICEINFORMATION, msg.getSubject(), new Payload(null, si.serialise()));
			outMsg.setCorrelation(msg.getCorrelation());
			outboundQueue.addMessage(outMsg);
		}
	}

	
	protected void processPublish(Message msg)
	{
		try
		{
			functionManager.executeFunction(msg);
		}
		catch (FunctionUnavailableException e)
		{
			logger.severe(e.getMessage());
		}		
	}
	
	protected void processNodeInformationRequest(int destinationNodeId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(connectionManager.getAddressStateString(nodeId));
		sb.append(functionManager.getFunctionStateString(nodeId));
		sb.append(directory.getDirectoryStateString(nodeId));
		Message msg = new Message(destinationNodeId, nodeId, Message.MSGTYPE_NODEINFORMATION, null, new Payload(null, sb.toString().getBytes()));
		outboundQueue.addMessage(msg);
	}
	

	
	public void run()
	{
		while(!quit)
		{
			try
			{
				correlationManager.checkExpiredCalls();
				if(lastConnectionMaintenance < System.currentTimeMillis() - 1000)
				{
					maintainConnectionCount();
				}
				if(inboundQueue.getMessageCount() > 0)
				{
					processNextInboundMessage();
				}
				else if(outboundQueue.getMessageCount() > 0)
				{
					processNextOutboundMessage();
				}
				else
				{
					Thread.sleep(10);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Node Id :" + nodeId + "\r\n");
		sb.append("-------Functions----------\r\n");
		sb.append(functionManager);
		sb.append("-------Connections--------\r\n");
		sb.append(connectionManager);
		sb.append("-------Directory----------\r\n");
		sb.append(directory);
		sb.append("\r\n");
		return sb.toString();
	}



}
