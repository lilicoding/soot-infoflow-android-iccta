package soot.jimple.infoflow.android.iccta.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;

import javax.naming.InitialContext;

public class ConnectionProviderManager {
	
	public Connection getConnection(String dbName) throws Exception{
		return getConnectionProvider().getConnection(dbName);
	}

	private static boolean defaultType = true;
	private static ConnectionType connType = ConnectionType.JDBC;
	private static String c3p0Path = "c3p0.xml";
	private static String jdbcPath = "jdbc.xml";
	
	public static void setConnectionType(ConnectionType type) {
		connType = type;
		defaultType = false;
	}
	public static void setC3p0Path(String path) {
		c3p0Path = path;
	}
	public static void setJdbcPath(String path) {
		jdbcPath = path;
	}
	
	public IConnectionProvider getConnectionProvider() throws Exception {
		if (true == defaultType) {
			if (isC3p0()) {
				connType = ConnectionType.C3P0;
			}else if (isServer()) {
				connType = ConnectionType.JNDI;
			}
		}
		
		switch(connType) {
		case C3P0:
			return new ConnectionByC3P0(c3p0Path);
		case JNDI:
			return new ConnectionByJNDI();
		default:
			return new ConnectionByXMLProperty(jdbcPath);
		}
	}

	private boolean isC3p0() 
	{
		boolean isC3p0 = false;
		try 
		{
			InputStream stream = new FileInputStream(new File(c3p0Path));
			
			//InputStream stream = this.getClass().getClassLoader().getResourceAsStream(c3p0Path);
			if (stream != null) 
			{
				isC3p0 = true;  //and not throw a exception
			}
			
			stream.close();
		}catch(Exception ex) {
			//do noting, means not deploy the c3p0 pool
		}
		
		return isC3p0;
	}
	
	private boolean isServer() {
		boolean isServer = false;
		try {
			new InitialContext().lookup("java:comp/env");
			isServer = true;  //not throw a exception
		}catch(Exception ex) {
			//do noting, means not deploy the jndi method
		}
		
		return isServer;
	}
}
