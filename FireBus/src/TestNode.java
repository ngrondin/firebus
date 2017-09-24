import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nic.firebus.Firebus;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.interfaces.ServiceRequestor;
import com.nic.firebus.logging.FirebusSimpleFormatter;


public class TestNode
{
	public static void main(String[] args)
	{
		Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
		try
		{
			FileHandler fh = new FileHandler("TestNode.log");
			fh.setFormatter(new FirebusSimpleFormatter());
			fh.setLevel(Level.FINEST);
			Logger logger = Logger.getLogger("com.nic.firebus");
			logger.addHandler(fh);
			logger.setLevel(Level.FINEST);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		Firebus n = new Firebus();
		
		if(args.length > 0)
		{
			if(args[0].equals("console"))
			{
				if(args.length > 1)
				{
					String functionName = args[1];
					boolean quit = false;
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					while(!quit)
					{
						try
						{
							String in = br.readLine();
							String[] parts = in.split(" ");
							if(parts[0].equals("info"))
							{
								ServiceInformation si = n.getServiceInformation(functionName);
								if(si != null)
									System.out.println(si.toString());
							}
							else if(parts[0].equals("req"))
							{
								String line = in.substring(parts[0].length() + 1);
								n.requestService(functionName, new Payload(null, line.getBytes()), 10000, new ServiceRequestor() {
									public void requestCallback(Payload payload) {
										System.out.println(new String(payload.data));
									}
									public void requestTimeout() {
										System.out.println("Timed out");
									}
									public void requestErrorCallback(FunctionErrorException e) {
										System.out.println("Error: " + e.getMessage());
									}});
							}
							else if(parts[0].equals("reqs"))
							{
								String line = in.substring(parts[0].length() + 1);
								try
								{
									String resp = new String(n.requestService(functionName, new Payload(null, line.getBytes()), 2000).data);
									System.out.println(resp);
								}
								catch (FunctionErrorException e)
								{
									e.printStackTrace();
								}
							}
							else if(parts[0].equals("pub"))
							{
								String line = in.substring(parts[0].length());
								n.publish(functionName, new Payload(line.getBytes()));
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
					n.registerServiceProvider(args[1], new ServiceProvider() {
						public Payload service(Payload payload) throws FunctionErrorException
						{
							System.out.println("Providing Service");
							//try{ Thread.sleep(3000); } catch(Exception e) {}
							String val = new String(payload.data);
							if(val.equals("throw"))
								throw new FunctionErrorException("this is my error");
							else
								return new Payload(null,  (prefix + " " + new String(payload.data)).getBytes());
						}

						public ServiceInformation getServiceInformation()
						{
							return  new ServiceInformation("text/plain", "{request:String}", "text/plain", "{response:String}");
						}
					}, 2);
					System.out.println("Service Provider Registered");
				}
			}

			
			if(args[0].equals("consumer"))
			{
				if(args.length > 1)
				{
					n.registerConsumer(args[1], new Consumer(){
						public void consume(Payload payload)
						{
							System.out.println(new String(payload.data));
						}
					}, 10);
					System.out.println("Consumer Registered");
				}
			}
		}
	}
}
