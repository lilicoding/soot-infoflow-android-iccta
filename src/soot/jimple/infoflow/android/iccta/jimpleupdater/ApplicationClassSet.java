package soot.jimple.infoflow.android.iccta.jimpleupdater;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Scene;
import soot.SootClass;
import soot.util.Chain;
import soot.util.HashChain;

public class ApplicationClassSet implements JimpleUpdater {

	public static Chain<SootClass> applicationClasses = new HashChain<SootClass>();
	public Set<String> prefixes = new HashSet<String>();
	
	public String filename = "res/androidClassPrefixes.list";
	
	@Override
	public void updateJimple() 
	{
		load();
		
		Chain<SootClass> classes = Scene.v().getClasses();
		for (Iterator<SootClass> iter = classes.snapshotIterator(); iter.hasNext(); )
		{
			SootClass sc = iter.next();
			String name = sc.getName();
			String prefix = toPrefix(name);
			
			if (! prefixes.contains(prefix))
			{
				applicationClasses.add(sc);
			}
		}
		
		System.out.println("Application classes:");
		for (SootClass sc : applicationClasses)
		{
			System.out.println(sc.getName());
		}
	}

	private void load()
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));

			String line = "";
			
			while ((line = br.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("%"))
				{
					continue;
				}
				
				prefixes.add(line);
			}
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private String toPrefix(String clsName)
	{
		int idx = clsName.indexOf('.');
		
		if (-1 != idx)
		{
			idx = clsName.indexOf('.', idx+1);
		}
		
		if (-1 != idx)
		{
			return clsName.substring(0, idx);
		}
		
		return clsName;
	}
	
	public static void main(String[] args)
	{
		System.out.println(new ApplicationClassSet().toPrefix(""));
	}
}
