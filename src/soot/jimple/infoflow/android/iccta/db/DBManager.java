package soot.jimple.infoflow.android.iccta.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBManager {
	
	public static Connection getConnection(String dbName) throws SQLException {
		Connection conn = null;
		try {
			ConnectionProviderManager connManager = new ConnectionProviderManager();
			conn = connManager.getConnectionProvider().getConnection(dbName);
		} catch (Exception e) {
			throw new SQLException(e);
		}
		
		return conn;
	}
	
	public static void closeConnection(Connection conn) throws SQLException {
		if (null != conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new SQLException(e);
			}
		}
	}
	
	public static PreparedStatement getPreparedStatement(Connection conn, String sql) throws SQLException {
		PreparedStatement ps = null;
		
		try {
			ps = conn.prepareStatement(sql);
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		
		return ps;
	}
	
	public static void closePreparedStatement(PreparedStatement ps) throws SQLException {
		try {
			if (null != ps)
				ps.close();
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}

	public static ResultSet getResultSet(PreparedStatement ps) throws SQLException {
		ResultSet rs = null;
		try {
			rs = ps.executeQuery();
		} catch (SQLException e) {
			throw new SQLException(e);
		}
		return rs;
	}
	
	public static void closeResultSet(ResultSet rs) throws SQLException {
		try {
			if (null != rs)
				rs.close();
		} catch (SQLException e) {
			throw new SQLException(e);
		}
	}
}
