package soot.jimple.infoflow.android.iccta.jimpleupdater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.iccta.util.AndroidHelper;

public class ExtraExtractor extends JimpleBodyUpdater 
{
	//method to extras, when a string is not directly specified in the invoke stmt, <anything> is specified.
	public static Map<String, List<String>> getExtras = new HashMap<String, List<String>>(); 
	public static Map<String, List<String>> putExtras = new HashMap<String, List<String>>();
	public static final String intentCls = "android.content.Intent";
	public static final String bundleCls = "android.os.Bundle";
	
	@Override
	public void updateBodyJimple(Body body) 
	{
		if (AndroidHelper.isAndroidClass(body.getMethod().getDeclaringClass().getName()))
		{
			return;
		}
		
		PatchingChain<Unit> units = body.getUnits();
		
		String methodSignature = body.getMethod().getSignature();
		int count = 0;
		List<String> getKeys = new ArrayList<String>();
		List<String> putKeys = new ArrayList<String>();
		
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
		{
			Stmt stmt = (Stmt) iter.next();
			
			if (! stmt.containsInvokeExpr())
			{
				continue;
			}
			
			SootMethod sm = stmt.getInvokeExpr().getMethod();
			String methodName = sm.getName();
			int type = 0; //0:other | 1:get | 2:put
			String extraKey = null;
			
			if (methodName.startsWith("get"))
			{
				type = 1;
			}
			else if (methodName.startsWith("put"))
			{
				type = 2;
			}
			
			if (0 == type)
			{
				continue;
			}
			
			if ( (sm.getDeclaringClass().toString().equals(intentCls) && methodName.contains("Extra")) ||
				 (sm.getDeclaringClass().toString().equals(bundleCls)))
			{
				if (stmt.getInvokeExpr().getArgs().size() > 0)
				{
					Value v = stmt.getInvokeExpr().getArgs().get(0);
					if (v.toString().contains("\""))
					{
						extraKey = v.toString();
					}
					else
					{
						extraKey = "<anything>" + (count++);
					}
				}
			}
			
			if (type == 1 && extraKey != null)
			{
				getKeys.add(extraKey);
			}
			else if (type == 2 && extraKey != null)
			{
				putKeys.add(extraKey);
			}
		}
		
		if (getKeys.size() != 0)
		{
			getExtras.put(methodSignature, getKeys);
		}
		if (putKeys.size() != 0)
		{
			putExtras.put(methodSignature, putKeys);
		}
		
		//System.out.println(getExtras);
		//System.out.println(putExtras);
	}
}
