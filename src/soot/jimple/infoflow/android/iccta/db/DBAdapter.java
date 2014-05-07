package soot.jimple.infoflow.android.iccta.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DBAdapter{

	public Object executeQuery(String sql, Object[] objs, String dbName) throws SQLException 
	{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Object rtObj = null;
		
		try 
		{
			conn = DBManager.getConnection(dbName);
			ps = DBManager.getPreparedStatement(conn, sql);
			
			if (null != objs) 
			{
				for (int i = 1; i <= objs.length; i++) 
				{
					ps.setObject(i, objs[i-1]);
				}
			}
			rs = DBManager.getResultSet(ps);
		
			rtObj = processResultSet(rs);
		}
		catch(Exception ex) 
		{
			throw new SQLException(ex);
		}
		finally 
		{
			DBManager.closeResultSet(rs);
			DBManager.closePreparedStatement(ps);
			DBManager.closeConnection(conn);
		}
		
		return rtObj;
	}

	protected abstract Object processResultSet(ResultSet rs);

}
