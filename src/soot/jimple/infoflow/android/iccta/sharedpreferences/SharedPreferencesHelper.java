package soot.jimple.infoflow.android.iccta.sharedpreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;

public class SharedPreferencesHelper {

	private SootClass srhSC = null;
	private Map<String, SootField> fieldMapping = new HashMap<String, SootField>();
	
	public SharedPreferencesHelper()
	{
		srhSC = new SootClass("SharedPreferencesHelper");
		srhSC.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        Scene.v().addClass(srhSC);
	}
	
	public void fieldGenerater(Set<String> fieldNames)
	{
		for (String fieldName : fieldNames)
		{
			fieldName = fieldName.replace("\"", "");
			
			int m = Modifier.PUBLIC | Modifier.STATIC;
	        SootField sf = new SootField(fieldName, RefType.v("java.lang.Object"), m);
	        srhSC.addField(sf);
	        
	        fieldMapping.put(fieldName, sf);
		}
	}
	
	public void fieldInstrumentation(Map<StmtWrapper, String> fieldStmts)
	{
		for (StmtWrapper stmtWrapper : fieldStmts.keySet())
		{
			SootMethod sm = stmtWrapper.stmt.getInvokeExpr().getMethod();
			
			if (sm.getDeclaringClass().getName().contains("android.content.SharedPreferences$Editor"))
			{
				if (sm.getName().equals("clear") || sm.getName().equals("remove"))
				{
					for (String fieldName : fieldMapping.keySet())
					{
						Stmt stmt1 = SootHelper.createAssignStmt(Jimple.v().newStaticFieldRef(fieldMapping.get(fieldName).makeRef()), NullConstant.v());
						SootHelper.insertAfter(stmtWrapper.belongsTo, stmt1, stmtWrapper.stmt);
					}
				}
				else if (sm.getName().startsWith("put"))
				{
					String str = stmtWrapper.stmt.getInvokeExpr().getArg(0).toString();
					String fieldName = fieldStmts.get(stmtWrapper) + "_" + str;
					fieldName = fieldName.replace("\"", "");
					
					Value v = stmtWrapper.stmt.getInvokeExpr().getArg(1);
					
					Body b = stmtWrapper.belongsTo.retrieveActiveBody();
					
					LocalGenerator lg = new LocalGenerator(b);
					Local l = lg.generateLocal(RefType.v("java.lang.Object"));
					
					CastExpr castExpr = Jimple.v().newCastExpr(v, RefType.v("java.lang.Object"));
					Stmt stmt1 = SootHelper.createAssignStmt(l, castExpr);
					
					Stmt stmt2 = SootHelper.createAssignStmt(Jimple.v().newStaticFieldRef(fieldMapping.get(fieldName).makeRef()), l);
					
					SootHelper.insertAfter(stmtWrapper.belongsTo, stmt2, stmtWrapper.stmt);
					SootHelper.insertAfter(stmtWrapper.belongsTo, stmt1, stmtWrapper.stmt);
					
					System.out.println(b);
				}
			}
			else
			{
				if (sm.getName().equals("getAll"))
				{	
					//TODO return the value of all fields
				}
				else if (sm.getName().startsWith("get"))
				{
					String str = stmtWrapper.stmt.getInvokeExpr().getArg(0).toString();
					String fieldName = fieldStmts.get(stmtWrapper) + "_" + str;
					fieldName = fieldName.replace("\"", "");
					
					AssignStmt assignStmt = (AssignStmt) stmtWrapper.stmt;
					
					Value v = assignStmt.getLeftOp();
					
					Body b = stmtWrapper.belongsTo.retrieveActiveBody();
					
					LocalGenerator lg = new LocalGenerator(b);
					Local l = lg.generateLocal(RefType.v("java.lang.Object"));
					
					Stmt stmt1 = SootHelper.createAssignStmt(l, Jimple.v().newStaticFieldRef(fieldMapping.get(fieldName).makeRef()));
					Stmt stmt2 = SootHelper.createAssignStmt(v, l);
					
					SootHelper.insertAfter(stmtWrapper.belongsTo, stmt2, stmtWrapper.stmt);
					SootHelper.insertAfter(stmtWrapper.belongsTo, stmt1, stmtWrapper.stmt);
					
					System.out.println(b);
				}
			}
		}
	}
}
