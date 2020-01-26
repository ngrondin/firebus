package com.nic.firebus.adapters.http.outbound;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;

public class PostHandler extends OutboundHandler 
{
	private static final long serialVersionUID = 1L;


	public PostHandler(DataMap c, Firebus f) 
	{
		super(c, f);
	}

	protected HttpUriRequest processRequest(Payload payload) throws ServletException, IOException, DataException 
	{
		String path = this.baseUrl;
		if(path.endsWith("/"))
			path = path.substring(0, path.length() - 1);
		String relativePath = payload.metadata.get("path");
		if(relativePath != null) 
		{
			if(relativePath.startsWith("/"))
				relativePath = relativePath.substring(1);
			path = path + "/" + relativePath;
		}
		HttpPost httppost = new HttpPost(path);
		httppost.setEntity(new ByteArrayEntity(payload.data));
		return httppost;
	}


	protected Payload processResponse(HttpEntity response) throws ServletException, IOException, DataException 
	{
		InputStream is = response.getContent();
		byte[] data = new byte[is.available()];
		is.read(data);
		Payload payload = new Payload(data);
		return payload;
	}

}
