package io.firebus.adapters.jdbc;

import java.util.ArrayList;
import java.util.List;

import io.firebus.utils.DataEntity;
import io.firebus.utils.DataLiteral;

public class StatementBuilder {
	public StringBuilder statement;
	public List<DataEntity> params;
	
	public StatementBuilder()
	{
		statement = new StringBuilder();
		params = new ArrayList<DataEntity>();
	}
	
	public StatementBuilder(String s)
	{
		statement = new StringBuilder();
		params = new ArrayList<DataEntity>();
		statement.append(s);
	}
	
	public void append(String s)
	{
		statement.append(s);
	}
	
	public void append(DataEntity d)
	{
		statement.append("?");
		params.add(d);
	}
	
	public void append(StatementBuilder b)
	{
		statement.append(b.toString());
		params.addAll(b.getParams());
	}
	
	public String toString()
	{
		return statement.toString();
	}
	
	public List<DataEntity> getParams()
	{
		return params;
	}
	
	public int getLength()
	{
		return statement.length();
	}
	
}
