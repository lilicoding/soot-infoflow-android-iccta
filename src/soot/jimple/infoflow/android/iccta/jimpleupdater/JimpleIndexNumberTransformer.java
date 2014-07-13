package soot.jimple.infoflow.android.iccta.jimpleupdater;

import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.iccta.JimpleIndexNumberTag;
import soot.tagkit.Tag;
import soot.util.Chain;

public class JimpleIndexNumberTransformer implements JimpleUpdater
{
	@Override
	public void updateJimple() 
	{
		Chain<SootClass> sootClasses = Scene.v().getClasses();
		
		for (Iterator<SootClass> iter = sootClasses.iterator(); iter.hasNext(); )
		{
			SootClass sc = iter.next();
			
			//Putting all the code in a try-catch.
			//Just trying the best to put the index number to "JimpleIndexNumberTag" of Stmt.
			try
			{
				List<SootMethod> sms = sc.getMethods();
				
				for (SootMethod sm : sms)
				{
					Body b = sm.retrieveActiveBody();
					
					PatchingChain<Unit> units = b.getUnits();
					
					int indexNumber = 0;
					
					for (Iterator<Unit> iterU = units.snapshotIterator(); iterU.hasNext(); )
					{
						Stmt stmt = (Stmt) iterU.next();
						
						//System.out.println(indexNumber + "->" + stmt);
						
						Tag t = new JimpleIndexNumberTag(indexNumber++);
						stmt.addTag(t);
					}
				}
			}
			catch (Exception ex)
			{
				//System.out.println("Exception in " + sc.getName());
				//ex.printStackTrace();
			}
		}
	}
}
