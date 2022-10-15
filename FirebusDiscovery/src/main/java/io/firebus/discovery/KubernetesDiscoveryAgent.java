package io.firebus.discovery;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import io.firebus.Address;
import io.firebus.DiscoveryAgent;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;

public class KubernetesDiscoveryAgent extends DiscoveryAgent {

	private Logger logger;
	protected boolean active = false;
	protected CoreV1Api api;
	protected List<InetAddress> localAddresses;
	
	public void init() {
		logger = Logger.getLogger("io.firebus");
        setName("fbKubernetesDiscoveryAgent");
	}
	
	public void run() {
		V1PodList podList = null;
		V1Pod self = null;
		String namespace = null;
		
		try {
	        localAddresses = new ArrayList<InetAddress>();
			Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
	        while (ifs.hasMoreElements()) 
	        {
	            NetworkInterface iface = ifs.nextElement();
	            if(!iface.isLoopback()  &&  iface.isUp())
	            {
	            	for(InterfaceAddress ifaceAddress : iface.getInterfaceAddresses()) {
	            		logger.fine("Added " + ifaceAddress.getAddress().getHostAddress() + " as a local address");
	            		localAddresses.add(ifaceAddress.getAddress());
	            	}
	            }
	        }	    

			ApiClient client = Config.defaultClient();
	        client.setVerifyingSsl(false);
	        Configuration.setDefaultApiClient(client);
	        api = new CoreV1Api();
	        
	        podList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
	        for (V1Pod pod : podList.getItems()) {
	        	String podIp = pod.getStatus().getPodIP();
	        	if(podIp != null) {
		        	for(InetAddress addr : localAddresses) {
		        		if(addr != null && podIp.equals(addr.getHostAddress())) {
		        			self = pod;
		        			namespace = pod.getMetadata().getNamespace();
		        			String labelValue = nodeCore.getConnectionManager().getPort() + "." + nodeCore.getNetworkName() + "." + nodeCore.getNodeId();
		                	String jsonPatchStr = "[{\"op\":\"add\",\"path\":\"/metadata/labels/io.firebus\",\"value\":\"" + labelValue + "\"}]";
	                		api.patchNamespacedPod(pod.getMetadata().getName(), namespace, new V1Patch(jsonPatchStr), null, null, null, null);
		        		}
		        	}
	        	}
	        }
		} catch(Exception e) {
			logger.severe("Error while trying to publish coordinates : " + e.getMessage());
		}


        active = true;
		try {
	        while(active) {
	        	if(podList != null) {
		        	for(V1Pod pod: podList.getItems()) {
		        		if(namespace == null || pod.getMetadata().getNamespace().equals(namespace)) {
		        			if(pod.getMetadata().getLabels().containsKey("io.firebus") && pod.getStatus().getPodIP() != null && pod != self) {
		        				String podIp = pod.getStatus().getPodIP();
		        				String labelValue = pod.getMetadata().getLabels().get("io.firebus");
	        					String[] parts = labelValue.split("\\.");
	        					int port = Integer.parseInt(parts[0]);
	        					String network = parts.length >= 2 ? parts[1] : null;
	        					String nodeId = parts.length >= 3 ? parts[2] : null;
	        					Address address = new Address(podIp, port);
		        				logger.fine("Found firebus node on " + pod.getMetadata().getName() + " : port " + port + " network " + network + (nodeId != null ? " nodeid " + nodeId : "" ));
	        					if(nodeCore.getNetworkName().equals(network) || network == null) {
	        						if(nodeId != null) {
		        						nodeCore.getDirectory().processDiscoveredNode(port, address);
	        						} else {
			        					nodeCore.addKnownNodeAddress(podIp, port);
	        						}
	        					}
		        			}
		        		}
			        } 
			        sleep(300000);
	        	}
			}
		} catch(Exception e) {
			logger.severe("Error while looking for other nodes : " + e.getMessage());
		}
	}

	public void close() {
		active = false;
	}



}
