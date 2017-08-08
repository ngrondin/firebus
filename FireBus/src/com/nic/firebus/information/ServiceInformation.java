package com.nic.firebus.information;

import java.nio.ByteBuffer;


public class ServiceInformation extends FunctionInformation
{
	protected boolean fullInformation;
	protected String requestMimeType;
	protected String requestContract;
	protected String responseMimeType;
	protected String responseContract;
	
	public ServiceInformation(String sn)
	{
		super(sn);
		fullInformation = false;
	}
	
	public ServiceInformation(String sn, String rqmt, String rqc, String rpmt, String rpc)
	{
		super(sn);
		requestMimeType = rqmt;
		requestContract = rqc;
		responseMimeType = rpmt;
		responseContract = rpc;
		fullInformation = true;
	}

	public String getRequestMimeType()
	{
		return requestMimeType;
	}
	
	public String getRequestCotnract()
	{
		return requestContract;
	}
	
	public String getResponseMimeType()
	{
		return responseMimeType;
	}

	public String getResponseContract()
	{
		return responseContract;
	}
	
	public boolean hasFullInformation()
	{
		return fullInformation;
	}
	
	public byte[] serialise()
	{
		int len = 10;
		if(requestMimeType != null)
			len += requestMimeType.length();
		if(requestContract != null)
			len += requestContract.length();
		if(responseMimeType != null)
			len += responseMimeType.length();
		if(responseContract != null)
			len += responseContract.length();
		ByteBuffer bb = ByteBuffer.allocate(len);
		
		if(requestMimeType != null)
		{
			bb.put((byte)requestMimeType.length());
			bb.put(requestMimeType.getBytes(), 0, requestMimeType.length());
		}
		else
		{
			bb.put((byte)0);
		}
		
		if(requestContract != null)
		{
			bb.putInt(requestContract.length());
			bb.put(requestContract.getBytes(), 0, requestContract.length());
		}
		else
		{
			bb.putInt(0);
		}
		
		if(responseMimeType != null)
		{
			bb.put((byte)responseMimeType.length());
			bb.put(responseMimeType.getBytes(), 0, responseMimeType.length());
		}
		else
		{
			bb.put((byte)0);
		}
		
		if(responseContract != null)
		{
			bb.putInt(responseContract.length());
			bb.put(responseContract.getBytes(), 0, responseContract.length());
		}
		else
		{
			bb.putInt(0);
		}
		
		return bb.array();
	}

	public void deserialise(byte[] bytes)
	{
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		int requestMimeTypeLen = bb.get();
		requestMimeType = new String(bytes, bb.position(), requestMimeTypeLen);
		bb.position(bb.position() + requestMimeTypeLen);
		
		int requestContractLen = bb.getInt();
		requestContract = new String(bytes, bb.position(), requestContractLen);
		bb.position(bb.position() + requestContractLen);

		int responseMimeTypeLen = bb.get();
		responseMimeType = new String(bytes, bb.position(), responseMimeTypeLen);
		bb.position(bb.position() + responseMimeTypeLen);

		int responseContractLen = bb.getInt();
		responseContract = new String(bytes, bb.position(), responseContractLen);
		bb.position(bb.position() + responseContractLen);

		fullInformation = true;
	}

	public String toLongString()
	{
		return requestMimeType + "\r\n" + requestContract + "\r\n" + responseMimeType + "\r\n" + responseContract + "\r\n"; 
	}
}