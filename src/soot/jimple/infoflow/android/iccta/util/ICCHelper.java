package soot.jimple.infoflow.android.iccta.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import soot.jimple.Stmt;

public class ICCHelper 
{
	public static List<String> iccMethods = new ArrayList<String>();
	
	static 
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("res/IPCMethods.txt"));
			
			String line = "";
			while ((line = br.readLine()) != null)
			{
				if (line.equals("") || line.startsWith("#"))
				{
					continue;
				}
				iccMethods.add(line);
			}
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static boolean isIccStmt(Stmt stmt, boolean source)
	{
		boolean rtVal = false;
		
		if (source)
		{
			if (stmt.containsInvokeExpr())
			{
				String clazz = StmtHelper.getClazzName(stmt.toString());
				String method = StmtHelper.getMethodName(stmt.toString());
				
				if (method.equals("getIntent"))
				{
					rtVal = true;
				}
				
				if (clazz.equals("android.os.Bundle") && method.startsWith("get"))
				{
					rtVal = true;
				}
				else if (clazz.equals("android.content.Intent") && method.startsWith("get"))
				{
					rtVal = true;
				}
			}
			else
			{
				if (stmt.toString().contains("Intent"))
				{
					rtVal = true;
				}
			}
		}
		else //sink
		{
			//more sophisticatedly, we need to compare the class whether it belongs to components or not
			String m = StmtHelper.getMethodName(stmt.toString());
			
			for (String method : iccMethods)
			{
				if (method.contains(m) && m.length() >= 4)
				{
					rtVal = true;
					break;
				}
			}
		}
		
		return rtVal;
	}
}
