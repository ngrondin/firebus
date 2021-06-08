package io.firebus.adapters.http.inbound;

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
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.InboundReqRespHandler;
import io.firebus.utils.DataException;
import io.firebus.utils.DataMap;

public class PostMultiPartHandler extends InboundReqRespHandler 
{
	public PostMultiPartHandler(HttpGateway gw, Firebus f, DataMap c) 
	{
		super(gw, f, c);
	}

	protected Payload processRequest(HttpServletRequest req) throws ServletException, IOException, DataException
	{
		String path = req.getRequestURI();
		String shortPath = path.substring(req.getContextPath().length() + getHttpHandlerPath().length());
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

	protected void processResponse(HttpServletResponse resp, Payload payload) throws ServletException, IOException, DataException
	{
        PrintWriter writer = resp.getWriter();
        writer.print(payload.getString());
	}	

}
