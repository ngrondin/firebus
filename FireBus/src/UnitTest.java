import com.nic.firebus.NodeCore;


public class UnitTest
{
	public static void main(String args[])
	{
		NodeCore n1 = new NodeCore();
		try{ Thread.sleep(5000);} catch(Exception e) {}
		NodeCore n2 = new NodeCore();
		
		try{ Thread.sleep(1000);} catch(Exception e) {}
		
		System.out.println(n1);
		System.out.println("\r\n\r\n");
		System.out.println(n2);
	}
}
