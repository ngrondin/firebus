package com.nic.firebus.adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.nic.firebus.Node;
import com.nic.firebus.Payload;
import com.nic.firebus.information.ConsumerInformation;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;

public class JDBCAdaptor implements ServiceProvider, Consumer
{
	protected Node node;
	protected String connStr;
	protected Connection connection;
	
	public JDBCAdaptor(String fbn, String fbpw, String sn, String cs) throws SQLException
	{
		connStr = cs;
		node = new Node();
		node.registerConsumer(new ConsumerInformation(sn), this, 10);
		node.registerServiceProvider(new ServiceInformation(sn), this, 10);
		connectJDBC();
	}
	
	protected void connectJDBC() throws SQLException
	{
		if(connection != null)
		{
			connection.close();
			connection = null;
		}
        connection = DriverManager.getConnection(connStr);
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

	public Payload service(Payload payload) 
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

	
	public static void main(String[] args)
	{
		if(args.length >= 4)
		{
			String firebusNetwork = args[0];
			String firebusPassword = args[1];
			String subjectName = args[2];
			String jdbcString = args[3];
			try 
			{
				new JDBCAdaptor(firebusNetwork, firebusPassword, subjectName, jdbcString);
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}			
		}
	}
}
