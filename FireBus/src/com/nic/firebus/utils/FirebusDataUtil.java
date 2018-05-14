package com.nic.firebus.utils;

import java.util.Date;
import java.util.Iterator;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeArray;

public class FirebusDataUtil
{

	public static JSONObject convertJSObjectToDataObject(JSObject jso)
	{
		JSONObject retObj = new JSONObject();
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
						long timestampLocalTime = (long) (double) jsDate.callMember("getTime"); 
						int timezoneOffsetMinutes = (int) (double) jsDate.callMember("getTimezoneOffset");
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
	
	public static JSONList convertJSArrayToDataList(JSObject jso)
	{
		JSONList retList = new JSONList();
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
						long timestampLocalTime = (long) (double) jsDate.callMember("getTime"); 
						int timezoneOffsetMinutes = (int) (double) jsDate.callMember("getTimezoneOffset");
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
	
	
	public static JSObject convertDataObjectToJSObject(JSONObject dataObject)
	{
		JSObject jso = new FirebusJSObject();
		Iterator<String> it = dataObject.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			JSONEntity childObject = dataObject.get(key);
			if(childObject instanceof JSONObject)
			{
				jso.setMember(key, convertDataObjectToJSObject((JSONObject)childObject));
			}
			else if(childObject instanceof JSONList)
			{
				jso.setMember(key, convertDataListToJSArray((JSONList)childObject));
			}
			else
			{
				jso.setMember(key, ((JSONLiteral)childObject).getObject());
			}
		}
		return jso;
	}

	public static JSObject convertDataListToJSArray(JSONList dataList)
	{
		JSObject jsa = new FirebusJSArray();
		for(int i = 0; i < dataList.size(); i++)
		{
			JSONEntity childObject = dataList.get(i);
			if(childObject instanceof JSONObject)
			{
				jsa.setSlot(i, convertDataObjectToJSObject((JSONObject)childObject));
			}
			else if(childObject instanceof JSONList)
			{
				jsa.setSlot(i, convertDataListToJSArray((JSONList)childObject));
			}
			else
			{
				jsa.setSlot(i, ((JSONLiteral)childObject).getObject());
			}		
		}
		return jsa;
	}
	
}
