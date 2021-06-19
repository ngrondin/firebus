package io.firebus.information;

public abstract class FunctionInformation
{
	protected NodeInformation nodeInformation;
	protected String name;
	protected boolean fullInformation;
	protected int rating;

	public FunctionInformation(String n)
	{
		name = n;
		fullInformation = false;
		rating = 100;
	}
	
	public FunctionInformation(NodeInformation ni, String n)
	{
		nodeInformation = ni;
		name = n;
		fullInformation = false;
		rating = 100;
	}
	
	public abstract byte[] serialise();
	
	public abstract void deserialise(byte[] bytes);

	public boolean hasFullInformation()
	{
		return fullInformation;
	}
	
	public synchronized void didNotRespond()
	{
		if(nodeInformation != null)
			nodeInformation.didNotRespond();
	}
	
	public synchronized void wasUnavailable()
	{
		rating--;
		if(rating < 0) rating = 0;
		if(nodeInformation != null)
			nodeInformation.responded();
	}
	
	public synchronized void timedOutWhileExecuting()
	{
		rating--;
		if(rating < 0) rating = 0;
	}
	
	public synchronized void returnedError()
	{
		
	}
	
	public synchronized void returnedProgress()
	{
		if(nodeInformation != null)
			nodeInformation.responded();
	}
	
	public synchronized void wasSuccesful()
	{ 
		rating++;
		if(rating > 100) rating = 100;
		if(nodeInformation != null)
			nodeInformation.responded();
	}
	
	public boolean shouldRemove()
	{
		return rating == 0;
	}
	
	public int getRating()
	{
		return rating;
	}
		
	public int getCombinedRating()
	{
		return (nodeInformation != null ? nodeInformation.getRating() : 1) * rating;
	}

	public int getNodeId()
	{
		return nodeInformation != null ? nodeInformation.getNodeId() : null;
	}
	
	public abstract String toString();
}
