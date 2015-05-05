package soot.jimple.infoflow.android.iccta.links;

import soot.Scene;
import soot.SootClass;
import soot.jimple.infoflow.android.iccta.icc.ICCLink;
import soot.jimple.infoflow.android.iccta.util.AndroidHelper;

public class UnreasonableLinksRemover 
{
	public static boolean exitTypeNotEqualsToTargetComponentType(ICCLink link)
	{
		String exitType = link.getExit_kind();
		
		String destCompName = link.getDestinationC();
		SootClass destComp = Scene.v().getSootClass(destCompName);
		
		String type = AndroidHelper.getComponentType(destComp);
		
		if (! exitType.equals(type))
		{
			return true;
		}
		
		return false;
	}

	public static boolean isUnreasonable(ICCLink link) 
	{
		boolean result = false;
		
		result = result || exitTypeNotEqualsToTargetComponentType(link);
		
		return result;
	}
	
	
}
