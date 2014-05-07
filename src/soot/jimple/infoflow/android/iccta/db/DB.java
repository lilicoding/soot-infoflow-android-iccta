package soot.jimple.infoflow.android.iccta.db;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class DB{
	
	public static void setConnectionType(ConnectionType type) {
		ConnectionProviderManager.setConnectionType(type);
	}
	public static void setC3p0Path(String path) {
		ConnectionProviderManager.setC3p0Path(path);
	}
	public static void setJdbcPath(String path) {
		ConnectionProviderManager.setJdbcPath(path);
	}
	
	public static List<Object> executeQuery(Class clazz, String sql, Object[] objs, String dbName) throws Exception {
		List<Object> lists = new ArrayList<Object>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
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
			
			Field[] fields = clazz.getDeclaredFields();
			String[] colNames = getColNames(rs);
			while (rs.next()) 
			{
				Object obj = clazz.newInstance();
				for (int j = 0; j < colNames.length; j++) 
				{
					String sqlColumnName = colNames[j];
					
					Object v = rs.getObject(j+1);
					for (int i = 0; i < fields.length; i++) 
					{
						if (fields[i].getName().equalsIgnoreCase(sqlColumnName)) 
						{
							if (null != v) {
								fields[i].setAccessible(true);
								fields[i].set(obj, v);
								fields[i].setAccessible(false);
								break;
							}
						}
					}
				}
				lists.add(obj);
			}
		}catch(Exception ex) {
			throw ex;
		}finally {
			DBManager.closeResultSet(rs);
			DBManager.closePreparedStatement(ps);
			DBManager.closeConnection(conn);
		}	
		
		return lists;
	}

	public static int executeUpdate(String sql, Object[] objs, String dbName) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		int count = -1;
		
		try {
			conn = DBManager.getConnection(dbName);
			ps = DBManager.getPreparedStatement(conn, sql);
			
			if (null != objs) {
				for (int i = 1; i <= objs.length; i++) {
					ps.setObject(i, objs[i-1]);
				}
			}
			
			count = ps.executeUpdate();
		}catch(Exception ex) {
			throw ex;
		}finally {
			DBManager.closePreparedStatement(ps);
			DBManager.closeConnection(conn);
		}

		return count;
	}
	
	
	
	private static String[] getColNames(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        
        String[] colNames = new String[columns];
        for (int i = 1; i <= columns; i++) {
            colNames[i - 1] = rsmd.getColumnLabel(i);
        }
        return colNames;
    }
}