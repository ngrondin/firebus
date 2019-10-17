package com.nic.firebus;
	
import java.io.FileInputStream;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jdk.nashorn.api.scripting.JSObject;

import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.DataMap;


public class UnitTest
{
	public static void main(String args[])
	{
		try
		{
			FileInputStream fis = new FileInputStream("test.json");
			DataMap j = new DataMap(fis);
			System.out.println(j.toString());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
