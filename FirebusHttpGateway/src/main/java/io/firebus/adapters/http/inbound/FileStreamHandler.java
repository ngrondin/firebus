package io.firebus.adapters.http.inbound;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import io.firebus.Firebus;
import io.firebus.Payload;
import io.firebus.StreamEndpoint;
import io.firebus.adapters.http.HttpGateway;
import io.firebus.adapters.http.InboundHandler;
import io.firebus.data.DataMap;
import io.firebus.utils.StreamReceiver;
import io.firebus.utils.StreamSender;

public class FileStreamHandler extends InboundHandler  {

	protected String streamName;
	
	public FileStreamHandler(HttpGateway gw, Firebus f, DataMap c) {
		super(gw, f, c);
		streamName = handlerConfig.getString("stream");	
	}

	@Override
	public void inboundService(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		String action = req.getMethod().equals("GET") ? "get" : "put";
		DataMap request = new DataMap();
		request.put("action", action);

		Payload payload = new Payload();
		if(securityHandler != null)
			securityHandler.enrichFirebusRequest(req, payload);
		enrichFirebusRequestDefault(req, payload);

		if(action.equals("get")) {
			Enumeration<String> en = req.getParameterNames();
			while(en.hasMoreElements())
			{
				String paramName = en.nextElement();
				request.put(paramName, req.getParameter(paramName));
			}
			payload.setData(request.toString());
			payload.metadata.put("mime", "application/json");
			StreamEndpoint sep = firebus.requestStream(streamName, payload, 10000);
			resp.setStatus(200);
			OutputStream os = resp.getOutputStream();
			new StreamReceiver(os, sep).sync();
			os.flush();
			os.close();
			//sep.close(); // The sender will close the stream
		} else if(action.equals("put")) {
			InputStream is = null;
			Iterator<Part> it = req.getParts().iterator();
			while(it.hasNext()) {
				Part part = it.next();
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
								request.put("filename", filename);
								is = part.getInputStream();
							}
						}
						else
						{
							int size = (int)part.getSize();
							byte[] data = new byte[size];
							InputStream pis = part.getInputStream();
							pis.read(data);
							pis.close();
							String value = new String(data);
							request.put(name, value);
						}
					}
				}
			}
			
			if(is != null) {
				payload.setData(request.toString());
				payload.metadata.put("mime", "application/json");
				StreamEndpoint sep = firebus.requestStream(streamName, payload, 10000);
				new StreamSender(is, sep).sync();
				resp.setStatus(200);
				is.close();
				
				OutputStream os = resp.getOutputStream();
				new StreamReceiver(os, sep).sync();
				os.flush();
				os.close();
				sep.close();
			}
		} else {
			resp.setStatus(400);
		}
		
	}


}
