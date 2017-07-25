package com.nic.utils.ssdp;

public class SSDPOKResponse extends SSDPResponse
{
	public SSDPOKResponse(String location, String server, String st, String usn)
	{
		super(200, "OK");
		putValue("HOST", "239.255.255.250:1900");
		putValue("CACHE-CONTROL", "max-age = 3600");
		putValue("EXT", "");
		putValue("LOCATION", location);
		putValue("SERVER", server);
		putValue("ST", st);
		putValue("USN", usn);
	}
}
