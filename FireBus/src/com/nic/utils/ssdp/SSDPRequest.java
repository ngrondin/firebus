package com.nic.utils.ssdp;

public class SSDPRequest extends SSDPMessage
{
	protected String operation;
	protected String url;
	protected String http;

	public SSDPRequest(String o, String u)
	{
		super("");
		operation = o;
		url = u;
		http = "HTTP/1.1";
	}
	
	public SSDPRequest(String msg)
	{
		super(msg);
		
		String[] lines = msg.split("\r\n");
		String[] flParts = lines[0].split(" ");
		operation = flParts[0];
		url = flParts[1];
		http = flParts[2];
	}
	
	public String getOperation()
	{
		return operation;
	}

	public String toString()
	{
		String ret = super.toString();
		ret = operation + " " + url + " " + http + "\r\n" + ret;
		return ret;
	}
	
	
}
