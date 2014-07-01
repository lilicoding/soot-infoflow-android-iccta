package soot.jimple.infoflow.android.iccta;

import java.util.List;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.util.Chain;

public abstract class JimpleBodyUpdater implements JimpleUpdater 
{
	@Override
	public void updateJimple() 
	{
		updateJimple(true);
	}
	
	
	public void updateJimple(boolean excludeAndroidSupportJar) 
	{
		Chain<SootClass> sootClasses = Scene.v().getClasses();
		for (SootClass sc : sootClasses)
		{	
			if (sc.getName().startsWith("android.support") && excludeAndroidSupportJar)
			{
				continue;
			}
			
			List<SootMethod> sootMethods = sc.getMethods();
			for (SootMethod sm : sootMethods)
			{
				Body b = null;
				try
				{
					b = sm.retrieveActiveBody();
				}
				catch (Exception ex) {
					continue;
				}
				
				this.updateBodyJimple(b);
			}
		}
		
	}

	public abstract void updateBodyJimple(Body body);
}
