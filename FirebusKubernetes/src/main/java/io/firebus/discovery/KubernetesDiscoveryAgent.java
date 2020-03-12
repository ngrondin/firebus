package io.firebus.discovery;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import io.firebus.DiscoveryAgent;
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
		try {
			ApiClient client = Config.defaultClient();
	        client.setVerifyingSsl(false);
	        Configuration.setDefaultApiClient(client);
	        api = new CoreV1Api();
	        
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
	        setName("fbKubernetesDiscoveryAgent");
	        active = true;
		} catch(Exception e) {
			
		}
	}
	
	public void run() {
		
		try {        
			while(active) {
				V1Pod self = null;
				String namespace = null;
		        V1PodList podList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
		        for (V1Pod pod : podList.getItems()) {
		        	String podIp = pod.getStatus().getPodIP();
		        	if(podIp != null) {
			        	for(InetAddress addr : localAddresses) {
			        		if(addr != null && podIp.equals(addr.getHostAddress())) {
			        			self = pod;
			        			namespace = pod.getMetadata().getNamespace();
			        		}
			        	}
		        	}
		        }
	        	for(V1Pod pod: podList.getItems()) {
	        		if(namespace == null || pod.getMetadata().getNamespace().equals(namespace)) {
	        			if(pod.getMetadata().getLabels().containsKey("io.firebus") && pod.getStatus().getPodIP() != null && pod != self) {
	        				logger.fine("Found a pod hosting a firebus node : " + pod.getMetadata().getName());
	        				String podIp = pod.getStatus().getPodIP();
	        				int port = Integer.parseInt(pod.getMetadata().getLabels().get("io.firebus"));
	        				nodeCore.addKnownNodeAddress(podIp, port);
	        			}
	        		}
		        } 
		        sleep(300000);
			}
		} catch(Exception e) {
			logger.severe("Error while executing the Kubernetes discovery agent : " + e.getMessage());
		}
	}

	public void close() {
		active = false;
	}



}
