package soot.jimple.infoflow.android.iccta.sharedpreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Immediate;
import soot.Modifier;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;

public class SootHelper 
{
	public static SootClass createSootClass(String clsName)
	{
		SootClass sc = new SootClass(clsName);
		sc.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		
		sc.setApplicationClass();
		sc.setPhantom(false);
		sc.setInScene(true);
       
		return sc;
	}
	
	public static SootMethod createSootMethod(SootClass sootClass, String name, List<Type> paramTypes, Type returnType, boolean isStatic)
	{
        int modifier = Modifier.PUBLIC;
        
        if (isStatic)
		{
			modifier = modifier | Modifier.STATIC;
		}
        
        SootMethod sootMethod = new SootMethod(name, paramTypes, returnType, modifier);
        sootClass.addMethod(sootMethod);
        
        Body body = Jimple.v().newBody(sootMethod);
        sootMethod.setActiveBody(body);
		
		return sootMethod;
	}
	
	public static SootField createSootField(SootClass sootClass, String name, Type type, boolean isStatic)
	{
		int modifier = Modifier.PUBLIC;
		
		if (isStatic)
		{
			modifier = modifier | Modifier.STATIC;
		}
		
        SootField sootField = new SootField(name, type, modifier);
        sootClass.addField(sootField);
		
        System.out.println("CREATE-FIELD: " + name);
        
		return sootField;
	}
	
	public static Stmt createAssignStmt(Value leftOp, Value rightOp)
	{
		return Jimple.v().newAssignStmt(leftOp, rightOp);
	}
	
	public static void insertBefore(SootMethod sootMethod, Unit insertedUnit, Unit originUnit)
	{
		Body b = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		
		units.insertBefore(insertedUnit, originUnit);
	}
	
	public static Unit insertAfter(SootMethod sootMethod, Unit insertedUnit, Unit originUnit)
	{
		Body b = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		
		units.insertAfter(insertedUnit, originUnit);
		
		return insertedUnit;
	}
	
	public static Stmt getFirstNonIdentityStmt(SootMethod sootMethod)
	{
		Stmt rtVal = null;
		
		Body b = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		
		for (Iterator<Unit> iter = units.iterator(); iter.hasNext(); )
		{
			Stmt stmt = (Stmt) iter.next();
			
			if ( ! (stmt instanceof IdentityStmt) )
			{
				rtVal = stmt;
			}
		}
		
		return rtVal;
	}
	
	public static Stmt getReturnStmt(SootMethod sootMethod)
	{
		Stmt rtVal = null;
		
		Body b = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		
		for (Iterator<Unit> iter = units.iterator(); iter.hasNext(); )
		{
			Stmt stmt = (Stmt) iter.next();
			
			if (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt)
			{
				rtVal = stmt;
			}
		}
		
		return rtVal;
	}
	
	public static List<Value> getAllImmediateValue(Stmt stmt)
	{
		List<Value> rtVal = new ArrayList<Value>();
		
		List<ValueBox> vbs = stmt.getUseAndDefBoxes();
		Set<String> frs = new HashSet<String>();
		
		for (ValueBox vb : vbs)
		{
			Value v = vb.getValue();
			
			if (v instanceof FieldRef)
			{
				int endPos = v.toString().indexOf('.');
				String name = v.toString().substring(0, endPos);
				frs.add(name);
				
				Value existV = null;
				for (ValueBox vBox : vbs)
				{
					if (name.equals(vBox.getValue().toString()))
					{
						existV = vBox.getValue();
						break;
					}
				}
				
				if (null != existV)
				{
					rtVal.remove(existV);
				}

				rtVal.add(v);
			}
			
			if (v instanceof Immediate)
			{
				if (! frs.contains(v.toString()))
				{
					rtVal.add(v);
				}
			}
		}
		
		return rtVal;
	}
}
