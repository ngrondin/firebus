package io.firebus.adapters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.apache.commons.dbcp.BasicDataSource;

import io.firebus.Payload;
import io.firebus.adapters.jdbc.StatementBuilder;
import io.firebus.exceptions.FunctionErrorException;
import io.firebus.information.ServiceInformation;
import io.firebus.interfaces.Consumer;
import io.firebus.interfaces.ServiceProvider;
import io.firebus.logging.Logger;
import io.firebus.data.DataException;
import io.firebus.data.DataList;
import io.firebus.data.DataLiteral;
import io.firebus.data.DataMap;

public class JDBCAdapter extends Adapter  implements ServiceProvider, Consumer
{
	protected String connStr;
	protected BasicDataSource dataSource;
	protected SimpleDateFormat sdf;
	
	public JDBCAdapter(DataMap c) throws SQLException
	{
		super(c);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
			if(dataSource == null || (dataSource != null && dataSource.isClosed()))
				connectJDBC();

			DataMap packet = new DataMap(payload.getString());
			upsert(packet);
		}
		catch(DataException e)
		{
			e.printStackTrace();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Payload service(Payload payload) throws FunctionErrorException
	{
		try
		{
			if(dataSource == null || (dataSource != null && dataSource.isClosed()))
				connectJDBC();

			DataMap packet = new DataMap(payload.getString());
			DataMap result = new DataMap();
			if(packet.containsKey("filter")) 
			{
				DataList list = get(packet);
				result.put("result", list);
			}
			else if(packet.containsKey("key"))
			{
				upsert(packet);
				result.put("result", "ok");
			}
			return new Payload(null, result.toString().getBytes());
		}
		catch(DataException e)
		{
			throw new FunctionErrorException("Error getting the data", e);
		}
		catch(SQLException e)
		{
			throw new FunctionErrorException("Error connecting to the data source", e);
		}
	}

	public ServiceInformation getServiceInformation()
	{
		return null;//new ServiceInformation("text/plain", "", "text/json", "{}");
	}

	private DataList get(DataMap packet) throws FunctionErrorException
	{
		//String operation = packet.getString("operation");
		String objectName = packet.getString("object");
		DataMap filter = packet.getObject("filter");
		int page = packet.containsKey("page") ? packet.getNumber("page").intValue() : 0;
		int pageSize = config.containsKey("pagesize") ? config.getNumber("pagesize").intValue() : 50;
		
		
		StatementBuilder select = new StatementBuilder("select top ");
		select.append(Integer.toString((page + 1) * pageSize));
		select.append(" ");

		StatementBuilder tuple = new StatementBuilder();
		if(packet.containsKey("tuple"))
		{
			for(int i = 0; i < packet.getList("tuple").size(); i++)
			{
				if(i > 0)
					tuple.append(", ");
				tuple.append(packet.getList("tuple").getString(i));
			}
		}

		StatementBuilder columns = new StatementBuilder();
		if(packet.containsKey("metrics"))
		{
			for(int i = 0; i < packet.getList("metrics").size(); i++)
			{
				DataMap metric = packet.getList("metrics").getObject(i);
				if(i > 0)
					columns.append(", ");
				columns.append(metric.getString("function"));
				columns.append("(");
				if(!metric.getString("function").equalsIgnoreCase("count")) 
					columns.append(metric.getString("field"));
				else
					columns.append("*");
				columns.append(") as ");
				columns.append(metric.getString("name"));
			}
		}
		else
		{
			columns.append("*");
		}
		
		if(tuple.getLength() > 0)
		{
			select.append(tuple);
			select.append(", ");
		}
		
		select.append(columns);
		select.append(" from ");
		select.append(objectName);
		select.append(" where ");
		select.append(getWhere(filter));

		if(tuple.getLength() > 0)
		{
			select.append(" group by ");
			select.append(tuple);
		}
		
		if(packet.containsKey("sort"))
		{
			DataMap sort = null;
			int i = 0;

			while ((sort = packet.getObject("sort").getObject("" + i++)) != null ) {
				select.append(i == 1 ? " order by " : ", ");
				select.append(sort.getString("attribute"));
				select.append(sort.getNumber("dir").intValue() == -1 ? " desc" : " asc");
			}
		}
		
		DataList list = new DataList();
		Connection conn = null;
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        long qt = 0;
        long start = System.currentTimeMillis();
		try
		{
			conn = dataSource.getConnection();
	        ps1 = conn.prepareStatement(select.getParameterizedStatement());
	        select.setStatementParams(ps1);
	        rs1 = ps1.executeQuery();
	        ResultSetMetaData rsmd = rs1.getMetaData();
	        int colCnt = rsmd.getColumnCount();
	        for(int i = 0; i < (page * pageSize); i++, rs1.next());
	        while(rs1.next()  &&  list.size() < pageSize)
	        {
	        	DataMap map = new DataMap();
	        	for(int i = 1; i <= colCnt; i++)
	        	{
	        		String colName = rsmd.getColumnName(i);
	        		Object val = rs1.getObject(i);
	        		if(val instanceof String)
	        		{
	        			String valStr = (String)val;
	        			if(valStr.startsWith("{") && valStr.endsWith("}"))
	        				try { val = new DataMap(valStr); } catch(DataException e) {}		        				
	        			if(valStr.startsWith("[") && valStr.endsWith("]"))
	        				try { val = new DataList(valStr); } catch(DataException e) {}		        				
	        		}
	        		map.put(colName, val);
	        	}
	        	list.add(map);
	        }
	        qt = System.currentTimeMillis() - start;
	        Logger.finer("fb.adapter.jdbc.resp", new DataMap("ms", qt, "stmt" ,select.getNonParameterizedStatement(), "rows", list.size()));
		}
		catch(Exception e)
		{
			Logger.severe("fb.adapter.jdbc.request", new DataMap( "stmt" ,select.getNonParameterizedStatement()), e);
			throw new FunctionErrorException("Error querying the database", e);
		}
		finally
		{
			if(rs1 != null) try {rs1.close();} catch(Exception e) {}
			if(ps1 != null) try {ps1.close();} catch(Exception e) {}
			if(conn != null) try {conn.close();} catch(Exception e) {}
		}
		return list;
	}
	
	private void upsert(DataMap packet)
	{
		String objectName = packet.getString("object");
		String operation = packet.getString("operation");
		DataMap filter = packet.getObject("key");
		DataMap data = packet.getObject("data");
		
		StatementBuilder where = getWhere(filter);
		StatementBuilder sql = null;
		if(where != null && (operation == null || (operation != null && (operation.equals("update") || operation.equals("insert") || operation.equals("upsert")))))
		{
			Connection conn = null;
	        PreparedStatement ps1 = null;
	        PreparedStatement ps2 = null;
	        ResultSet rs1 = null;
			try
			{
				StatementBuilder select = new StatementBuilder();
				select.append("select count(*) from ");
				select.append(objectName);
				select.append(" where ");
				select.append(where);
		        long qt = 0;
		        long start = System.currentTimeMillis();
				conn = dataSource.getConnection();
		        ps1 = conn.prepareStatement(select.getParameterizedStatement());
		        select.setStatementParams(ps1);
		        rs1 = ps1.executeQuery();
		        rs1.next();
		        int count = rs1.getInt(1);
		        qt = System.currentTimeMillis() - start;
		        Logger.finer("fb.adapter.jdbc.upsert", new DataMap("ms", qt, "stmt", select.getNonParameterizedStatement()));
		        if(count > 0)
		        {
		        	StatementBuilder update = new StatementBuilder();
		        	update.append("update ");
		        	update.append(objectName);
		        	update.append(" set ");
		        	Iterator<String> it2 = data.keySet().iterator();
		        	while(it2.hasNext())
		        	{
		        		String key = it2.next();
		        		update.append(key);
		        		update.append(" = ");
		        		update.append(data.get(key));
		        		if(it2.hasNext())
		        			update.append(", ");
		        	}
		        	update.append(" where ");
		        	update.append(where);
		        	sql = update;
		        }
		        else
		        {
		        	StatementBuilder insert = new StatementBuilder();
		        	insert.append("insert into  ");
		        	insert.append(objectName);
		        	insert.append(" (");
		        	StatementBuilder values = new StatementBuilder();
		        	values.append(") values (");
		        	data.merge(filter);
		        	Iterator<String> it2 = data.keySet().iterator();
		        	while(it2.hasNext())
		        	{
		        		String key = it2.next();
		        		insert.append(key);
		        		values.append(data.get(key));
		        		if(it2.hasNext())
		        		{
		        			insert.append(", ");
		        			values.append(", ");
		        		}
		        	}
		        	values.append(")");
		        	insert.append(values);
		        	sql = insert;
		        }
		        ps2 = conn.prepareStatement(sql.getParameterizedStatement());
				sql.setStatementParams(ps2);
		        long et = 0;
		        start = System.currentTimeMillis();
		        ps2.execute();
		        et = System.currentTimeMillis() - start;
		        Logger.finer("fb.adapter.jdbc.resp", new DataMap("ms", et, "stmt", sql.getNonParameterizedStatement()));
			}
			catch(Exception e)
			{
				Logger.severe("fb.adapter.jdbc.upsert", new DataMap("stmt", sql.getNonParameterizedStatement()), e);
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
	
	private StatementBuilder getWhere(DataMap filter)
	{
		StatementBuilder where = null;
		if(filter != null)
		{
			Iterator<String> it = filter.keySet().iterator();
			while(it.hasNext())
			{
				String key = it.next();
				if(where == null)
					where = new StatementBuilder();
				else
					where.append(" and ");
				if(key.equals("$or"))
				{
					DataList orList = filter.getList(key);
					where.append("(");
					for(int i = 0; i < orList.size(); i++)
					{
						if(i > 0)
							where.append(" or ");
						where.append(getWhere(orList.getObject(i)));
					}
					where.append(")");
				}
				else if(filter.get(key) instanceof DataMap)
				{
					DataMap map = filter.getObject(key);
					if(map.containsKey("$in"))
					{
						DataList list = map.getList("$in");
						if(list.size() > 0) {
							where.append(key);
							where.append(" in (");
							for(int i = 0 ; i < list.size(); i++)
							{
								if(i > 0)
									where.append(", ");
								where.append(list.get(i));
							}
							where.append(")");
						} else {
							where.append("0=1");
						}
					}
					else if(map.containsKey("$regex"))
					{
						where.append(key);
						where.append(" like ");
						String expr = map.getString("$regex");
						if(expr.contains("(?i)")) 
							expr = "%" + expr.replace("(?i)", "") + "%";
						where.append(new DataLiteral(expr));
					}
					else if(map.containsKey("$gt"))
					{
						where.append(key);
						where.append(" > ");
						where.append(map.get("$gt"));
					}
					else if(map.containsKey("$lt"))
					{
						where.append(key);
						where.append(" < ");
						where.append(map.get("$tt"));
					}
				}
				else
				{
					where.append(key);
					where.append(" = ");
					where.append(filter.get(key));
					where.append("");
				}
			}
		}
		
		if(where == null)
		{
			where = new StatementBuilder();
			where.append("1=1");
		}
		return where;
	}
	
	/*
	public void setStatementParams(PreparedStatement ps, StatementBuilder sb) throws SQLException
	{
		List<DataEntity> params = sb.getParams();
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
	*/
}
