package com.nic.firebus.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.commons.dbcp.BasicDataSource;

import com.nic.firebus.Payload;
import com.nic.firebus.exceptions.FunctionErrorException;
import com.nic.firebus.information.ServiceInformation;
import com.nic.firebus.interfaces.Consumer;
import com.nic.firebus.interfaces.ServiceProvider;
import com.nic.firebus.utils.DataEntity;
import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataList;
import com.nic.firebus.utils.DataLiteral;
import com.nic.firebus.utils.DataMap;

public class JDBCAdapter extends Adapter  implements ServiceProvider, Consumer
{
	protected String connStr;
	protected BasicDataSource dataSource;
	protected int pageSize;
	
	public JDBCAdapter(DataMap c) throws SQLException
	{
		super(c);
		pageSize = config.containsKey("pagesize") ? config.getNumber("pagesize").intValue() : 50;
		connectJDBC();
	}
	
	protected void connectJDBC() throws SQLException
	{
		dataSource = new BasicDataSource();
		dataSource.setDriverClassName(config.getString("driver"));
		dataSource.setUrl(config.getString("url"));
		dataSource.setUsername(config.getString("username"));
		dataSource.setPassword(config.getString("password"));
		dataSource.setMaxActive(10);
		dataSource.setMaxIdle(5);
		dataSource.setInitialSize(5);
		dataSource.setValidationQuery("SELECT 1");
		Connection c = dataSource.getConnection();
		c.close();
	}

	public void consume(Payload payload)  
	{
		try
		{
			DataMap packet = new DataMap(payload.getString());
			upsert(packet);
		}
		catch(DataException e)
		{
			e.printStackTrace();
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		try
		{
			DataMap packet = new DataMap(payload.getString());
			DataList list = get(packet);
			DataMap result = new DataMap();
			result.put("result", list);
			return new Payload(null, result.toString().getBytes());
		}
		catch(DataException e)
		{
			throw new FunctionErrorException("Error getting the data", e);
		}
	}

	public ServiceInformation getServiceInformation()
	{
		return new ServiceInformation("text/plain", "", "text/json", "{}");
	}

	private DataList get(DataMap packet) throws FunctionErrorException
	{
		DataList list = new DataList();
		String objectName = packet.getString("object");
		String operation = packet.getString("operation");
		DataMap filter = packet.getObject("filter");
		String where = getWhere(filter);
		if(where != null && (operation == null || operation.equals("get")))
		{
			Connection conn = null;
	        PreparedStatement ps1 = null;
	        ResultSet rs1 = null;
			try
			{
				String select = "select top " + pageSize + " * from " + objectName + " where " + where;
				conn = dataSource.getConnection();
		        ps1 = conn.prepareStatement(select);
		        rs1 = ps1.executeQuery();
		        ResultSetMetaData rsmd = rs1.getMetaData();
		        int colCnt = rsmd.getColumnCount();
		        while(rs1.next()  &&  list.size() < pageSize)
		        {
		        	DataMap map = new DataMap();
		        	for(int i = 1; i <= colCnt; i++)
		        		map.put(rsmd.getColumnName(i), rs1.getObject(i));
		        	list.add(map);
		        }
			}
			catch(Exception e)
			{
				throw new FunctionErrorException("Error querying the database", e);
			}
			finally
			{
				if(rs1 != null) try {rs1.close();} catch(Exception e) {}
				if(ps1 != null) try {ps1.close();} catch(Exception e) {}
				if(conn != null) try {conn.close();} catch(Exception e) {}
			}
		}
		return list;
	}
	
	private void upsert(DataMap packet)
	{
		String objectName = packet.getString("object");
		String operation = packet.getString("operation");
		DataMap filter = packet.getObject("filter");
		DataMap data = packet.getObject("data");
		String where = getWhere(filter);
		if(where != null && (operation == null || (operation != null && (operation.equals("update") || operation.equals("insert") || operation.equals("upsert")))))
		{
			Connection conn = null;
	        PreparedStatement ps1 = null;
	        PreparedStatement ps2 = null;
	        ResultSet rs1 = null;
			try
			{
				String select = "select count(*) from " + objectName + " where " + where;
				conn = dataSource.getConnection();
		        ps1 = conn.prepareStatement(select);
		        rs1 = ps1.executeQuery();
		        rs1.next();
		        int count = rs1.getInt(1);
		        String sql = "";
		        if(count > 0)
		        {
		        	String update = "update " + objectName + " set ";
		        	Iterator<String> it2 = data.keySet().iterator();
		        	while(it2.hasNext())
		        	{
		        		String key = it2.next();
		        		update += key + " = " + getSQLStringFromObject(data.get(key));
		        		if(it2.hasNext())
		        			update += ", ";
		        	}
		        	update += " where " + where;
		        	sql = update;
		        }
		        else
		        {
		        	String insert = "insert into  " + objectName + " (";
		        	String values = ") values (";
		        	data.merge(filter);
		        	Iterator<String> it2 = data.keySet().iterator();
		        	while(it2.hasNext())
		        	{
		        		String key = it2.next();
		        		insert += key;
		        		values += getSQLStringFromObject(data.get(key));
		        		if(it2.hasNext())
		        		{
		        			insert += ", ";
		        			values += ", ";
		        		}
		        	}
		        	sql = insert + values + ")";
		        }
		        ps2 = conn.prepareStatement(sql);
		        ps2.execute();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(rs1 != null) try {rs1.close();} catch(Exception e) {}
				if(ps1 != null) try {ps1.close();} catch(Exception e) {}
				if(ps2 != null) try {ps2.close();} catch(Exception e) {}
				if(conn != null) try {conn.close();} catch(Exception e) {}
			}
		}
	}
	
	private String getWhere(DataMap filter)
	{
		String where = null;
		if(filter != null)
		{
			Iterator<String> it = filter.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				if(where == null)
					where = "";
				else
					where += " and ";
				if(filter.get(key) instanceof DataMap)
				{
					DataMap map = filter.getObject(key);
					if(map.containsKey("$in"))
					{
						DataList list = map.getList("$in");
						where += key + " in (";
						for(int i = 0 ; i < list.size(); i++)
						{
							if(i > 0)
								where += ", ";
							where += getSQLStringFromObject(list.get(i));
						}
						where += ")";
					}
				}
				else
				{
					where += key + " = " + getSQLStringFromObject(filter.get(key)) + "";
				}
			}
		}
		
		if(where == null)
			where = "1=1";
		return where;
	}
	
	private String getSQLStringFromObject(DataEntity val)
	{
		if(val == null)
		{
			return "null";
		}
		else if(val instanceof DataLiteral)
		{
			DataLiteral valLit = (DataLiteral)val;
			if(valLit.getType() == DataLiteral.TYPE_BOOLEAN)
			{
				if(valLit.getBoolean() == true)
					return "true";
				else
					return "false";
			}
			else if(valLit.getType() == DataLiteral.TYPE_NUMBER)
			{
				return valLit.getNumber().toString();
			}
			else if(valLit.getType() == DataLiteral.TYPE_STRING)
			{
				if(valLit.getString().length() == 0)
					return "null";
				else
					return "'" + valLit.getString().replaceAll("'", "''") + "'";
			}
			else
			{
				return "'" + val.toString() + "'";
			}
		} 
		else 
		{
			return "'" + val.toString().replaceAll("'", "''") + "'";
		}
	}
}
