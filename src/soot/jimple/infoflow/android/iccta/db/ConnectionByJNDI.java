package soot.jimple.infoflow.android.iccta.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class ConnectionByJNDI implements IConnectionProvider 
{

	private DataSource ds;

	public DataSource getDataSource(String dbName) throws Exception
	{
		if(ds == null)
		{
			InitialContext context = new InitialContext();
			ds = (DataSource)context.lookup("java:comp/env/jdbc/" + dbName);
		}
		
		return ds;
	}

	@SuppressWarnings("finally")
	@Override
	public Connection getConnection(String dbName) throws DBException 
	{
		Connection conn = null;
		
		try {
			conn = getDataSource(dbName).getConnection();
		} catch (SQLException e) 
		{
			//do log
		} catch (Exception e) 
		{
			//do log
		}finally 
		{
			return conn;
		}
	}
}
