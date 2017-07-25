package com.nic.utils.ssdp;

public class SSDPSearch extends SSDPRequest
{
	public SSDPSearch(String servicetype)
	{
		super("M-SEARCH", "*");
		putValue("HOST", "239.255.255.250:1900");
		putValue("MAN", "\"ssdp:discover\"");
		putValue("ST", servicetype);
		putValue("MX",  "3");
	}
}
