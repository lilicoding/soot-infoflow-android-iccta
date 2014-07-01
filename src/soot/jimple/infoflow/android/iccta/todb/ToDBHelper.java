package soot.jimple.infoflow.android.iccta.todb;

import java.sql.ResultSet;
import java.sql.SQLException;

import soot.jimple.infoflow.android.iccta.db.DBAdapter;
import soot.jimple.infoflow.android.iccta.util.Constants;

public class ToDBHelper 
{
	public static int getClassId(String clsName, String appName) throws Exception
	{
		String sql = "select a.id from Classes a, Applications b where a.app_id = b.id and a.class=? and b.app=?";
		
		DBAdapter adapter = new DBAdapter()
		{

			@Override
			protected Object processResultSet(ResultSet rs) 
			{
				int id = -1;
				try {
					if (rs.next())
					{
						id = rs.getInt(1);
					}
					
					if (rs.next())
					{
						//throw new RuntimeException("multiple class exsit in a single app.");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				return id;
			}
			
		};
		
		int id = (int) adapter.executeQuery(sql, new Object[] {clsName, appName}, Constants.DB_NAME);
		
		return id;
	}
	
	public static int getStmtId(String stmt, String method, int classId) throws Exception
	{
		String sql = "select id from Stmts where stmt=? and method=? and class_id=?;";
		
		DBAdapter adapter = new DBAdapter()
		{

			@Override
			protected Object processResultSet(ResultSet rs) 
			{
				int id = -1;
				try {
					if (rs.next())
					{
						id = rs.getInt(1);
					}
					
					if (rs.next())
					{
						//throw new RuntimeException("multiple class exsit in a single app.");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				return id;
			}
			
		};
		
		int id = (int) adapter.executeQuery(sql, new Object[] {stmt, method, classId}, Constants.DB_NAME);

		return id;
	}
	
	public static int getStmtId(String stmt, String method, String className, String appName) throws Exception
	{
		int classId = getClassId(className, appName);
		int id = getStmtId(stmt, method, classId);

		return id;
	}
	
	public static int getExitId(int class_id, String method, int jimpleIndex) throws Exception
	{
		String sql = "select id from ExitPoints where class_id=? and method=? and instruction=?;";
		
		DBAdapter adapter = new DBAdapter()
		{

			@Override
			protected Object processResultSet(ResultSet rs) 
			{
				int id = -1;
				try {
					if (rs.next())
					{
						id = rs.getInt(1);
					}
					
					if (rs.next())
					{
						//throw new RuntimeException("multiple class exsit in a single app.");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				return id;
			}
			
		};
		
		int id = (int) adapter.executeQuery(sql, new Object[] {class_id, method, jimpleIndex}, Constants.DB_NAME);

		return id;
	}
}
