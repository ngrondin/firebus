import com.nic.firebus.Node;
import com.nic.firebus.ServiceProvider;


public class TestNode1
{
	public static void main(String[] args)
	{
		Node n1 = new Node(1991);
		//n1.addKnownNodeAddress("127.0.0.1", 1992);
		/*
		n1.registerServiceProvider("testservice", new ServiceProvider(){
			public byte[] requestService(byte[] payload)
			{
				return new String("allo").getBytes();
			}});
		*/
		
		Node n2 = new Node(1992);
		//n2.addKnownNodeAddress("127.0.0.1", 1991);
		//try{Thread.sleep(1000);} catch(Exception e) {}
		//byte[] ret = n2.requestService("testservice", new byte[]{0, 1, 2});
		//System.out.print(new String(ret));
		System.out.println("\r\n\r\n");
		System.out.println("Node 1");
		System.out.println(n1);
		System.out.println("Node 2");
		System.out.println(n2);
	}
}
