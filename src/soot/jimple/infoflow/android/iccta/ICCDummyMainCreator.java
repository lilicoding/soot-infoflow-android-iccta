 package soot.jimple.infoflow.android.iccta;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.infoflow.android.iccta.Component.ComponentType;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointConstants;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.util.Chain;


public class ICCDummyMainCreator 
{
    private static ICCDummyMainCreator s = null;
    ICCDummyMainCreator () {}
    public static ICCDummyMainCreator v() 
    {
        if (s == null) 
        {
            s = new ICCDummyMainCreator();
        
        }
        return s;
    }
    
    public static final String DUMMY_MAIN_METHOD = "dummyMainMethod";
    
    /**
     * 
     * since flowdroid already resolve this problem, 
     * they will generate a dummyMain method for all comp class.
     * but here, we want to generate dummyMain method for each comp class.
     * this is kind of special use.
     * 
     * @param sootClassName
     * @return
     */
    public SootMethod generateDummyMainMethod(String sootClassName)
    {
    	return generateDummyMainMethod(new ArrayList<String>(), sootClassName);
    }
    
    public SootMethod generateDummyMainMethod(List<String> entryPoints, String sootClassName)
    {
    	List<String> androidClasses = new ArrayList<String>();
    	androidClasses.add(sootClassName);
    	
    	SootMethod mainMethod = new SootMethod(DUMMY_MAIN_METHOD, 
    			new ArrayList<Type>(), 
    			VoidType.v(), 
    			Modifier.PUBLIC);// | Modifier.STATIC);    //no need be static
    	JimpleBody body = Jimple.v().newBody(mainMethod);
    	mainMethod.setActiveBody(body);
    	
    	SootClass compSootClass = Scene.v().getSootClass(sootClassName);
    	compSootClass.addMethod(mainMethod);
    	
    	//this is mandatory, the default dummyMainMethod is static, so they 
    	//do not deal thisIdentity. since we don't need static dummyMainMethod, 
    	//we should define it explicit
    	body.insertIdentityStmts();
    	
    	Map<String, List<String>> callbackFunctions = new HashMap<String, List<String>>();
    	callbackFunctions.put(sootClassName, getCallbackFunctions(compSootClass));
    	
    	AndroidEntryPointCreator androidEPCreator = new AndroidEntryPointCreator(androidClasses);	
    	androidEPCreator.setCallbackFunctions(callbackFunctions);
    	
    	return androidEPCreator.createDummyMain(mainMethod);
    }
    
    private List<String> getCallbackFunctions(SootClass sc)
    {
    	List<String> callbacks = new ArrayList<String>();
    	
    	for (SootMethod sm : sc.getMethods())
        {
        	//<init> and <cinit>
        	if (sm.getName().contains("init>"))
        	{
        		continue;
        	}
        	
        	ComponentType compType = Component.getComponentType(sc);
        	switch (compType)
        	{
        	case Activity:
        		if (AndroidEntryPointConstants.getActivityLifecycleMethods().contains(sm.getName()))
            	{
            		continue;
            	}
        		
        		break;
        	case Service:
        		if (AndroidEntryPointConstants.getServiceLifecycleMethods().contains(sm.getName()))
            	{
            		continue;
            	}
        		break;
        	case BroadcastReceiver:
        		if (AndroidEntryPointConstants.getBroadcastLifecycleMethods().contains(sm.getName()))
            	{
            		continue;
            	}
        		break;
        	case ContentProvider:
        		if (AndroidEntryPointConstants.getContentproviderLifecycleMethods().contains(sm.getName()))
            	{
            		continue;
            	}
        		break;
        	default:
        		break;
        	}
        	
        	if (! isPotentialCallbackMethod(sc, sm.getName()))
        	{
        		continue;
        	}
        	
        	callbacks.add(sm.getSignature());
        }
    	
    	callbacks.addAll(getAnonymousCallbacks(sc));
    	
    	return callbacks;
    }
    
    
    private List<String> getAnonymousCallbacks(SootClass sootClass)
    {
    	List<String> rtVal = new ArrayList<String>();
    	
    	try
    	{
    		String clsName = sootClass.getName();
    		
    		if (clsName.contains("$"))
    		{
    			return rtVal;
    		}
    		
    		clsName = clsName + "$";
    		
    		Chain<SootClass> scs = Scene.v().getClasses();
    		
    		for (SootClass sc : scs)
    		{
    			if (sc.getName().startsWith(clsName))
    			{
    				List<SootMethod> sms = sc.getMethods();
    				
    				for (SootMethod sm : sms)
    				{
    					if (sm.getName().contains("<init>"))
    					{
    						continue;
    					}
    					
    					if (isPotentialCallbackMethod(sc, sm.getName()))
    					{
    						System.out.println("--------------------------->" + sc.getName() +":" + sm.getName());
    						rtVal.add(sm.getSignature());
    					}
    				}
    			}
    		}
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	
    	return rtVal;
    }
    
    
    
    
    /**
     * a callback method at least is extended from its supper class
     * @param currentClass
     * @param methodName
     * @return
     */
    private boolean isPotentialCallbackMethod(SootClass currentClass, String methodName)
    {
    	boolean existCurrentMethod = false;
    	List<SootMethod> currentMethods = currentClass.getMethods();
		for (SootMethod method : currentMethods)
		{
			if (method.getName().equals(methodName))
			{
				existCurrentMethod = true;
				break;
			}
		}
		
		if (! existCurrentMethod)
		{
			throw new RuntimeException(methodName + " is not belong to class " + currentClass.getName());
		}
    	
    	List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
		for(SootClass sc : extendedClasses)
		{
			List<SootMethod> methods = sc.getMethods();
			for (SootMethod method : methods)
			{
				if (method.getName().equals(methodName))
				{
					return true;
				}
			}
		}
		
		Chain<SootClass> interfaces = currentClass.getInterfaces();
    	for (SootClass i : interfaces)
    	{
    		List<SootMethod> methods = i.getMethods();
			for (SootMethod method : methods)
			{
				if (method.getName().equals(methodName))
				{
					return true;
				}
			}
    	}
		
    	
    	return false;
    }
}
