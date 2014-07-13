package soot.jimple.infoflow.android.iccta.links;

import soot.jimple.infoflow.android.iccta.stat.IntentDB;
import soot.jimple.infoflow.android.iccta.stat.IntentFilterDB;

public interface IMatchAlgo 
{
	public boolean match(IntentDB intent, IntentFilterDB intentFilter, int level);
}
