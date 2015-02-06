package soot.jimple.infoflow.android.iccta.sharedpreferences;

import soot.SootMethod;
import soot.jimple.Stmt;

public class StmtWrapper
{
	public Stmt stmt = null;
	public SootMethod belongsTo = null;
	
	public StmtWrapper() {}
	
	public StmtWrapper(Stmt stmt, SootMethod belongsTo)
	{
		this.stmt = stmt;
		this.belongsTo = belongsTo;
	}
}
