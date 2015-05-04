package soot.jimple.infoflow.android.iccta.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootClass;

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
	
	public static String[] androidComponents = {
    	"android.app.Activity",
    	"android.app.Service",
    	"android.content.BroadcastReceiver",
    	"android.content.ContentProvider"
    };
	
	public static String getComponentType(SootClass sc)
	{
		try
		{
			List<String> comps = Arrays.asList(androidComponents);
			
	    	if (comps.contains(sc.getName()))
	    	{
	    		if (sc.getName().equals(androidComponents[0]))
	    		{
	    			return "a";
	    		}
	    		else if (sc.getName().equals(androidComponents[1]))
	    		{
	    			return "s";
	    		}
	    		else if (sc.getName().equals(androidComponents[2]))
	    		{
	    			return "r";
	    		}
	    		else if (sc.getName().equals(androidComponents[3]))
	    		{
	    			return "p";
	    		}
	    	}
	    	
	    	while (sc.hasSuperclass())
	    	{
	    		sc = sc.getSuperclass();
	    		
	    		if (comps.contains(sc.getName()))
	        	{
	    			if (sc.getName().equals(androidComponents[0]))
		    		{
		    			return "a";
		    		}
		    		else if (sc.getName().equals(androidComponents[1]))
		    		{
		    			return "s";
		    		}
		    		else if (sc.getName().equals(androidComponents[2]))
		    		{
		    			return "r";
		    		}
		    		else if (sc.getName().equals(androidComponents[3]))
		    		{
		    			return "p";
		    		}
	        	}
	    	}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return "NULL";
	}
}
