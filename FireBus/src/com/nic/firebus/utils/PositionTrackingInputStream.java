package com.nic.firebus.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PositionTrackingInputStream extends BufferedInputStream
{
	protected int col;
	protected int line;
	protected boolean cornl;
	protected int lastVal;
	
	public PositionTrackingInputStream(InputStream is)
	{
		super(is);
		col = 1;
		line = 1;
		cornl = false;
	}

	public int read() throws IOException
	{
		int i = super.read();
		char c = (char)i;
		if(c == '\r' || c == '\n')
		{
			if(cornl == false)
			{
				line++;
				col = 0;
				cornl = true;
			}
			else
			{
				cornl = false;
			}
		}
		else
		{
			cornl = false;
			col++;
		}
		lastVal = i;
		return i;
	}
	
	public int getColumn()
	{
		return col;
	}
	
	public int getLine()
	{
		return line;
	}
}
