package soot.jimple.infoflow.android.iccta.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class AndroidHelper 
{
	private static Set<String> androidClassPrefixes = new HashSet<String>();
	
	static
	{
		loadAndroidClass("res/androidClassPrefixes.list");
	}
	
	public static boolean isAndroidClass(String clsName)
	{
		for (String prefix : androidClassPrefixes)
		{
			if (clsName.startsWith(prefix))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static void loadAndroidClass(String filePath)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			
			String line = "";
			
			while ((line = br.readLine()) != null)
			{
				androidClassPrefixes.add(line);
			}
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
