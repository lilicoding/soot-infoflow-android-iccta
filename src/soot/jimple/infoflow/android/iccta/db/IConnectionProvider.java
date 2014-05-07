package soot.jimple.infoflow.android.iccta.db;

import java.sql.Connection;

public interface IConnectionProvider {
	public Connection getConnection(String dbName) throws DBException;
	//public Connection getConnection(String dbName, boolean isSlave) throws DBException;
	
}
