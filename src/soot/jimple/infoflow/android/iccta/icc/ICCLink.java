package soot.jimple.infoflow.android.iccta.icc;

import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.iccta.AndroidIPCManager;
import soot.util.Chain;

public class ICCLink 
{    
    String fromSMString;
    SootMethod fromSM;
    Unit fromU;
    int instruction;
    String exit_kind;
    String destinationC;
    
    List<Integer> instructions;
    
    Chain<Unit> units = null;
    
    public ICCLink(String fromSMString, int instruction, String exit_kind, String destinationC, List<Integer> instructions) 
    {
        this.fromSMString = fromSMString;
        this.fromSM = null;
        this.fromU = null;
        this.instruction = instruction;
        this.exit_kind = exit_kind;
        this.destinationC = destinationC;
        this.instructions = instructions;
        
        linkWithTarget();
    }
    
    public void linkWithTarget() {
        if (fromSM == null) {
        	try
        	{
        		fromSM = Scene.v().getMethod(fromSMString);
        	}
        	catch (Exception ex)
        	{
        		ex.printStackTrace();
        	}
        	
        }
            
        Body body = fromSM.retrieveActiveBody();
        units = body.getUnits();
        
        // index in (0, 1, 2, 3, ...)
        int index = instructions.indexOf(instruction);
        
        System.out.println("body: "+ body);
        // get correct unit for the link source method
        int i = 0;
        for (Unit u: units) {
            Stmt stmt = (Stmt)u;
            System.out.println("bs: "+ stmt);
            if (!stmt.containsInvokeExpr())
                continue;
            System.out.println("s: "+ stmt);
            if (isICCMethod(stmt.getInvokeExpr().getMethod())) {
                System.out.println("u: "+ u);
                if (index == i++) {
                    fromU = u;
                    break;
                }
            }
            
        }
        System.out.println("fromU: "+ fromU);
    }

    public boolean isICCMethod(SootMethod sm) 
    {
        Set<AndroidMethod> amSet = AndroidIPCManager.ipcAMethods;
        String rightSm = sm.toString().split(":")[1];
        for (AndroidMethod am: amSet) 
        {
            String amRight = am.getSignature().split(":")[1];
            if (amRight.equals(rightSm)) 
            {
            	return true;
            }
        }
        return false;
    }

    /**
     * this will return a unique String for ICCLink object
     */
    public String toString() 
    {
    	StringBuilder sb = new StringBuilder();
    	boolean first = true;
    	if (instructions != null)
    	{
    		for (int num : instructions)
    		{
    			if (first)
    			{
    				sb.append(num);
    				first = false;
    			}
    			else
    			{
    				sb.append("," + num);
    			}
    			
    		}
    	}
    	
    	return fromSMString + " [" + instruction + "] " + destinationC + " {" + sb.toString() + "}";
    }

	public String getFromSMString() {
		return fromSMString;
	}

	public void setFromSMString(String fromSMString) {
		this.fromSMString = fromSMString;
	}

	public SootMethod getFromSM() {
		return fromSM;
	}

	public void setFromSM(SootMethod fromSM) {
		this.fromSM = fromSM;
	}

	public Unit getFromU() {
		return fromU;
	}

	public void setFromU(Unit fromU) {
		this.fromU = fromU;
	}

	public int getInstruction() {
		return instruction;
	}

	public void setInstruction(int instruction) {
		this.instruction = instruction;
	}

	public String getExit_kind() {
		return exit_kind;
	}

	public void setExit_kind(String exit_kind) {
		this.exit_kind = exit_kind;
	}

	public String getDestinationC() {
		return destinationC;
	}

	public void setDestinationC(String destinationC) {
		this.destinationC = destinationC;
	}

	public List<Integer> getInstructions() {
		return instructions;
	}

	public void setInstructions(List<Integer> instructions) {
		this.instructions = instructions;
	}

	public Chain<Unit> getUnits() {
		return units;
	}

	public void setUnits(Chain<Unit> units) {
		this.units = units;
	}
}
