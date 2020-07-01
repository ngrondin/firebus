package io.firebus.utils;


public class FirebusDataUtil
{
/*
	public static DataMap convertJSObjectToDataObject(JSObject jso)
	{
		DataMap retObj = new DataMap();
		if(jso.getClassName().equals("Object"))
		{
			Iterator<String> it = jso.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				Object childObj = jso.getMember(key);
				if(childObj instanceof NativeArray)
					childObj = new FirebusJSArray((NativeArray)childObj);
				if(childObj instanceof JSObject)
				{
					JSObject childJSObject = (JSObject)childObj;
					if(childJSObject.getClassName().equals("Object"))
					{
						retObj.put(key, convertJSObjectToDataObject(childJSObject));
					}
					else if(childJSObject.getClassName().equals("Array"))
					{
						retObj.put(key, convertJSArrayToDataList(childJSObject));
					}
					else if(childJSObject.getClassName().equals("Date")  &&  childJSObject instanceof ScriptObjectMirror)
					{
						ScriptObjectMirror jsDate = (ScriptObjectMirror)childJSObject;
						long timestampLocalTime = ((Double) jsDate.callMember("getTime")).longValue(); 
						int timezoneOffsetMinutes = ((Double) jsDate.callMember("getTimezoneOffset")).intValue();
						retObj.put(key, new Date(timestampLocalTime + timezoneOffsetMinutes * 60 * 1000));
					}
				}
				else
				{
					retObj.put(key, childObj);
				}
			}
		}
		return retObj;
	}
	
	public static DataList convertJSArrayToDataList(JSObject jso)
	{
		DataList retList = new DataList();
		if(jso.getClassName().equals("Array"))
		{
			Iterator<Object> it = jso.values().iterator();
			while(it.hasNext())
			{
				Object childObj = it.next();
				if(childObj instanceof JSObject)
				{
					JSObject childJSObject = (JSObject)childObj;
					if(childJSObject.getClassName().equals("Object"))
					{
						retList.add(convertJSObjectToDataObject(childJSObject));
					}
					else if(childJSObject.getClassName().equals("Array"))
					{
						retList.add(convertJSArrayToDataList(childJSObject));
					}
					else if(childJSObject.getClassName().equals("Date")  &&  childJSObject instanceof ScriptObjectMirror)
					{
						ScriptObjectMirror jsDate = (ScriptObjectMirror)childJSObject;
						long timestampLocalTime = ((Double) jsDate.callMember("getTime")).longValue(); 
						int timezoneOffsetMinutes = ((Double) jsDate.callMember("getTimezoneOffset")).intValue();
						retList.add(new Date(timestampLocalTime + timezoneOffsetMinutes * 60 * 1000));
					}
				}
				else
				{
					retList.add(childObj);
				}
			}
		}
		return retList;
	}
	
	
	public static JSObject convertDataObjectToJSObject(DataMap dataObject)
	{
		if(dataObject != null) 
		{
			JSObject jso = new FirebusJSObject();
			Iterator<String> it = dataObject.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				DataEntity childObject = dataObject.get(key);
				if(childObject instanceof DataMap)
				{
					jso.setMember(key, convertDataObjectToJSObject((DataMap)childObject));
				}
				else if(childObject instanceof DataList)
				{
					jso.setMember(key, convertDataListToJSArray((DataList)childObject));
				}
				else
				{
					jso.setMember(key, ((DataLiteral)childObject).getObject());
				}
			}
			return jso;
		}
		else
		{
			return null;
		}
	}

	public static JSObject convertDataListToJSArray(DataList dataList)
	{
		JSObject jsa = new FirebusJSArray();
		for(int i = 0; i < dataList.size(); i++)
		{
			DataEntity childObject = dataList.get(i);
			if(childObject instanceof DataMap)
			{
				jsa.setSlot(i, convertDataObjectToJSObject((DataMap)childObject));
			}
			else if(childObject instanceof DataList)
			{
				jsa.setSlot(i, convertDataListToJSArray((DataList)childObject));
			}
			else
			{
				jsa.setSlot(i, ((DataLiteral)childObject).getObject());
			}		
		}
		return jsa;
	}
	*/
}
