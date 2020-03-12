package io.firebus;

public class FunctionWorker extends Thread
{
	//private Logger logger = Logger.getLogger("io.firebus");
	protected FunctionEntry functionEntry;
	protected Message inboundMessage;
	protected NodeCore nodeCore;
	
	public FunctionWorker(FunctionEntry fe, Message im, NodeCore nc)
	{
		functionEntry = fe;
		inboundMessage = im;
		nodeCore = nc;
		setName("fbWorker" + getId());
		functionEntry.runStarted();
		start();
	}
	
	public void run()
	{

	}
}
