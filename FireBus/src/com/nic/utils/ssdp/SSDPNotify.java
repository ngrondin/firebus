package com.nic.utils.ssdp;

public class SSDPNotify extends SSDPRequest
{
	public SSDPNotify(String location, String server, String nt, String usn)
	{
		super("NOTIFY", "*");
		putValue("HOST", "239.255.255.250:1900");
		putValue("CACHE-CONTROL", "max-age = 3600");
		putValue("LOCATION", location);
		putValue("SERVER", server);
		putValue("NT", nt);
		putValue("NTS",  "ssdp:alive");
		putValue("USN", usn);
	}
}
