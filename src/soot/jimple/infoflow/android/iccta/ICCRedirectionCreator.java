 package soot.jimple.infoflow.android.iccta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.NullType;
import soot.PointsToSet;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.iccta.icc.ICCLink;
import soot.jimple.internal.JVirtualInvokeExpr;

public class ICCRedirectionCreator {

    private static int num = 0;
    
    private static SootClass ipcSC = null;
    private static String ipcSCClassName = "IpcSC";
    private static RefType INTENT_TYPE = RefType.v("android.content.Intent");
    private static RefType IBINDER_TYPE = RefType.v("android.os.IBinder");
    
    //dest to SootMethod is not good since one dest can have multiple redirect method
    //private Map<String, SootMethod> destinations2SootMethod = new HashMap<String, SootMethod>();
    private Map<String, SootMethod> source2RedirectMethod = new HashMap<String, SootMethod>();
    private Map<String, SootClass> destination2sootClass = new HashMap<String, SootClass>();
    
    private static ICCRedirectionCreator s = null;
    private ICCRedirectionCreator () {}
    
    public static ICCRedirectionCreator v() {
        if (s == null) {
            s = new ICCRedirectionCreator();
            ipcSC = new SootClass(ipcSCClassName);
            ipcSC.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
            
            ipcSC.setPhantom(false);
            ipcSC.setApplicationClass();
            ipcSC.setInScene(true);

            //Scene.v().addClass(ipcSC);
        }
        return s;
    }
    
    
    public void redirectToDestination(ICCLink link)
    {
    	if (Scene.v().getSootClass(link.getDestinationC()).isPhantom())
    	{
    		return;
    	}
    	
    	//1) generate redirect method
    	SootMethod redirectSM = ICCRedirectionCreator.v().getRedirectMethod(link);
    	
    	//2) instrument the source to call the generated redirect method after ICC methods
    	ICCInstrumentSource.v().instrumentSource(link, redirectSM);
    }
    
    /**
     * Redirect ICC call at unit in sm to the right component
     * 
     * 
     * 
     * @param link
     * @return
     */
    public SootMethod getRedirectMethod(ICCLink link)
    {	 	
    	if (! destination2sootClass.keySet().contains(link.getDestinationC())) {
    		SootClass destinationSC = null;
    		
    		if (link.getExit_kind().equals(IccTAConstants.ContentProviderExitKind))
    		{
    			destinationSC = ICCInstrumentDestination.v().instrumentDestinationForContentProvider(link.getDestinationC());
    		}
    		else
    		{
    			destinationSC = ICCInstrumentDestination.v().instrumentDestination(link.getDestinationC());
    		}
            
        	//build up destination2sootClass map
            destination2sootClass.put(link.getDestinationC(), destinationSC);
            
    	}
    	
    	SootClass instrumentedDestinationSC = destination2sootClass.get(link.getDestinationC());

        if (! source2RedirectMethod.containsKey(link.toString()))
        {
            //build up source2RedirectMethod map
            String source = link.toString();
            SootMethod redirectMethod = null;
            Stmt stmt = (Stmt) link.getFromU();
            String exitKind = link.getExit_kind();
            
            // deal with Content Provider
            if (IccTAConstants.ContentProviderExitKind.equals(exitKind))
            {
            	redirectMethod = generateRedirectMethodForContentProvider(stmt, instrumentedDestinationSC);
            }
            else if (stmt.containsInvokeExpr())
            {
                if (stmt.getInvokeExpr().getMethod().getName().equals("startActivityForResult"))
                {
                	//{sc} maybe is the super class of the real class
                	
                	/*
                	ValueBox vb = (ValueBox) stmt.getUseBoxes().get(0);
                	
                	Chain<Local> locals = link.getFromSM().getActiveBody().getLocals();
                	
                	SootClass sc = null;
                	
                	for (Iterator<Local> iter = locals.snapshotIterator(); iter.hasNext(); )
                	{
                		Local l = iter.next();
                		if (l.equivTo(vb.getValue()))
                		{
                			System.out.println(l.getType());
                			sc = Scene.v().getSootClass(l.getType().toString());
                			break;
                		}
                		
                	}*/
                	
                	JVirtualInvokeExpr jviExpr = (JVirtualInvokeExpr) stmt.getInvokeExprBox().getValue();
                	String clsName = jviExpr.getBase().getType().toString();
                	SootClass sc = Scene.v().getSootClass(clsName);
                    //SootClass sc = stmt.getInvokeExpr().getMethod().getDeclaringClass();
                    redirectMethod = generateRedirectMethodForStartActivityForResult(sc, instrumentedDestinationSC);
                }
                else if (stmt.getInvokeExpr().getMethod().getName().equals("bindService"))
                {
                	Value v = stmt.getInvokeExpr().getArg(1);
                	
                	SootClass sc = Scene.v().getSootClass(v.getType().toString());           	
                	redirectMethod = generateRedirectMethodForBindService(sc, instrumentedDestinationSC);
                }
                //else if (stmt.getInvokeExpr().getMethod().getName().equals("startActivity"))
                //{
                //    redirectMethod = generateRedirectMethodForStartActivity(instrumentedDestinationSC);
                //}
                else
                {
                    redirectMethod = generateRedirectMethod(instrumentedDestinationSC);
                }
            }

            if (redirectMethod == null)
            {
                throw new RuntimeException("wrong ICCLink [" + link.toString() + "]");
            }

            source2RedirectMethod.put(source, redirectMethod);
        }

    	return source2RedirectMethod.get(link.toString());
    }
    

    public SootMethod generateRedirectMethodForStartActivityForResult(SootClass originActivity, SootClass destComp)
    {
    	String newSM_name = "redirector" + num++;
    	
        List<Type> newSM_parameters = new ArrayList<Type>();
        newSM_parameters.add(originActivity.getType());
        newSM_parameters.add(INTENT_TYPE);
        Type newSM_return_type = VoidType.v();
        int modifiers = Modifier.STATIC | Modifier.PUBLIC;
                
        SootMethod newSM = new SootMethod(newSM_name, newSM_parameters, newSM_return_type, modifiers);
        ipcSC.addMethod(newSM);
        JimpleBody b = Jimple.v().newBody(newSM);
        newSM.setActiveBody(b);
        
        LocalGenerator lg = new LocalGenerator(b);
    	
        Local originActivityParameterLocal = lg.generateLocal(originActivity.getType());
        Unit originActivityParameterU = Jimple.v().newIdentityStmt(
        		originActivityParameterLocal, 
        		Jimple.v().newParameterRef(originActivity.getType(), 0));
        
        Local intentParameterLocal = lg.generateLocal(INTENT_TYPE);
        Unit intentParameterU = Jimple.v().newIdentityStmt(
                intentParameterLocal,
                Jimple.v().newParameterRef(INTENT_TYPE, 1));
        
        // new dest component
        Local destCompLocal = lg.generateLocal(destComp.getType());
        Unit newU = (Unit) Jimple.v().newAssignStmt(destCompLocal, 
                Jimple.v().newNewExpr(destComp.getType())
                );
        
        //call <init> method
        List<Type> parameters = new ArrayList<Type>();
        parameters.add(INTENT_TYPE);
        SootMethod method = destComp.getMethod("<init>", parameters, VoidType.v());
        List<Value> args = new ArrayList<Value>();
        args.add(intentParameterLocal);
        Unit initU = (Unit) Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(destCompLocal, method.makeRef(), args));
        
        List<SootMethod> sms = destComp.getMethods();
        for (SootMethod sm : sms)
        {
        	System.out.println(sm);
        }
        
        // call onCreate
        method = destComp.getMethodByName(ICCDummyMainCreator.DUMMY_MAIN_METHOD);
        InvokeExpr invoke = Jimple.v().newVirtualInvokeExpr(destCompLocal, method.makeRef());
        Unit callU = (Unit) Jimple.v().newInvokeStmt(invoke);
        
        
        //call sc.getIntentForActivityResult
        Local arIntentLocal = lg.generateLocal(INTENT_TYPE);
        Unit nullarIntentLocalParamU = (Unit) Jimple.v().newAssignStmt(
        		arIntentLocal, NullConstant.v());
        method = destComp.getMethodByName("getIntentForActivityResult");
        invoke = Jimple.v().newVirtualInvokeExpr(destCompLocal, method.makeRef());
        Unit destCompCallU = (Unit) Jimple.v().newAssignStmt(arIntentLocal, invoke);
        
        //some apps do not have an onActivityResult method even they use startActivityForResult to communicate with other components.
        try
        {
        	method = originActivity.getMethodByName("onActivityResult");
        }
        catch (Exception ex)
        {
        	method = generateFakeOnActivityResult(originActivity);
        }
        
        Local iLocal1 = lg.generateLocal(IntType.v());
        Local iLocal2 = lg.generateLocal(IntType.v());
        Unit defaultValueParamU1 = (Unit) Jimple.v().newAssignStmt(iLocal1, IntConstant.v(-1));
        Unit defaultValueParamU2 = (Unit) Jimple.v().newAssignStmt(iLocal2, IntConstant.v(-1));
        args = new ArrayList<Value>();
        args.add(iLocal1);
        args.add(iLocal2);
        args.add(arIntentLocal);
        invoke = Jimple.v().newVirtualInvokeExpr(originActivityParameterLocal, method.makeRef(), args);
        Unit onActivityResultCall = (Unit) Jimple.v().newInvokeStmt(invoke);
        
        b.getUnits().add(originActivityParameterU);
        b.getUnits().add(intentParameterU);
        b.getUnits().add(newU);
        b.getUnits().add(initU);
        //b.getUnits().add(nullParamU);
        b.getUnits().add(callU);
        b.getUnits().add(nullarIntentLocalParamU);
        b.getUnits().add(destCompCallU); 
        b.getUnits().add(defaultValueParamU1);
        b.getUnits().add(defaultValueParamU2);
        b.getUnits().add(onActivityResultCall);
        b.getUnits().add(Jimple.v().newReturnVoidStmt());
        
        System.out.println("new lifecypcle method: "+ newSM +" body: "+ newSM.retrieveActiveBody());
        
    	return newSM;
    }
    
    public SootMethod generateRedirectMethod(SootClass wrapper) {
        System.out.println("create method to call wrapper class: "+ wrapper);
        String newSM_name = "redirector" + num++;
        List<Type> newSM_parameters = new ArrayList<Type>();
        newSM_parameters.add(INTENT_TYPE);
        Type newSM_return_type = VoidType.v();
        int modifiers = Modifier.STATIC | Modifier.PUBLIC;
                
        SootMethod newSM = new SootMethod(newSM_name, newSM_parameters, newSM_return_type, modifiers);
        ipcSC.addMethod(newSM);
        JimpleBody b = Jimple.v().newBody(newSM);
        newSM.setActiveBody(b);
        
        LocalGenerator lg = new LocalGenerator(b);
        
        // identity
        Local intentParameterLocal = lg.generateLocal(INTENT_TYPE);
        Unit intentParameterU = Jimple.v().newIdentityStmt(
                intentParameterLocal,
                Jimple.v().newParameterRef(INTENT_TYPE, 0));
        
        // new
        Local al = lg.generateLocal(wrapper.getType());
        Unit newU = (Unit) Jimple.v().newAssignStmt(al, 
                Jimple.v().newNewExpr(wrapper.getType())
                );
        // init
        List<Type> parameters = new ArrayList<Type>();
        parameters.add(INTENT_TYPE);
        SootMethod method = wrapper.getMethod("<init>", parameters, VoidType.v());
        List<Value> args = new ArrayList<Value>();
        args.add(intentParameterLocal);
        Unit initU = (Unit) Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(al, method.makeRef(), args));
        
        // call dummyMainMethod
        method = wrapper.getMethodByName(ICCDummyMainCreator.DUMMY_MAIN_METHOD);
        //args = new ArrayList<Value>();
        //Local pLocal = lg.generateLocal(RefType.v("android.os.Bundle"));
        //Unit nullParamU = (Unit) Jimple.v().newAssignStmt(pLocal, NullConstant.v());
        //args.add(pLocal);
        InvokeExpr invoke = Jimple.v().newVirtualInvokeExpr(al, method.makeRef());
        Unit callU = (Unit) Jimple.v().newInvokeStmt(invoke);
        
        b.getUnits().add(intentParameterU);
        b.getUnits().add(newU);
        b.getUnits().add(initU);
        //b.getUnits().add(nullParamU);
        b.getUnits().add(callU);
        b.getUnits().add(Jimple.v().newReturnVoidStmt());
        
        System.out.println("new lifecypcle method: "+ newSM +" body: "+ newSM.retrieveActiveBody());
        
        return newSM;
        
    }
   
    public SootMethod generateRedirectMethodForStartActivity(SootClass wrapper) {
        System.out.println("create method to call wrapper class: "+ wrapper);
        String newSM_name = "redirector" + num++;
        List<Type> newSM_parameters = new ArrayList<Type>();
        newSM_parameters.add(INTENT_TYPE);
        Type newSM_return_type = VoidType.v();
        int modifiers = Modifier.STATIC | Modifier.PUBLIC;

        SootMethod newSM = new SootMethod(newSM_name, newSM_parameters, newSM_return_type, modifiers);
        ipcSC.addMethod(newSM);
        JimpleBody b = Jimple.v().newBody(newSM);
        newSM.setActiveBody(b);

        LocalGenerator lg = new LocalGenerator(b);

        // identity
        Local intentParameterLocal = lg.generateLocal(INTENT_TYPE);
        Unit intentParameterU = Jimple.v().newIdentityStmt(
                intentParameterLocal,
                Jimple.v().newParameterRef(INTENT_TYPE, 0));

        // new
        Local al = lg.generateLocal(wrapper.getType());
        Unit newU = (Unit) Jimple.v().newAssignStmt(al,
                Jimple.v().newNewExpr(wrapper.getType())
                );
        // init
        List<Type> parameters = new ArrayList<Type>();
        parameters.add(INTENT_TYPE);
        SootMethod method = wrapper.getMethod("<init>", parameters, VoidType.v());
        List<Value> args = new ArrayList<Value>();
        args.add(intentParameterLocal);
        Unit initU = (Unit) Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(al, method.makeRef(), args));

        // call dummyMainMethod
        //method = wrapper.getMethodByName(ICCDummyMainCreator.DUMMY_MAIN_METHOD);
        method = wrapper.getMethodByName("onCreate");
        args = new ArrayList<Value>();
        Local pLocal = lg.generateLocal(RefType.v("android.os.Bundle"));
        Unit nullParamU = (Unit) Jimple.v().newAssignStmt(pLocal, NullConstant.v());
        args.add(pLocal);
        InvokeExpr invoke = Jimple.v().newVirtualInvokeExpr(al, method.makeRef(), args);
        Unit callU = (Unit) Jimple.v().newInvokeStmt(invoke);

        b.getUnits().add(intentParameterU);
        b.getUnits().add(newU);
        b.getUnits().add(initU);
        b.getUnits().add(nullParamU);
        b.getUnits().add(callU);
        b.getUnits().add(Jimple.v().newReturnVoidStmt());

        System.out.println("new lifecypcle method: "+ newSM +" body: "+ newSM.retrieveActiveBody());

        return newSM;

    }
    
    public SootMethod generateRedirectMethodForBindService(SootClass serviceConnection, SootClass destComp)
    {
    	String newSM_name = "redirector" + num++;
    	
        List<Type> newSM_parameters = new ArrayList<Type>();
        newSM_parameters.add(serviceConnection.getType());
        newSM_parameters.add(INTENT_TYPE);
        Type newSM_return_type = VoidType.v();
        int modifiers = Modifier.STATIC | Modifier.PUBLIC;
                
        SootMethod newSM = new SootMethod(newSM_name, newSM_parameters, newSM_return_type, modifiers);
        ipcSC.addMethod(newSM);
        JimpleBody b = Jimple.v().newBody(newSM);
        newSM.setActiveBody(b);
        
        LocalGenerator lg = new LocalGenerator(b);
    	
        Local originActivityParameterLocal = lg.generateLocal(serviceConnection.getType());
        Unit originActivityParameterU = Jimple.v().newIdentityStmt(
        		originActivityParameterLocal, 
        		Jimple.v().newParameterRef(serviceConnection.getType(), 0));
        
        Local intentParameterLocal = lg.generateLocal(INTENT_TYPE);
        Unit intentParameterU = Jimple.v().newIdentityStmt(
                intentParameterLocal,
                Jimple.v().newParameterRef(INTENT_TYPE, 1));
        
        // new dest component
        Local destCompLocal = lg.generateLocal(destComp.getType());
        Unit newU = (Unit) Jimple.v().newAssignStmt(destCompLocal, 
                Jimple.v().newNewExpr(destComp.getType())
                );
        
        //call <init> method
        List<Type> parameters = new ArrayList<Type>();
        parameters.add(INTENT_TYPE);
        SootMethod method = destComp.getMethod("<init>", parameters, VoidType.v());
        List<Value> args = new ArrayList<Value>();
        args.add(intentParameterLocal);
        Unit initU = (Unit) Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(destCompLocal, method.makeRef(), args));
        
        List<SootMethod> sms = destComp.getMethods();
        for (SootMethod sm : sms)
        {
        	System.out.println(sm);
        }
        
        // call dummy main method
        method = destComp.getMethodByName(ICCDummyMainCreator.DUMMY_MAIN_METHOD);
        InvokeExpr invoke = Jimple.v().newVirtualInvokeExpr(destCompLocal, method.makeRef());
        Unit callU = (Unit) Jimple.v().newInvokeStmt(invoke);
        
        //call dest.getIBinderForIpc
        
        method = destComp.getMethodByName("getIBinderForIpc");
        Type binderType = method.getReturnType();
        Local ibinderLocal = lg.generateLocal(binderType);
        Unit nullarIntentLocalParamU = (Unit) Jimple.v().newAssignStmt(
        		ibinderLocal, NullConstant.v());
        invoke = Jimple.v().newVirtualInvokeExpr(destCompLocal, method.makeRef());
        Unit destCompCallU = (Unit) Jimple.v().newAssignStmt(ibinderLocal, invoke);
        
        //anonymous inner class problem, cannot get correct stmt
        method = serviceConnection.getMethodByName("onServiceConnected");
        
        changeParameterType(method, IBINDER_TYPE, binderType);
        
        Local iLocal1 = lg.generateLocal(NullType.v());
        Unit defaultValueParamU1 = (Unit) Jimple.v().newAssignStmt(iLocal1, NullConstant.v());
        args = new ArrayList<Value>();
        args.add(iLocal1);
        args.add(ibinderLocal);
        
        PointsToSet pointsToSet = Scene.v().getPointsToAnalysis().reachingObjects(originActivityParameterLocal);
        
        System.out.println(pointsToSet.possibleClassConstants());
        System.out.println(pointsToSet.possibleStringConstants());
        System.out.println(pointsToSet.possibleTypes());
        
        SootClass sc = Scene.v().getSootClass(originActivityParameterLocal.getType().toString());
        if (sc.isInterface())
        {
        	invoke = Jimple.v().newInterfaceInvokeExpr(originActivityParameterLocal, method.makeRef(), args);
        }
        else
        {
        	invoke = Jimple.v().newVirtualInvokeExpr(originActivityParameterLocal, method.makeRef(), args); 
        }
        
        Unit onActivityResultCall = (Unit) Jimple.v().newInvokeStmt(invoke);
        
        b.getUnits().add(originActivityParameterU);
        b.getUnits().add(intentParameterU);
        b.getUnits().add(newU);
        b.getUnits().add(initU);
        //b.getUnits().add(nullParamU);
        b.getUnits().add(callU);
        b.getUnits().add(nullarIntentLocalParamU);
        b.getUnits().add(destCompCallU); 
        b.getUnits().add(defaultValueParamU1);
        b.getUnits().add(onActivityResultCall);
        b.getUnits().add(Jimple.v().newReturnVoidStmt());
        
        System.out.println("new lifecypcle method: "+ newSM +" body: "+ newSM.retrieveActiveBody());
        
    	return newSM;
    }
    
    public void changeParameterType(SootMethod sm, Type originalType, Type newType)
    {
    	List<Type> newTypes = new ArrayList<Type>();
    	List<Type> types = sm.getParameterTypes();
    	for (int i = 0; i < types.size(); i++)
    	{
    		Type t = types.get(i);
    		if (t.equals(originalType))
    		{
    			newTypes.add(newType);
    		}
    		else
    		{
    			newTypes.add(t);
    		}
    	}
    	
    	sm.setParameterTypes(newTypes);
    }
    
    public SootMethod generateFakeOnActivityResult(SootClass sootClass)
    {
    	List<Type> parameters = new ArrayList<Type>();
    	parameters.add(IntType.v());
    	parameters.add(IntType.v());
    	parameters.add(INTENT_TYPE);
        SootMethod newOnActivityResult = new SootMethod("onActivityResult", 
        		parameters, 
        		VoidType.v(), 
        		Modifier.PUBLIC);
        sootClass.addMethod(newOnActivityResult);
        
        Body b = Jimple.v().newBody(newOnActivityResult);
        
        //generate identityStmt
        LocalGenerator lg = new LocalGenerator(b);
        
        //this
        Local thisLocal = lg.generateLocal(sootClass.getType());
        Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
                Jimple.v().newThisRef(sootClass.getType()));
        
        //parameter1
        Local int1ParameterLocal = lg.generateLocal(IntType.v());
        Unit int1ParameterU = Jimple.v().newIdentityStmt(int1ParameterLocal, Jimple.v().newParameterRef(IntType.v(), 0));
        
        //parameter2
        Local int2ParameterLocal = lg.generateLocal(IntType.v());
        Unit int2ParameterU = Jimple.v().newIdentityStmt(int2ParameterLocal, Jimple.v().newParameterRef(IntType.v(), 1));
        
        
        
        //parameter
        Type intentType = RefType.v("android.content.Intent");
        Local intentParameterLocal = lg.generateLocal(intentType);
        Unit intentParameterU = Jimple.v().newIdentityStmt(intentParameterLocal, Jimple.v().newParameterRef(intentType, 2));
           
        //return
        Unit returnU = Jimple.v().newReturnVoidStmt();
        
        b.getUnits().add(thisU);
        b.getUnits().add(int1ParameterU);
        b.getUnits().add(int2ParameterU);
        b.getUnits().add(intentParameterU);
        b.getUnits().add(returnU);

        newOnActivityResult.setActiveBody(b);
        return newOnActivityResult;
    }

	public SootMethod generateRedirectMethodForContentProvider(Stmt iccStmt, SootClass destProvider) {
	    System.out.println("create method to call dest class: "+ destProvider);
	    
	    SootMethod iccMethod = iccStmt.getInvokeExpr().getMethod();
	    
	    String newSM_name = "redirector" + num++;
	    List<Type> newSM_parameters = iccMethod.getParameterTypes();
	    Type newSM_return_type = iccMethod.getReturnType();
	    int modifiers = Modifier.STATIC | Modifier.PUBLIC;
	            
	    SootMethod newSM = new SootMethod(newSM_name, newSM_parameters, newSM_return_type, modifiers);
	    ipcSC.addMethod(newSM);
	    JimpleBody b = Jimple.v().newBody(newSM);
	    newSM.setActiveBody(b);
	    
	    LocalGenerator lg = new LocalGenerator(b);
	    
	    // all parameters
	    List<Unit> units = new ArrayList<Unit>();
	    List<Local> locals = new ArrayList<Local>();
	    for (int i = 0; i < newSM_parameters.size(); i++)
	    {
	    	Type type = newSM_parameters.get(i);
	    	Local local = lg.generateLocal(type);
	    	Unit localU = Jimple.v().newIdentityStmt(local, Jimple.v().newParameterRef(type, i));
	    	
	    	locals.add(local);
	    	units.add(localU);
	    }
	    
	    // new
	    Local al = lg.generateLocal(destProvider.getType());
	    Unit newU = (Unit) Jimple.v().newAssignStmt(al, 
	            Jimple.v().newNewExpr(destProvider.getType())
	            );
	    
	    // init
	    List<Type> parameters = new ArrayList<Type>();
	    List<Value> args = new ArrayList<Value>();
	    SootMethod method = destProvider.getMethod("<init>", parameters, VoidType.v());
	    Unit initU = (Unit) Jimple.v().newInvokeStmt(
	            Jimple.v().newSpecialInvokeExpr(al, method.makeRef(), args));
	    
	    Local rtLocal = lg.generateLocal(newSM_return_type);
	    
	    
	    // call related method and assign the result to return local, may optimize it to dummyMain method as well
	    parameters = iccMethod.getParameterTypes();
	    method = destProvider.getMethodByName(iccMethod.getName());
        InvokeExpr invoke = Jimple.v().newVirtualInvokeExpr(al, method.makeRef(), locals);
        //Unit callU = (Unit) Jimple.v().newInvokeStmt(invoke);
        Unit assignU = (Unit) Jimple.v().newAssignStmt(rtLocal, invoke);
        
        // return statement
        Unit returnU = (Unit) Jimple.v().newReturnStmt(rtLocal);
	    
        for (Unit unit : units)
        {
        	b.getUnits().add(unit);
        }
	    b.getUnits().add(newU);
	    b.getUnits().add(initU);
	    b.getUnits().add(assignU);
	    b.getUnits().add(returnU);
	    
	    System.out.println("new lifecypcle method: "+ newSM +" body: "+ newSM.retrieveActiveBody());
	    
	    return newSM;
	    
	}
}
