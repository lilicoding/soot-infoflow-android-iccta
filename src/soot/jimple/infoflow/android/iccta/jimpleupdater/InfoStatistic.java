package soot.jimple.infoflow.android.iccta.jimpleupdater;

import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.util.Chain;

public class InfoStatistic implements JimpleUpdater 
{
	public static int numberOfClass = 0;
	public static int numberOfComponent = 0;
	public static int numberOfLine = 0;
	public static int numberOfMethod = 0;
	
	private String step = "";
	
	public InfoStatistic()
	{
		this("Default");
	}
	
	public InfoStatistic(String step)
	{
		this.step = step;
	}
	
	@Override
	public void updateJimple() 
	{
		numberOfClass = 0;
		numberOfComponent = 0;
		numberOfLine = 0;
		numberOfMethod = 0;
		
		Chain<SootClass> sootClasses = Scene.v().getClasses();
		
		for (Iterator<SootClass> iter = sootClasses.iterator(); iter.hasNext(); )
		{
			SootClass sc = iter.next();
			
			if (sc.getName().startsWith("android.support."))
			{
				continue;
			}
			
			numberOfClass++;

			if (isAndroidComponent(sc))
			{
				numberOfComponent++;
			}
			
			List<SootMethod> sootMethods = sc.getMethods();
			for (SootMethod sm : sootMethods)
			{
				numberOfMethod++;
				
				try
				{
					Body b = sm.retrieveActiveBody();
					PatchingChain<Unit> units = b.getUnits();
					for (Iterator<Unit> iter2 = units.snapshotIterator(); iter2.hasNext(); )
					{
						numberOfLine++;
						
						iter2.next();
					}
				}
				catch (Exception ex)
				{
					//ex.printStackTrace();
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("In step " + step + ", " + "The number of Jimple line is " + numberOfLine + "\n");
		sb.append("In step " + step + ", " + "The number of Jimple method is " + numberOfMethod + "\n");
		sb.append("In step " + step + ", " + "The number of Jimple class is " + numberOfClass + "\n");
		sb.append("In step " + step + ", " + "The number of Jimple component is " + numberOfComponent + "\n");
		System.out.println(sb.toString());
	}

	boolean isAndroidComponent(SootClass sc)
	{
		while (sc.hasSuperclass())
		{
			SootClass superClass = sc.getSuperclass();
			
			if (superClass.getName().equals("android.content.Context"))
			{
				return true;
			}
			else if (superClass.getName().equals("android.content.BroadcastReceiver"))
			{
				return true;
			}
			else if (superClass.getName().equals("android.content.ContentProvider"))
			{
				return true;
			}
			
			sc = superClass;
		}
		
		return false;
	}
}
