package soot.jimple.infoflow.android.iccta;

import java.util.Arrays;
import java.util.List;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.infoflow.android.data.AndroidMethod;

public class ICCMethodHelper {

	public static String[] androidComponents = {
    	"android.content.Context",    //Activity and Service
    	"android.content.BroadcastReceiver",
    	"android.content.ContentProvider"
    };
	
	public static boolean isIccMethod(SootMethod sm)
	{
		if (isAndroidComponent(sm))
    	{
    		for (AndroidMethod am : AndroidIPCManager.ipcAMethods)
    		{
    			//same method and same parameters
    			if (! am.getMethodName().equals(sm.getName()))
    			{
    				continue;
    			}
    			
				List<String> params = am.getParameters();
				List<Type> types = sm.getParameterTypes();
    				
				if (params.size() != types.size())
				{
					continue;
				}
    				
				for (int i = 0; i < params.size(); i++)
				{
					String p1 = params.get(i);
					String p2 = types.get(i).toString();
					
					if (! p1.equals(p2))
					{
						continue;
					}
    			}
				
				return true;
    		}
    	}
		
		return false;
	}
	
    private static boolean isAndroidComponent(SootMethod method)
    {
    	SootClass sc = method.getDeclaringClass();
    	List<String> comps = Arrays.asList(androidComponents);
    	
    	if (comps.contains(sc.getName()))
    	{
    		return true;
    	}
    	
    	while (sc.hasSuperclass())
    	{
    		sc = sc.getSuperclass();
    		
    		if (comps.contains(sc.getName()))
        	{
        		return true;
        	}
    	}
    	
    	return false;
    }
}
