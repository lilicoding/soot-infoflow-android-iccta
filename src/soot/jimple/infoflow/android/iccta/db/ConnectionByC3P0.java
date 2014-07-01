package soot.jimple.infoflow.android.iccta.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.mchange.v2.c3p0.DataSources;

public class ConnectionByC3P0 implements IConnectionProvider {

	static String c3p0_properties_path = "c3p0.xml";
	
	public ConnectionByC3P0(String path) 
	{
		ConnectionByC3P0.c3p0_properties_path = path;
	}
	
	public DataSource getDataSource(String dbName) throws DBException
	{
		return C3p0PoolSource.getDataSource(dbName);
	}
	
	@Override
	public Connection getConnection(String dbName) throws DBException 
	{
		DataSource ds = getDataSource(dbName);

		Connection conn = null;
		try 
		{
			conn = ds.getConnection();
		} catch (SQLException e) 
		{
			//do log
		}
		
		return conn;
		
	}
	
	private static class C3p0PoolSource 
	{
		//this is good for multiple db-server
		private static Map<String, DataSource> services = new HashMap<String, DataSource>();

		private static C3p0PoolSource poolSource = null;
		public static DataSource getDataSource(String dbName) 
		{
			if (null == poolSource) {
				poolSource = new C3p0PoolSource();
			}
			
			return services.get(dbName);
		}
		
		private C3p0PoolSource() 
		{
			
			//this.getClass().getClassLoader().getResourceAsStream(c3p0_properties_path);
			
			SAXBuilder sax = new SAXBuilder();
			
			try 
			{
				InputStream inStream = new FileInputStream(new File(c3p0_properties_path));
				
				Document document = sax.build(inStream);
				Element root = document.getRootElement();
				
				List<Element> svs = root.getChildren();
				
				for (Element service : svs) {
					String dbName = "";
					String driver = "";
					String url = "";
					String username = "";
					String password = "";
					//boolean isSlave = false;
					
					Map<String, Object> maps = new HashMap<String, Object>();
					
					List<Element> elements = service.getChildren();
					for (Element e : elements) 
					{
						String name = e.getName();
						String value = e.getValue();
						
						if ("dbName".equals(name)) 
						{
							dbName = value;
						}else if ("driver".equals(name)) 
						{
							driver = value;
						}else if ("url".equals(name)) 
						{
							url = value;
						}else if ("username".equals(name)) 
						{
							username = value;
						}else if ("password".equals(name)) 
						{
							password = value;
						}else 
						{
							maps.put(name, value);
						}
					}
					
					Class.forName(driver);
					DataSource ds_unpooled = DataSources.unpooledDataSource(url, username, password);
					DataSource ds = DataSources.pooledDataSource(ds_unpooled, maps);
					
					services.put(dbName, ds);
				}
			} 
			catch (Exception e) 
			{
				//do log
			}
			
		}
	}
}
