package com.nic.utils.ssdp;

public class SSDPResponse extends SSDPMessage
{
	protected String codeStr;
	protected int code;
	protected String http;

	public SSDPResponse(int c, String cs)
	{
		super("");
		code = c;
		codeStr = cs;
		http = "HTTP/1.1";
	}
	
	public SSDPResponse(String msg)
	{
		super(msg);
		
		String[] lines = msg.split("\r\n");
		String[] flParts = lines[0].split(" ");
		http = flParts[0];
		code = Integer.parseInt(flParts[1]);
		codeStr = flParts[2];
	}
	
	public int getCode()
	{
		return code;
	}

	public String toString()
	{
		String ret = super.toString();
		ret = http + " " + code + " " + codeStr + "\r\n" + ret;
		return ret;
	}
	
	
}
