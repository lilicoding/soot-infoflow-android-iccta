package soot.jimple.infoflow.android.iccta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.util.Chain;


/**
 * One ICC Link contain one source component and one destination component.
 * this class is used to collect all the assist methods which instrument destination component.
 *
 */
public class ICCInstrumentDestination 
{
	private static ICCInstrumentDestination s = null;
	private ICCInstrumentDestination () {}
    public static ICCInstrumentDestination v() 
    {
        if (s == null) 
        {
            s = new ICCInstrumentDestination();
        }
        return s;
    }
	
    private static RefType INTENT_TYPE = RefType.v("android.content.Intent");
    private static RefType IBINDER_TYPE = RefType.v("android.os.IBinder");
    public static final String FIELD_INTENT_FOR_IPC = "intent_for_ipc";
    public static final String FIELD_INTENT_FOR_ACTIVITY_RESULT = "intent_for_activity_result";
    public static final String FIELD_IBINDER_FOR_IPC = "ibinder_for_ipc";
    
    //call this method for all your need to instrument the destination class
    public SootClass instrumentDestination(String destination)
    {
    	System.out.println("instrument destination class for "+ destination);
        SootClass sc = Scene.v().getSootClass(destination);
        
        SootField intent_for_ipc = generateIntentFieldForIpc(sc);
        SootField intent_for_ar = generateIntentFieldForActivityResult(sc);
        
        
        generateInitMethod(sc, intent_for_ipc);
        generateGetIntentMethod(sc, intent_for_ipc);
        generateGetIntentForActivityResultMethod(sc, intent_for_ar);
        
        
        //set the intent which will be transfered back to original call component
        //to intent_for_activity_result field
        List<SootMethod> sms = sc.getMethods();
        for (SootMethod sm : sms)
        {
        	instrumentSetIntentARStmt(sm, intent_for_ar);
        }
        
        //generate dummy main method
        generateDummyMainMethod(destination);
        
        instrumentDummyMainMethod(sc, intent_for_ipc);
        
        if (isService(sc) && containOnBindMethod(sc))
        {
        	Type binderType = extractBinderType(sc);
        	SootField ibinder_for_ipc = generateFieldForIBinder(sc, binderType);
        	generateGetIBinderMethod(sc, ibinder_for_ipc, binderType);
        	instrumentOnBindMethod(sc, ibinder_for_ipc);
        }

        for (SootMethod sm: sc.getMethods())
        {
        	try
        	{
        		//abstract and native method do not have a body inside.
        		if (sm.isAbstract() || sm.isNative())
        		{
        			continue;
        		}
        		
        		System.out.println("wrapper method "+ sm +" \n"+ sm.retrieveActiveBody());
        	}
        	catch (Exception ex)
        	{
        		System.out.println("Exception: to retrieve activi body for " + sm.getSignature());
        	}
        }
            
        
        return sc;
    }
    
    public SootClass instrumentDestinationForContentProvider(String destination)
    {
    	return Scene.v().getSootClass(destination);
    }
    
    private boolean containOnBindMethod(SootClass sc)
    {
    	String onBind = "onBind";
    	List<SootMethod> sms = sc.getMethods();
    	for (SootMethod sm : sms)
    	{
    		if (sm.getName().equals(onBind))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean isService(SootClass sootClass)
    {
    	String baseClassName = "android.app.Service";
    	
    	if (sootClass.toString().equals(baseClassName))
    	{
    		return true;
    	}
    	
    	while (sootClass.hasSuperclass())
    	{
    		sootClass = sootClass.getSuperclass();
    		
    		if (sootClass.toString().equals(baseClassName))
        	{
        		return true;
        	}
    	}
    	
    	return false;
    }
    
    /**
     * create a field intent_for_ipc in compSootClass
     * 
     * @param compSootClass 
     * @return
     */
    public SootField generateIntentFieldForIpc(SootClass compSootClass)
    {
    	int m = Modifier.PUBLIC | Modifier.STATIC;
        SootField sf = new SootField(FIELD_INTENT_FOR_IPC, INTENT_TYPE, m);
        compSootClass.addField(sf);
        
        return sf;
    }
    
    /**
     * create a field intent_for_activity_result in compSootClass
     * since setResult(int, Intent) will be used to transfer intent back to 
     * source component which use startActivityResult to link this component.
     * we use this field to store the special intent set by setResult()
     * 
     * @param compSootClass
     * @return
     */
    public SootField generateIntentFieldForActivityResult(SootClass compSootClass)
    {
    	int m = Modifier.PUBLIC | Modifier.STATIC;
        SootField sf = new SootField(FIELD_INTENT_FOR_ACTIVITY_RESULT, INTENT_TYPE, m);
        compSootClass.addField(sf);
        
        return sf;
    }
    
    public SootField generateFieldForIBinder(SootClass compSootClass, Type binderType)
    {
    	int m = Modifier.PUBLIC | Modifier.STATIC;
        SootField sf = new SootField(FIELD_IBINDER_FOR_IPC, binderType, m);
        compSootClass.addField(sf);
        
        return sf;
    }
    
    /**
     * generate construct method for this component, this construct method should take
     * one Intent as their parameter.
     * 
     * the best way is to use a place holder parameter to avoid exist construct method and 
     * use exist construct to build the new <init> method.
     * 
     * @param compSootClass
     * @param intentSootField
     * @return
     */
    public SootMethod generateInitMethod(SootClass compSootClass, SootField intentSootField)
    {
    	String name = "<init>";
        List<Type> parameters = new ArrayList<Type>();
        parameters.add(INTENT_TYPE);
        Type returnType = VoidType.v();
        int modifiers = Modifier.PUBLIC;
        SootMethod newConstructor = new SootMethod(name, parameters, returnType, modifiers);
        compSootClass.addMethod(newConstructor);
        {
        Body b = Jimple.v().newBody(newConstructor);
        newConstructor.setActiveBody(b);
        LocalGenerator lg = new LocalGenerator(b);
        Local thisLocal = lg.generateLocal(compSootClass.getType());
        Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
                Jimple.v().newThisRef(compSootClass.getType()));
        Local intentParameterLocal = lg.generateLocal(INTENT_TYPE);
        Unit intentParameterU = Jimple.v().newIdentityStmt(
                intentParameterLocal,
                Jimple.v().newParameterRef(INTENT_TYPE, 0));
        
        boolean noDefaultConstructMethod = false;
        Unit superU = null;
        try
        {
        	superU = (Unit) Jimple.v().newInvokeStmt(
                Jimple.v().newSpecialInvokeExpr(thisLocal, 
            		compSootClass.getMethod(name, new ArrayList<Type>(), VoidType.v()).makeRef())
                    );
        }
        catch (Exception ex)
        {
        	//It is possible that a class doesn't have a default construct method (<init>()).
            noDefaultConstructMethod = true;
        }
        
        if (noDefaultConstructMethod)
        {
        	List<SootMethod> sootMethods = compSootClass.getMethods();
        	for (SootMethod sm : sootMethods)
        	{
        		if (sm.getName().equals("<init>"))
        		{
        			if (sm.getParameterCount() == 1 && sm.getParameterType(0).equals(INTENT_TYPE))
        			{
        				List<Value> args = new ArrayList<Value>();
        				args.add(intentParameterLocal);
        				superU = (Unit) Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(thisLocal, sm.makeRef(), args));
        				continue;
        			}
        			
        			List<Value> args = new ArrayList<Value>();
        			for (int i = 0; i < sm.getParameterCount(); i++)
        			{
        				args.add(NullConstant.v());
        			}
        			
        			superU = (Unit) Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(thisLocal, sm.makeRef(), args));
        			break;
        		}
        	}
        }
        
        Unit storeIntentU = Jimple.v().newAssignStmt(
                Jimple.v().newStaticFieldRef(intentSootField.makeRef()), 
                intentParameterLocal);
        b.getUnits().add(thisU);
        b.getUnits().add(intentParameterU);
        b.getUnits().add(superU);
        b.getUnits().add(storeIntentU);
        b.getUnits().add(Jimple.v().newReturnVoidStmt());
        }
        
        return newConstructor;
    }
    
    /**
     * Override getIntent method to be able to transfer the intent to the destination component
     * 
     * @param compSootClass
     * @param intentSootField
     * @return
     */
    public SootMethod generateGetIntentMethod(SootClass compSootClass, SootField intentSootField)
    {
        String name = "getIntent";
        List<Type> parameters = new ArrayList<Type>();
        Type returnType = INTENT_TYPE;
        int modifiers = Modifier.PUBLIC;
        SootMethod newGetIntent = new SootMethod(name, parameters, returnType, modifiers);
        compSootClass.addMethod(newGetIntent);
        {
        Body b = Jimple.v().newBody(newGetIntent);
        newGetIntent.setActiveBody(b);
        LocalGenerator lg = new LocalGenerator(b);
        Local thisLocal = lg.generateLocal(compSootClass.getType());
        Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
                Jimple.v().newThisRef(compSootClass.getType()));
        Local intentParameterLocal = lg.generateLocal(INTENT_TYPE);
        Unit getIntentU = Jimple.v().newAssignStmt(                 
                intentParameterLocal,
                Jimple.v().newStaticFieldRef(intentSootField.makeRef()));
        Unit returnU = Jimple.v().newReturnStmt(intentParameterLocal);
        b.getUnits().add(thisU);
        b.getUnits().add(getIntentU);
        b.getUnits().add(returnU);
        }
        
        return newGetIntent;
    }
    
    /**
     * create getIntentForActivityResult method to be able to transfer the intent back to source component
     * 
     * @param compSootClass
     * @param intentSootField
     * @return
     */
    public SootMethod generateGetIntentForActivityResultMethod(SootClass compSootClass, SootField intentSootField)
    {
        String name = "getIntentForActivityResult";
        List<Type> parameters = new ArrayList<Type>();
        Type returnType = INTENT_TYPE;
        int modifiers = Modifier.PUBLIC;
        SootMethod newGetIntent = new SootMethod(name, parameters, returnType, modifiers);
        compSootClass.addMethod(newGetIntent);
        {
        Body b = Jimple.v().newBody(newGetIntent);
        newGetIntent.setActiveBody(b);
        LocalGenerator lg = new LocalGenerator(b);
        Local thisLocal = lg.generateLocal(compSootClass.getType());
        Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
                Jimple.v().newThisRef(compSootClass.getType()));
        Local intentParameterLocal = lg.generateLocal(INTENT_TYPE);
        Unit getIntentU = Jimple.v().newAssignStmt(                 
                intentParameterLocal,
                Jimple.v().newStaticFieldRef(intentSootField.makeRef()));
        Unit returnU = Jimple.v().newReturnStmt(intentParameterLocal);
        b.getUnits().add(thisU);
        b.getUnits().add(getIntentU);
        b.getUnits().add(returnU);
        }
        
        return newGetIntent;
    }
    
    public SootMethod generateGetIBinderMethod(SootClass compSootClass, SootField ibinderSootField, Type binderType)
    {
        String name = "getIBinderForIpc";
        List<Type> parameters = new ArrayList<Type>();
        Type returnType = binderType;
        int modifiers = Modifier.PUBLIC;
        SootMethod newGetIBinder = new SootMethod(name, parameters, returnType, modifiers);
        compSootClass.addMethod(newGetIBinder);
        {
        Body b = Jimple.v().newBody(newGetIBinder);
        newGetIBinder.setActiveBody(b);
        LocalGenerator lg = new LocalGenerator(b);
        Local thisLocal = lg.generateLocal(compSootClass.getType());
        Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
                Jimple.v().newThisRef(compSootClass.getType()));
        Local ibinderParameterLocal = lg.generateLocal(IBINDER_TYPE);
        Unit getIBinderU = Jimple.v().newAssignStmt(                 
        		ibinderParameterLocal,
                Jimple.v().newStaticFieldRef(ibinderSootField.makeRef()));
        Unit returnU = Jimple.v().newReturnStmt(ibinderParameterLocal);
        b.getUnits().add(thisU);
        b.getUnits().add(getIBinderU);
        b.getUnits().add(returnU);
        }
        
        return newGetIBinder;
    }
    
    /**
     * we should change this method to override setResult method, 
     * but Activity.setResult has been defined to final. 
     * this may doesn't matter the static analysis
     * 
     * @param sm
     * @param intentSootField
     */
    public void instrumentSetIntentARStmt(SootMethod sm, SootField intentSootField)
    {
    	Body body = null;
    	
    	//only need focus on convrete method since the other method (contain activity body) 
    	//is generated by ourself
    	if (sm.isConcrete())
    	{
    		body = sm.retrieveActiveBody();
    	}
    	else
    	{
    		return;
    	}
    	
    	PatchingChain<Unit> units = body.getUnits();
    	
    	Value intent_for_ar = null;
    	Stmt stmt_for_ar = null;
    	
    	
    	for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
    	{
    		Stmt stmt = (Stmt) iter.next();
    		if (! stmt.containsInvokeExpr())
    		{
    			continue;
    		}
    		
    		//currently, just deal with: setResult (int resultCode, Intent data)
    		//because only this method will return a Intent to origin activity
    		if (stmt.getInvokeExpr().getMethod().getName().equals("setResult"))
    		{
    			int argCount = stmt.getInvokeExpr().getArgCount();
    			if (2 == argCount)
    			{
    				intent_for_ar = stmt.getInvokeExpr().getArgs().get(1);
    				stmt_for_ar = stmt;
    				
    				Unit setIntentAR = Jimple.v().newAssignStmt(                 
    	                    Jimple.v().newStaticFieldRef(intentSootField.makeRef()), 
    	                    intent_for_ar);
    	    		//build an AssignStmt like this: intent_for_activity_result = parameter
    	    		units.insertAfter(setIntentAR, stmt_for_ar);
    	    		
    				//don't break, who knows some developer will invoke setResult more than once.  
    			}
    		}
    	}
    }
    
    
    public void instrumentDummyMainMethod(SootClass compSootClass, SootField intentSootField)
    {
    	SootMethod mainMethod = compSootClass.getMethodByName(ICCDummyMainCreator.DUMMY_MAIN_METHOD);
    	if (null == mainMethod)
    	{
    		mainMethod = generateDummyMainMethod(compSootClass.getName());
    	}
    	
    	Body body = mainMethod.getActiveBody();
    	
    	//For the purpose of confusion dex optimization (because of the strategy of generating dummyMain method)
    	boolean firstStmt = true;
    	
    	PatchingChain<Unit> units = body.getUnits();
    	for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
    	{
    		Stmt stmt = (Stmt) iter.next();
    		
    		if (stmt instanceof IdentityStmt)
    		{
    			continue;
    		}
    		   		
    		if (firstStmt)
    		{
    			firstStmt = false;
    			AssignStmt aStmt = (AssignStmt) stmt;
    			SootMethod fuzzyMe = generateFuzzyMethod(compSootClass);
    			InvokeExpr invokeExpr = Jimple.v().newVirtualInvokeExpr(body.getThisLocal(), fuzzyMe.makeRef());
    			Unit assignU = Jimple.v().newAssignStmt(aStmt.getLeftOp(), invokeExpr);
    			units.insertAfter(assignU, aStmt);
    		}
    		
    		if (! stmt.containsInvokeExpr())
    		{
    			continue;
    		}
    		
    		if (stmt.toString().contains("<init>"))
    		{
    			continue;
    		}
    			
    		List<Value> argValues = stmt.getInvokeExpr().getArgs();	
    		/*
    		for (Value value : argValues)
    		{
    			Type type = value.getType();
    			if (type.equals(INTENT_TYPE))
    			{
    				Unit setIntentU = Jimple.v().newAssignStmt(     
    						value,
    	                    Jimple.v().newStaticFieldRef(intentSootField.makeRef()));
    				
    	    		units.insertBefore(setIntentU, stmt);
    			}
    		}*/
    		
    		//Using another way to transfer Intent
    		for (int i = 0; i < argValues.size(); i++)
    		{
    			Value value = argValues.get(i);
    			Type type = value.getType();
    			if (type.equals(INTENT_TYPE))
    			{
    				assignIntent(compSootClass, stmt.getInvokeExpr().getMethod(), i+1);
    			}
    		}
    	}
    }
    
    public SootMethod generateFuzzyMethod(SootClass sootClass)
	{
    	String name = "fuzzyMe";
	    List<Type> parameters = new ArrayList<Type>();
	    Type returnType = IntType.v();
	    int modifiers = Modifier.PUBLIC;
	    SootMethod fuzzyMeMethod = new SootMethod(name, parameters, returnType, modifiers);
	    sootClass.addMethod(fuzzyMeMethod);
	    
	    {
	    	Body b = Jimple.v().newBody(fuzzyMeMethod);
	    	fuzzyMeMethod.setActiveBody(b);
	    	LocalGenerator lg = new LocalGenerator(b);
	        Local thisLocal = lg.generateLocal(sootClass.getType());
	        Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
	                Jimple.v().newThisRef(sootClass.getType()));
	        Unit returnU = Jimple.v().newReturnStmt(IntConstant.v(1));
	        b.getUnits().add(thisU);
	        b.getUnits().add(returnU);
	    }
	        
	    return fuzzyMeMethod;
	}
    
    public void assignIntent(SootClass hostComponent, SootMethod method, int indexOfArgs)
    {
    	Body body = method.getActiveBody();

    	PatchingChain<Unit> units = body.getUnits();
    	Chain<Local> locals = body.getLocals();
    	Value intentV = null;
		int identityStmtIndex = 0;
		
    	for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
    	{
    		Stmt stmt = (Stmt) iter.next();
			if (! method.isStatic())
			{
	    		if (stmt instanceof IdentityStmt)
	    		{			
	    			if (identityStmtIndex == indexOfArgs)
	    			{
	    				intentV = ((IdentityStmt) stmt).getLeftOp();
	    			}
	    			
	    			identityStmtIndex++;
	    		}
	    		else
	    		{
	    	 		Local thisLocal = locals.getFirst();
	    			
	    	 		/*
	    			Unit setIntentU = Jimple.v().newAssignStmt(     
	    					intentV,
	    					Jimple.v().newVirtualInvokeExpr(thisLocal, method.getDeclaringClass().getMethodByName("getIntent").makeRef()));
					*/
	    	 		
	    	 		/* Using the component that the dummyMain() belongs to, as in some cases the invoked method is only available in its superclass.
	    	 		 * and its superclass does not contain getIntent() and consequently cause an runtime exception of couldn't find getIntent(). 
	    	 		 * 
	    	 		 * RuntimeException: couldn't find method getIntent(*) in com.google.android.gcm.GCMBroadcastReceiver
	    	 		*/
	    	 		Unit setIntentU = Jimple.v().newAssignStmt(     
	    					intentV,
	    					Jimple.v().newVirtualInvokeExpr(thisLocal, hostComponent.getMethodByName("getIntent").makeRef()));
	    	 		
		    		units.insertBefore(setIntentU, stmt);
		    		
		    		System.out.println(body);
		    		
		    		return;
	    		}
			}
			
    		
    	}
    }
    
    public void instrumentOnBindMethod(SootClass sootClass, SootField ibinder_for_ipc)
    {
    	SootMethod onBindMethod = null;
    	try
    	{
    		onBindMethod = sootClass.getMethodByName("onBind");
    	}
    	catch (RuntimeException ex)
    	{
    	}
    	
    	
    	if (null == onBindMethod)
    	{
    		return;
    	}
    	
    	Body body = onBindMethod.retrieveActiveBody();
    	PatchingChain<Unit> units = body.getUnits();
    	for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
    	{
    		Stmt stmt = (Stmt) iter.next();
    		
    		if (stmt instanceof ReturnStmt)
    		{
    			ReturnStmt rtStmt = (ReturnStmt) stmt;
    			Value rtValue = rtStmt.getOp();
    			
    			Unit setIBinderU = Jimple.v().newAssignStmt(
    					Jimple.v().newStaticFieldRef(ibinder_for_ipc.makeRef()), 
    					rtValue);
    			
    			units.insertBefore(setIBinderU, rtStmt);
    		}
    		
    	}
    	
    	
    }
    
    /**
     * create dummyMainMethod to simulate the component's lifecycle
     * 
     * @param sootClassName
     * @return
     */
    public SootMethod generateDummyMainMethod(String sootClassName)
    {
    	return ICCDummyMainCreator.v().generateDummyMainMethod(sootClassName);
    }
    
    /**
     * To extract the real binder type,
     * Thus, a more precision way is to perform a type analysis for IBinder reference
     * 
     * @return
     */
    public Type extractBinderType(SootClass sootClass)
    {
    	SootMethod onBindMethod = null;
    	try
    	{
    		onBindMethod = sootClass.getMethodByName("onBind");
    	}
    	catch (RuntimeException ex)
    	{
    	}
    	
    	
    	if (null == onBindMethod)
    	{
    		return null;
    	}
    	
    	
    	Body body = onBindMethod.retrieveActiveBody();
    	PatchingChain<Unit> units = body.getUnits();
    	for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext(); )
    	{
    		Stmt stmt = (Stmt) iter.next();
    		
    		if (stmt instanceof ReturnStmt)
    		{
    			ReturnStmt rtStmt = (ReturnStmt) stmt;
    			Value rtValue = rtStmt.getOp();
    			
    			if (rtValue.toString().equals("null"))
    			{
    				return onBindMethod.getReturnType();
    			}
    			
    			return rtValue.getType();
    		}
    		
    	}
    	
    	return onBindMethod.getReturnType();
    }
}
