package io.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataFilter;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;

public class LocalDataAdapter  extends Adapter  implements ServiceProvider, Consumer {
	private Logger logger = Logger.getLogger("io.firebus.adapters");
	private String path;
	private Map<String, DataList> collections;
	
	public LocalDataAdapter(DataMap c) {
		super(c);
		if(config.containsKey("path")) {
			path = config.getString("path");
		} else {
			path = System.getProperty("user.home") + File.separator + "firebusdata";
		}
		collections = new HashMap<String, DataList>();
		try {
			File dir = new File(path);
			if(!dir.exists()) 
				dir.mkdir();
			File[] files = dir.listFiles();
			for(File file: files) {
				String name = file.getName().substring(0, file.getName().lastIndexOf("."));
				DataList collection = new DataList(new FileInputStream(file));
				collections.put(name, collection);
			}
		} catch(Exception e) {
			logger.severe(e.getMessage());
		}
	}
	
	private void saveData(String collection) {
		try {
			DataList list = collections.get(collection);
			FileOutputStream fos = new FileOutputStream(path + File.separator + collection + ".json");
			list.write(fos);
			fos.close();
		} catch(Exception e) {
			logger.severe(e.getMessage());
		}
	}

	public void consume(Payload payload) {
		try
		{
			DataMap request = new DataMap(payload.getString());
			upsert(request);
		}
		catch(DataException e)
		{
			logger.severe("Error consuming data : " + e.getMessage());
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		long start = System.currentTimeMillis();
		Payload response = new Payload();
		DataMap responseJSON = new DataMap();
		try
		{
			DataMap request = new DataMap(payload.getString());
			logger.finer("Starting mongo request : " + request.toString(0, true));
			if(request.containsKey("tuple")) 
			{
				DataList list = aggregate(request);
				responseJSON.put("result", list);
			}
			else if(request.containsKey("filter")) 
			{
				DataList list = get(request);
				responseJSON.put("result", list);
			}
			else if(request.containsKey("key") || request.containsKey("multi"))
			{
				try {
					if(request.containsKey("key")) {
						upsert(request);
					} else if(request.containsKey("multi")) {
						DataList multi = request.getList("multi");
						for(int i = 0; i < multi.size(); i++) 
							upsert(multi.getObject(i));
					}
					responseJSON.put("result", "ok");	
				} catch(Exception e) {
					throw new FunctionErrorException("Error in db multi transaction", e);
				}
			}
			response = new Payload(null, responseJSON.toString().getBytes());
			long duration = System.currentTimeMillis() - start;
			if(duration > 2000) 
				logger.warning("Long running mongo request (" + duration + "ms): " + request.toString(0, true));
			logger.finer("Returning mongo response in " + duration + "ms");
		}
		catch(Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.severe("Error processing data request\r\n" + sw.toString());
			throw new FunctionErrorException("Error in db service", e);
		}		
		return response;
	}

	public ServiceInformation getServiceInformation() {
		return null;
	}

	
	private DataList get(DataMap request) throws DataException {
		DataList responseList = null;
		String collection = request.getString("object");
		DataMap filter = request.getObject("filter");
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
		responseList = find(collection, filter);
		for(int i = 0; i < (page * pageSize); i++)
			responseList.remove(0);
		while(responseList.size() > pageSize) {
			responseList.remove(pageSize);
		}
		return responseList;
	}
	
	private DataList aggregate(DataMap request) throws DataException {
		return null;
	}
	
	private void upsert(DataMap packet) {
		String collection = packet.getString("object");
		//String operation = packet.getString("operation");
		DataMap data = packet.getObject("data");
		DataMap key = packet.getObject("key");		
		if(!collections.containsKey(collection))
			collections.put(collection, new DataList());
		DataList existingList = find(collection, key);
		if(existingList.size() > 0) {
			for(int i = 0; i < existingList.size(); i++) {
				DataMap item = existingList.getObject(i);
				item.merge(data);
			}
		} else {
			DataMap item = new DataMap();
			item.merge(data);
			item.merge(key);
			collections.get(collection).add(item);
		}
		saveData(collection);
	}
	
	private DataList find(String collection, DataMap filterData) {
		DataFilter filter = new DataFilter(filterData);
		DataList list = collections.get(collection);
		DataList ret = new DataList();
		if(list != null) {
			for(int i = 0; i < list.size(); i++) {
				DataMap listItem = list.getObject(i);
				if(filter.apply(listItem))
					ret.add(listItem);
			}
		}
		return ret;
	}
}
