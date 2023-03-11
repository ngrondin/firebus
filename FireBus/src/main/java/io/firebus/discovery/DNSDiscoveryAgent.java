package io.firebus.discovery;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import io.firebus.DiscoveryAgent;
import io.firebus.data.DataMap;
import io.firebus.logging.Logger;

public class DNSDiscoveryAgent extends DiscoveryAgent  {
	private String srvName;
	private int portOverride;
	private boolean active;
	
	public void init() {
        setName("fbDNSDiscoveryAgent");	
        srvName = config.getString("name");
        if(srvName != null && srvName.equals(""))
        	srvName = null;
        portOverride = -1;
        if(config.containsKey("port") && !config.getString("port").equals(""))
        	portOverride = config.getNumber("port").intValue();
    }

	public void run() {
		active = true;
		while(active && srvName != null) {
			try {
				Hashtable<String, String> env = new Hashtable<String, String>();
				env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
				env.put("java.naming.provider.url", "dns:");
				InitialDirContext iDirC = new InitialDirContext(env);;
				Attributes attributes = iDirC.getAttributes(srvName, new String[] {"SRV"});
			    Attribute attributeMX = attributes.get("SRV");
			    if(attributeMX != null) {
				    for(int i = 0; i < attributeMX.size(); i++) {
				    	String[] parts = attributeMX.get(i).toString().split(" ");
				    	String portStr = parts[2];
				    	int port = portOverride != -1 ? portOverride : Integer.parseInt(portStr);
				    	String address = parts[3];
				    	if(address.endsWith("."))
				    		address = address.substring(0, address.length() - 1);
				    	Logger.info("fb.discovery.dns.discover.knownaddress", new DataMap("address", address, "port", port));
				    	nodeCore.addKnownNodeAddress(address, port);
				    }
			    }
			} catch(Exception e) {
				Logger.severe("fb.discovery.dns.discover", e);
			}
			try {
				synchronized(this) {
					this.wait(300000);
				}
			} catch(Exception e) {
				Logger.severe("fb.discovery.dns.sleeping", e);
			}
		}
		
	}

	public void close() {
		active = false;
		try {
			this.notify();
		} catch(Exception e) {
			Logger.severe("fb.discovery.dns.closing", e);
		}
	}

}
