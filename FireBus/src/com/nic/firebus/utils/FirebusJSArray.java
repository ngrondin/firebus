package com.nic.firebus.utils;

import java.util.ArrayList;
import java.util.Collection;

import jdk.nashorn.api.scripting.AbstractJSObject;

public class FirebusJSArray extends AbstractJSObject
{
	protected ArrayList<Object> list;
	
	public FirebusJSArray()
	{
		list = new ArrayList<Object>();
	}
	
	public String getClassName()
	{
		return "Array";
	}

	public Object getSlot(int arg0)
	{
		return list.get(arg0);
	}

	public boolean hasSlot(int arg0)
	{
		return list.size() > arg0;
	}

	public boolean isArray()
	{
		return true;
	}

	public void setSlot(int arg0, Object arg1)
	{
		list.add(arg0, arg1);
	}

	public Collection<Object> values()
	{
		return list;
	}

}
