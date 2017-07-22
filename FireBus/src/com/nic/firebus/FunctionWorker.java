package com.nic.firebus;

public class FunctionWorker extends Thread
{
	protected BusFunction busFunction;
	protected byte[] payload;
	protected FunctionListener functionListener;
	protected int correlation;
	
	public FunctionWorker(BusFunction f, byte[] pl)
	{
		busFunction = f;
		payload = pl;
		start();
	}
	
	public FunctionWorker(BusFunction f, byte[] pl, FunctionListener fl, int c)
	{
		busFunction = f;
		payload = pl;
		functionListener = fl;
		correlation = c;
		start();
	}
	
	public void run()
	{
		if(busFunction instanceof ServiceProvider)
		{
			byte[] returnPayload = ((ServiceProvider)busFunction).requestService(payload);
			if(functionListener != null)
				functionListener.functionCallback(correlation, returnPayload);
		}
		else if(busFunction instanceof Consumer)
		{
			((Consumer)busFunction).consume(payload);
		}
		
	}

}
