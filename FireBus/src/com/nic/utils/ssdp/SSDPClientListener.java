package com.nic.utils.ssdp;

public interface SSDPClientListener
{
	public void registerServer(String host, int port, SSDPResponse response);
	
	public void deregisterServer(String host, int port, SSDPNotify notify);

}
