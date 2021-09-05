package io.firebus.adapters.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.firebus.data.DataEntity;
import io.firebus.data.DataLiteral;

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
	
	public String getParameterizedStatement() 
	{
		return statement.toString();
	}
	
	public String getNonParameterizedStatement() 
	{
		String str = statement.toString();
		for(int i = 0; i < params.size(); i++)
		{
			String sub = "";
			DataEntity val = params.get(i);
			if(val == null)
			{
				sub = "null";
			}
			else if(val instanceof DataLiteral)
			{
				DataLiteral valLit = (DataLiteral)val;
				if(valLit.getType() == DataLiteral.TYPE_BOOLEAN || valLit.getType() == DataLiteral.TYPE_NUMBER)
				{
					sub = valLit.getString();
				}
				else if(valLit.getType() == DataLiteral.TYPE_NULL)
				{
					sub = "null";
				}
				else
				{
					sub = "'" + valLit.getString().replaceAll("'", "''") + "'";
				}
			}
			str = str.replaceFirst("\\?", sub);
		}
		return str;
	}
	
	public void setStatementParams(PreparedStatement ps) throws SQLException
	{
		for(int i = 1; i <= params.size(); i++)
		{
			DataEntity val = params.get(i - 1);
			if(val == null)
			{
				ps.setNull(i, java.sql.Types.NULL);
			}
			else if(val instanceof DataLiteral)
			{
				DataLiteral valLit = (DataLiteral)val;
				if(valLit.getType() == DataLiteral.TYPE_BOOLEAN)
				{
					ps.setBoolean(i, valLit.getBoolean());
				}
				else if(valLit.getType() == DataLiteral.TYPE_NUMBER)
				{
					if(valLit.getObject() instanceof Integer)
						ps.setInt(i, valLit.getNumber().intValue());
					else
						ps.setDouble(i, valLit.getNumber().doubleValue());
				}
				else if(valLit.getType() == DataLiteral.TYPE_STRING)
				{
					ps.setString(i, valLit.getString());
				}
				else if(valLit.getType() == DataLiteral.TYPE_DATE)
				{
					ps.setTimestamp(i, new java.sql.Timestamp(valLit.getDate().getTime()));
				}
				else if(valLit.getType() == DataLiteral.TYPE_NULL)
				{
					ps.setNull(i, java.sql.Types.NULL);
				}
				else
				{
					ps.setString(i, valLit.getString());
				}
			} 
			else 
			{
				ps.setString(i, val.toString());
			}			
		}
	}
	
}
