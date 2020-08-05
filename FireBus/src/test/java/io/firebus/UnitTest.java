package io.firebus;
	
import java.io.FileInputStream;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.utils.DataMap;
import io.firebus.utils.FirebusDataUtil;
import jdk.nashorn.api.scripting.JSObject;


public class UnitTest
{
	public static void main(String args[])
	{
		try
		{
			Firebus firebus = new Firebus();
			firebus.registerServiceProvider("test", new ServiceProvider() {
				public Payload service(Payload payload) throws FunctionErrorException {
					return new Payload("allo");
				}

				public ServiceInformation getServiceInformation() {
					return null;
				}
				
			}, 10);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
