package io.firebus.adapters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import io.firebus.Payload;
import io.firebus.data.DataException;
import io.firebus.data.DataFilter;
import io.firebus.data.DataList;
import io.firebus.data.DataMap;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Logger;

public class LocalDataAdapter  extends Adapter  implements ServiceProvider, Consumer {
	private String path;
	private Map<String, DataList> collections;
	
	public LocalDataAdapter(DataMap c) {
		super(c);
		if(config.containsKey("path")) {
			path = config.getString("path");
			if(path.startsWith("./"))
				path = System.getProperty("user.home") + path.substring(1);
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
			Logger.severe("rb.adapter.localdata.init", e);
		}
	}
	
	private void saveData(String collection) {
		try {
			DataList list = collections.get(collection);
			FileOutputStream fos = new FileOutputStream(path + File.separator + collection + ".json");
			list.write(fos);
			fos.close();
		} catch(Exception e) {
			Logger.severe("rb.adapter.localdata.save", e);
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
			Logger.severe("rb.adapter.localdata.consume", e);
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException {
		long start = System.currentTimeMillis();
		Payload response = new Payload();
		DataMap responseJSON = new DataMap();
		try
		{
			DataMap request = new DataMap(payload.getString());
			Logger.finer("fb.adapter.localdata.request", request);
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
				Logger.warning("rb.adapter.localdata.longtx", new DataMap("ms", duration, "req", request));
			Logger.finer("fb.adapter.localdata.resp", new DataMap("ms", duration));
		}
		catch(Exception e)
		{
			Logger.severe("rb.adapter.localdata.request", e);
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
		DataMap sort = request.getObject("sort");
		//DataList tuple = request.getList("tuple");
		int page = request.containsKey("page") ? request.getNumber("page").intValue() : 0;
		int pageSize = request.containsKey("pagesize") ? request.getNumber("pagesize").intValue() : 50;
		DataList unsortedList = find(collection, filter);
		for(int i = 0; i < (page * pageSize) && unsortedList.size() > 0; i++)
			unsortedList.remove(0);
		while(unsortedList.size() > pageSize && unsortedList.size() > pageSize) {
			unsortedList.remove(pageSize);
		}
		if(sort != null) {
			responseList = new DataList();
			for(int i = 0; i < unsortedList.size(); i++) {
				DataMap o = unsortedList.getObject(i);
				int j = 0;
				for(; j < responseList.size() && !isBefore(o, responseList.getObject(j), sort); j++);
				responseList.add(j, o);
			}
		} else {
			responseList = unsortedList;
		}
		return responseList;
	}
	
	private DataList aggregate(DataMap request) throws DataException {
		return new DataList();
	}
	
	private void upsert(DataMap packet) {
		String collection = packet.getString("object");
		String operation = packet.getString("operation");
		DataMap data = packet.getObject("data");
		DataMap key = packet.getObject("key");		
		if(!collections.containsKey(collection))
			collections.put(collection, new DataList());
		if(operation != null && operation.equals("delete")) {
			DataList list = collections.get(collection);
			DataFilter filter = new DataFilter(key);
			if(list != null) {
				for(int i = list.size() - 1; i >= 0; i--) {
					DataMap listItem = list.getObject(i);
					if(filter.apply(listItem)) {
						list.remove(i);
					}
				}
			}
		} else {
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
	
	private boolean isBefore(DataMap a, DataMap b, DataMap sort) {
		int k = 0;
		while(sort.containsKey(String.valueOf(k))) {
			DataMap p = sort.getObject(String.valueOf(k));
			String attr = p.getString("attribute");
			int dir = p.containsKey("dir") ? p.getNumber("dir").intValue() : 1;
			String as = a.getString(attr);
			if(as != null) {
				String bs = b.getString(attr);
				if(bs != null) {
					int i = dir * as.compareTo(bs);
					if(i > 0) {
						return false;
					} else if(i < 0) {
						return true;
					}
				} else {
					return true;
				}
			} else {
				return false;
			}
			k++;
		}
		return true;
	}
}
