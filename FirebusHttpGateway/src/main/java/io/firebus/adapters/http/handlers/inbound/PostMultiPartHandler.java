package io.firebus.adapters.http.handlers.inbound;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.adapters.http.HttpRequest;
import io.firebus.adapters.http.HttpResponse;
import io.firebus.adapters.http.handlers.ReqRespHandler;
import io.firebus.data.DataException;
import io.firebus.data.DataMap;

public class PostMultiPartHandler extends ReqRespHandler 
{
	public PostMultiPartHandler(Firebus f, DataMap c) 
	{
		super(f, c);
	}

	protected Payload produceFirebusRequest(HttpRequest req) throws Exception
	{
		String shortPath = req.getShortPath();
		Payload payload = new Payload();
		payload.metadata.put("post", shortPath);
		Iterator<Part> it = req.getParts().iterator();
		while(it.hasNext())
		{
			Part part = it.next();
			int size = (int)part.getSize();
			byte[] data = new byte[size];
			InputStream is = part.getInputStream();
			is.read(data);
			is.close();
			String contentType = part.getContentType();
			String contentDispo = part.getHeader("content-disposition");
			String[] dispoSegments = contentDispo.split(";");
			if(dispoSegments.length > 0 && dispoSegments[0].equals("form-data"))
			{
				String name = null;
				for(int i = 1; i < dispoSegments.length; i++)
					if(dispoSegments[i].trim().startsWith("name="))
						name = dispoSegments[i].trim().substring(5);
				if(name != null)
				{
					if(name.startsWith("\"") && name.endsWith("\""))
						name = name.substring(1, name.length() - 1);
					if(name.equals("file"))
					{
						String filename = null;
						for(int i = 1; i < dispoSegments.length; i++)
							if(dispoSegments[i].trim().startsWith("filename="))
								filename = dispoSegments[i].trim().substring(9);
						if(filename != null)
						{
							if(filename.startsWith("\"") && filename.endsWith("\""))
								filename = filename.substring(1, filename.length() - 1);
							payload.metadata.put("filename", filename);
							if(contentType != null)
								payload.metadata.put("mime", contentType);
							payload.setData(data);
						}
					}
					else
					{
						String value = new String(data);
						payload.metadata.put(name, value);
					}
				}
			}
		}
		return payload;
	}

	protected HttpResponse produceHttpResponse(Payload payload) throws Exception
	{
		return new HttpResponse(200, payload.getBytes());
	}	

}
