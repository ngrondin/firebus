import java.io.FileInputStream;

import com.nic.firebus.utils.JSONObject;


public class UnitTest
{
	public static void main(String args[])
	{
		try
		{
			JSONObject obj = new JSONObject(new FileInputStream("test.json"));
			System.out.println(obj);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
