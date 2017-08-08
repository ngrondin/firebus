import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.nic.firebus.Node;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.interfaces.ServiceRequestor;


public class TestNode
{
	public static void main(String[] args)
	{
		Logger log = LogManager.getLogManager().getLogger("");
		log.setLevel(Level.FINE);
		for (Handler h : log.getHandlers()) 
		    h.setLevel(Level.INFO);

		Node n = new Node();
		
		if(args.length > 0)
		{
			if(args[0].equals("requestor"))
			{
				if(args.length > 1)
				{
					boolean quit = false;
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					while(!quit)
					{
						try
						{
							String in = br.readLine();
							if(in.equals("-si"))
							{
								ServiceInformation si = n.getServiceInformation(args[1]);
								if(si != null)
									System.out.println(si.toLongString());
							}
							else if(in.startsWith("-a "))
							{
								n.requestService(args[1], in.substring(3).getBytes(), 10000, new ServiceRequestor() {
									public void requestCallback(byte[] payload) {
										System.out.println(new String(payload));
									}
									public void requestTimeout() {
										System.out.println("Timed out");
									}});
							}
							else
							{
								byte[] ret = n.requestService(args[1], in.getBytes(), 10000);
								if(ret != null)
									System.out.println(new String(ret));
							}
						} 
						catch (IOException e) {}
					}
				}
			}
			
			if(args[0].equals("provider"))
			{
				if(args.length > 2)
				{
					final String prefix = args[2];
					ServiceInformation si = new ServiceInformation(args[1], "text/plain", "{request:String}", "text/plain", "{response:String}");
					n.registerServiceProvider(si, new ServiceProvider() {
						public byte[] requestService(byte[] payload)
						{
							System.out.println("Providing Service");
							//try{ Thread.sleep(3000); } catch(Exception e) {}
							return (prefix + " " + new String(payload)).getBytes();
						}
					}, 2);
					System.out.println("Service Provider Registered");
				}
			}
			
			if(args[0].equals("publisher"))
			{
				if(args.length > 1)
				{
					boolean quit = false;
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					while(!quit)
					{
						try
						{
							String in = br.readLine();
							n.publish(args[1], in.getBytes());
						} 
						catch (IOException e) {}
					}
				}
			}
			
			if(args[0].equals("consumer"))
			{
				if(args.length > 1)
				{
					n.registerConsumer(new ConsumerInformation(args[1]), new Consumer(){
						public void consume(byte[] payload)
						{
							System.out.println(new String(payload));
						}
					}, 10);
					System.out.println("Consumer Registered");
				}
			}
		}
	}
}
