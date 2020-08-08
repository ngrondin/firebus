package io.firebus.information;

import java.nio.ByteBuffer;


public class StreamInformation extends FunctionInformation
{
	protected String serviceName;
	protected boolean fullInformation;
	protected String requestMimeType;
	protected String requestContract;
	protected String responseMimeType;
	protected String responseContract;
	protected int rating;
	
	public StreamInformation(String sn)
	{
		serviceName = sn;
		fullInformation = false;
		rating = 0;
	}
	
	public StreamInformation(String rqmt, String rqc, String rpmt, String rpc)
	{
		requestMimeType = rqmt;
		requestContract = rqc;
		responseMimeType = rpmt;
		responseContract = rpc;
		fullInformation = true;
		rating = 100;
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
	
	public int getRating()
	{
		return rating;
	}
	
	public boolean hasFullInformation()
	{
		return fullInformation;
	}
	
	public void reduceRating()
	{
		rating--;
	}
	
	public void increaseRating()
	{
		rating++;
	}
	
	public byte[] serialise()
	{
		int len = 11;
		if(requestMimeType != null)
			len += requestMimeType.length();
		if(requestContract != null)
			len += requestContract.length();
		if(responseMimeType != null)
			len += responseMimeType.length();
		if(responseContract != null)
			len += responseContract.length();
		ByteBuffer bb = ByteBuffer.allocate(len);
		
		bb.put((byte)'t');
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
		bb.get();
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

	public String toString() 
	{
		return "Stream   : " + serviceName;
	}
}
