package com.nic.firebus.adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.nic.firebus.Node;
import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.JSONObject;

public class JDBCAdapter extends Adapter  implements ServiceProvider, Consumer
{
	protected String connStr;
	protected Connection connection;
	
	public JDBCAdapter(Node n, JSONObject c) throws SQLException
	{
		super(n, c);
		connectJDBC();
	}
	
	protected void connectJDBC() throws SQLException
	{
		if(connection != null)
		{
			connection.close();
			connection = null;
		}
        connection = DriverManager.getConnection(config.getString("connectionstring"));
	}

	public void consume(Payload payload) 
	{
		String sqlStr = new String(payload.data);
		try
		{
	        PreparedStatement stmt = connection.prepareStatement(sqlStr);
	        stmt.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		StringBuilder sb = new StringBuilder();
		String sqlStr = new String(payload.data);
		try
		{
	        PreparedStatement stmt = connection.prepareStatement(sqlStr);
	        ResultSet rs = stmt.executeQuery();
	        ResultSetMetaData rsmd = rs.getMetaData();
	        int colCnt = rsmd.getColumnCount();
	        int l = 0;
	        
	        sb.append("[\r\n");
	        while(rs.next())
	        {
	        	if(l++ > 0)
	        		sb.append(",\r\n");
		        sb.append("{");
	        	for(int i = 1; i <= colCnt; i++)
	        	{
	        		if(i > 1)
	        			sb.append(",\r\n");
	        		sb.append("\"" + rsmd.getColumnName(i) + "\" : \"" + rs.getString(i) + "\"");
	        	}
		        sb.append("}");
	        }
	        sb.append("\r\n]");
		}
		catch(Exception e)
		{
			sb.append(e.getMessage());
		}
		return new Payload(null, sb.toString().getBytes());
	}

	
}
