package soot.jimple.infoflow.android.iccta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.iccta.icc.ICCLink;

/**
 * One ICC Link contain one source component and one destination component.
 * this class is used to collect all the assist methods which instrument source component.
 *
 */
public class ICCInstrumentSource 
{
	private static ICCInstrumentSource s = null;
	private ICCInstrumentSource () {}
    public static ICCInstrumentSource v() 
    {
        if (s == null) 
        {
            s = new ICCInstrumentSource();
        }
        return s;
    }
	
  //call this method for all your need to instrument the source class
    public void instrumentSource(ICCLink link, SootMethod redirectMethod)
    {
    	insertRedirectMethodCallAfterIccMethod(link, redirectMethod);
    	
    	//other instrument call
    }
    
    /**
     * we have the intent in a register at this point, 
     * create a new statement to call the static method with the intent as parameter
     * 
     * @param link
     * @param redirectMethod
     */
    public void insertRedirectMethodCallAfterIccMethod(ICCLink link, SootMethod redirectMethod)
    {
        List<Value> args = new ArrayList<Value>();
        
        Stmt fromStmt = (Stmt) link.getFromU();
    	
        if (fromStmt == null)
        {
        	return;
        }
        
        //specially deal with startActivityForResult since they have two parameters
    	if (fromStmt.toString().contains("startActivityForResult"))
    	{
    		Value arg0 = fromStmt.getInvokeExpr().getUseBoxes().get(0).getValue();
    		Value arg1 = fromStmt.getInvokeExpr().getArg(0);
    		args.add(arg0);
    		args.add(arg1);
    		
    		/*
    		ValueBox vb = (ValueBox) fromStmt.getUseBoxes().get(0);
        	Chain<Local> locals = link.fromSM.getActiveBody().getLocals();
        	//SootClass sc = null;
        	for (Iterator<Local> iter = locals.snapshotIterator(); iter.hasNext(); )
        	{
        		Local l = iter.next();
        		if (l.equivTo(vb.getValue()))
        		{
        			//System.out.println(l.getType());
        			//sc = Scene.v().getSootClass(l.getType().toString());
        			break;
        		}
        		
        	}*/
        	
        	//SootMethod sm = sc.getMethodByName("onActivityResult");
        	//System.out.println(sm.retrieveActiveBody());
    	}
    	else if (fromStmt.toString().contains("bindService"))
    	{
    		Value arg0 = fromStmt.getInvokeExpr().getArg(0);    //intent
    		Value arg1 = fromStmt.getInvokeExpr().getArg(1);    //serviceConnection
    		args.add(arg1);
    		args.add(arg0);
    	}
    	else
    	{
    		Value arg0 = fromStmt.getInvokeExpr().getArg(0);
            args.add(arg0);
    	}

    	if (redirectMethod == null)
    	{
    		return;
    	}
    	
    	Unit redirectCallU = (Unit) Jimple.v().newInvokeStmt(
                Jimple.v().newStaticInvokeExpr(
                		redirectMethod.makeRef(), 
                        args));

    	PatchingChain<Unit> units = link.getFromSM().retrieveActiveBody().getUnits();
    	
    	link.getFromSM().retrieveActiveBody().getUnits().insertAfter(redirectCallU, link.getFromU());
    	
    	for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
    	{
    		Stmt stmt = (Stmt) iter.next();
    		
    		if (stmt.toString().contains("<android.content.Intent: android.content.Intent createChooser(android.content.Intent,java.lang.CharSequence)>"))
    		{
    			List<ValueBox> vbs = stmt.getUseAndDefBoxes();
    			Unit assignU = Jimple.v().newAssignStmt(vbs.get(0).getValue(), vbs.get(1).getValue());
    			units.insertAfter(assignU, stmt);
    			units.remove(stmt);
    		}
    	}
    	
    	//comment the real ICC methods call
    	//link.fromSM.retrieveActiveBody().getUnits().remove(link.fromU);
    	
        System.out.println("new body: \n"+ link.getFromSM().retrieveActiveBody());
    }
}
