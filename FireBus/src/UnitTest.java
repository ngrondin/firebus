import com.nic.firebus.Node;


public class UnitTest
{
	public static void main(String args[])
	{
		Node n1 = new Node();
		try{ Thread.sleep(5000);} catch(Exception e) {}
		Node n2 = new Node();
		
		try{ Thread.sleep(1000);} catch(Exception e) {}
		
		System.out.println(n1);
		System.out.println("\r\n\r\n");
		System.out.println(n2);
	}
}
