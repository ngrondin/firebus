import java.net.InetAddress;
import java.net.UnknownHostException;

import com.nic.firebus.Node;
import com.nic.firebus.ServiceProvider;


public class TestNode1
{
	public static void main(String[] args)
	{
		Node n1 = new Node(1991);
		n1.registerServiceProvider("testservice", new ServiceProvider(){
			public byte[] requestService(byte[] payload)
			{
				return new String("allo").getBytes();
			}});
		
		
		Node n2 = new Node(1992);
		try
		{
			n2.addKnownNodeAddress(InetAddress.getByName("127.0.0.1"), 1991);

		} 
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
}
