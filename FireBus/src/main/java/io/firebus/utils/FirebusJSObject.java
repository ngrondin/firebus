package io.firebus.utils;

import java.util.HashMap;
import java.util.Set;

import jdk.nashorn.api.scripting.AbstractJSObject;

public class FirebusJSObject extends AbstractJSObject
{
	protected HashMap<String, Object> map;

	public FirebusJSObject()
	{
		map = new HashMap<String, Object>();
	}
	
	public String getClassName()
	{
		return "Object";
	}

	public Object getMember(String arg0)
	{
		return map.get(arg0);
	}

	public boolean hasMember(String arg0)
	{
		return map.containsKey(arg0);
	}

	public Set<String> keySet()
	{
		return map.keySet();
	}

	public void removeMember(String arg0)
	{
		map.remove(arg0);
	}

	public void setMember(String arg0, Object arg1)
	{
		map.put(arg0, arg1);		
	}
}
