/**
 * Author: li li
 * date: 2012-4-12
 */

package soot.jimple.infoflow.android.iccta.TestDroidBench;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesHelper 
{
	static Properties properties = null;
	static String filename = "pm.properties";
	
	private static void init() 
	{
		try 
		{
			properties = new Properties();
			InputStream stream = PropertiesHelper.class.getClassLoader().getResourceAsStream(filename);
			properties.load(stream);
			stream.close();
		} 
		catch (IOException e) 
		{
		}
		
	}
	
	public static boolean init(String filePath)
	{
		boolean result = true;
		try
		{
			properties = new Properties();
			InputStream stream = new BufferedInputStream(new FileInputStream(filePath));;
			properties.load(stream);
			stream.close();
		}
		catch(Exception e)
		{
			result = false;
		}
		
		return result;
	}
	
	private PropertiesHelper() throws IOException 
	{
	}
	
	
	public static Properties getPropertiesInstance() 
	{
		if (null == properties) 
		{
			init();
		}
		return properties;
	}
	
	public static String getPropertiesValue(String key) 
	{
		if (null == properties) 
		{
			init();
		}
		return properties.getProperty(key);
	}
}
