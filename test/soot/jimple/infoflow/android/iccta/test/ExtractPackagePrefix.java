package soot.jimple.infoflow.android.iccta.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class ExtractPackagePrefix {

	static Set<String> strs = new HashSet<String>();
	
	public static void main(String[] args) 
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("res/androidClasses.list"));
			
			String line = "";
			
			while ((line = br.readLine()) != null)
			{
				String[] ss = line.split("\\.");
				
				String sss = ss[0] + "." + ss[1];
				
				if (sss.startsWith("android.R"))
				{
					continue;
				}
				
				strs.add(ss[0] + "." + ss[1]);
			}
			br.close();
			
			strs.add("android.R");
			
			for (String s : strs)
			{
				System.out.println(s);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

}
