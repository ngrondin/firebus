import java.io.FileInputStream;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jdk.nashorn.api.scripting.JSObject;

import com.nic.firebus.utils.FirebusDataUtil;
import com.nic.firebus.utils.FirebusJSONUtils;
import com.nic.firebus.utils.JSONObject;


public class UnitTest
{
	public static void main(String args[])
	{
		try
		{
			JSONObject obj = new JSONObject(new FileInputStream("test.json"));
			ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("javascript");
			CompiledScript script = ((Compilable)jsEngine).compile("var json = {first:'allo', second:1.2, third:null, fourth:{fifth:'toi'}, sixth:['a', 'b', 'c'], seventh:true, eigth:(new Date())}");
			Bindings context = jsEngine.createBindings();
			script.eval(context);
			JSObject json = (JSObject)context.get("json");
			JSONObject dataObj = FirebusDataUtil.convertJSObjectToDataObject(json);
			//String jsonStr = FirebusJSONUtils.stringify(json);
			System.out.println(dataObj);
			JSObject jso = FirebusDataUtil.convertDataObjectToJSObject(dataObj);
			System.out.println(FirebusDataUtil.convertJSObjectToDataObject(jso));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
