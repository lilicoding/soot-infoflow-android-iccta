package soot.jimple.infoflow.android.iccta.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class ConnectionByXMLProperty implements IConnectionProvider{
	
	private Map<String, DBElement> connMaps = new HashMap<String, DBElement>();
	
	public ConnectionByXMLProperty(String path) throws DBException 
	{
		InputStream inStream;
		try {
			inStream = new FileInputStream(new File(path));
			
			init(inStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.getClass().getClassLoader().getResourceAsStream(path);
		
		
	}
	
	@SuppressWarnings("finally")
	@Override
	public Connection getConnection(String dbName) throws DBException 
	{
		DBElement ele = connMaps.get(dbName);
		
		Connection conn = null;
		try {
			Class.forName(ele.getDriver());
			conn = DriverManager.getConnection(ele.getUrl(), ele.getUsername(), ele.getPassword());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			//do log
		} catch (SQLException e) {
			e.printStackTrace();
			//do log
		} finally {
			return conn;
		}
	}
	
	private void init(InputStream inStream) 
	{
		SAXBuilder sax = new SAXBuilder();
		try {
			Document document = sax.build(inStream);
			
			Element root = document.getRootElement();
			
			List<Element> dbs = root.getChildren();
			for (Element e : dbs) {
				String name = e.getChildText("name");
				String driver = e.getChildText("driver");
				String url = e.getChildText("url");
				String username = e.getChildText("username");
				String password = e.getChildText("password");
				
				DBElement ele = new DBElement();
				ele.setName(name);
				ele.setDriver(driver);
				ele.setUrl(url);
				ele.setUsername(username);
				ele.setPassword(password);
				
				connMaps.put(name, ele);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class DBElement {
		String name;
		String driver;
		String url;
		String username;
		String password;
		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDriver() {
			return driver;
		}
		public void setDriver(String driver) {
			this.driver = driver;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	}
}
