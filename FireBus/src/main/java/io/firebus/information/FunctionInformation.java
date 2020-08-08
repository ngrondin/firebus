package io.firebus.information;

public abstract class FunctionInformation
{
	protected boolean fullInformation;
	protected int rating;

	public abstract byte[] serialise();
	
	public abstract void deserialise(byte[] bytes);

	public int getRating()
	{
		return rating;
	}
	
	public boolean hasFullInformation()
	{
		return fullInformation;
	}
	
	public void reduceRating(int i)
	{
		rating -= i;
	}
	
	public void resetRating()
	{
		rating = 0;
	}
	
	public abstract String toString();
}
