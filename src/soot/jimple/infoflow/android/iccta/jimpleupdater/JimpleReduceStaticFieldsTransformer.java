package soot.jimple.infoflow.android.iccta.jimpleupdater;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.util.Chain;

public class JimpleReduceStaticFieldsTransformer implements JimpleUpdater
{
	public Map<String, List<SootField>> classesContainStatic = new HashMap<String, List<SootField>>();
	
	@Override
	public void updateJimple() 
	{
		reduceStaticField();
	}
	
	public void reduceStaticField()
	{
		extractClassesContainStatic();
		System.out.println("ReduceStaticField: " + classesContainStatic.size() + " classes, " + getFieldsNumber() + " static fields.");
		createNonParameterConstruct();
		createStaticHelperClass();
		degradeStaticField();
		updateStaticFieldReference();
	}
	
	public void extractClassesContainStatic()
	{
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		
		for (Iterator<SootClass> iter = sootClasses.snapshotIterator(); iter.hasNext(); )
		{
			SootClass sc = iter.next();
			
			if (sc.toString().startsWith("android.support"))
			{
				continue;
			}
			
			Chain<SootField> fields = sc.getFields();
			Iterator<SootField> fieldIter = fields.snapshotIterator();
			while (fieldIter.hasNext())
			{
				SootField sf = fieldIter.next();
				if (sf.isStatic())
				{
					put(sc.getName(), sf);
				}
			}
			
			/*
			List<SootMethod> sms = sc.getMethods();
			for (SootMethod sm : sms)
			{
				try
				{
					Body b = sm.retrieveActiveBody();
					System.out.println(b);
				}
				catch (Exception ex)
				{
					
				}
			}*/
		}
	}
	
	public void createNonParameterConstruct()
	{
		for (String cls : classesContainStatic.keySet())
		{
			SootClass sc = Scene.v().getSootClass(cls);
			
			boolean noConstructMethod = true;
			boolean existNonParameterConstructMethod = false;
			
			List<SootMethod> methods = sc.getMethods();
			for (SootMethod sm : methods)
			{
				String methodName = sm.getName();
				
				if (methodName.equals("<init>"))
				{
					noConstructMethod = false;
					
					if (0 == sm.getParameterCount())
					{
						existNonParameterConstructMethod = true;
					}
				}
			}
			
			//Exist construct methods but all of them containing at least one parameter
			//So we need to create a default non parameter construct method for this class
			if (! noConstructMethod && !existNonParameterConstructMethod)
			{
				SootMethod npc = new SootMethod("<init>", 
		    			new ArrayList<Type>(), 
		    			VoidType.v(), 
		    			Modifier.PUBLIC);
		    	JimpleBody body = Jimple.v().newBody(npc);
		    	npc.setActiveBody(body);
		    	sc.addMethod(npc);
		    	
		    	{
		    		LocalGenerator lg = new LocalGenerator(body);
		            Local thisLocal = lg.generateLocal(sc.getType());
		            Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
		                    Jimple.v().newThisRef(sc.getType()));
		            body.getUnits().add(thisU);
		            
		            SootClass supperC = sc.getSuperclass();
		            InvokeExpr expr = Jimple.v().newSpecialInvokeExpr(thisLocal, supperC.getMethod("<init>", new ArrayList<Type>()).makeRef());
		            Unit specialCallU = Jimple.v().newInvokeStmt(expr);
		            body.getUnits().add(specialCallU);
		            
		            Unit returnVoidU = Jimple.v().newReturnVoidStmt();
		            body.getUnits().add(returnVoidU);
		            
		            System.out.println("Create non parameter construct method: " + body);
		    	}
			}
		}
	}
	
	public void createStaticHelperClass()
	{
		SootClass staticHelper = new SootClass("StaticHelper");
		staticHelper.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        Scene.v().addClass(staticHelper);
        
        int m = Modifier.PUBLIC | Modifier.STATIC;
        SootField sf = new SootField("instance", staticHelper.getType(), m);
        staticHelper.addField(sf);
        
        for (String cls : classesContainStatic.keySet())
		{
			SootClass sc = Scene.v().getSootClass(cls);
			
			m = Modifier.PUBLIC;
	        sf = new SootField(sc.getName(), sc.getType(), m);
	        staticHelper.addField(sf);
		}
        
        SootMethod npc = new SootMethod("<init>", 
    			new ArrayList<Type>(), 
    			VoidType.v(), 
    			Modifier.PUBLIC);
    	Body body = Jimple.v().newBody(npc);
    	npc.setActiveBody(body);
    	staticHelper.addMethod(npc);
    	
    	//fill the context of the new created non parameter construct
    	{
    		LocalGenerator lg = new LocalGenerator(body);
            Local thisLocal = lg.generateLocal(staticHelper.getType());
            Unit thisU = Jimple.v().newIdentityStmt(thisLocal, 
                    Jimple.v().newThisRef(staticHelper.getType()));
            body.getUnits().add(thisU);
            
            for (String cls : classesContainStatic.keySet())
    		{
    			SootClass sc = Scene.v().getSootClass(cls);
    			
    			sf = staticHelper.getField(sc.getName(), sc.getType());
    			
    			Local sfLocal = lg.generateLocal(sc.getType());
    			Unit sfLocalAssignU = Jimple.v().newAssignStmt(
    	                sfLocal, 
    	                Jimple.v().newNewExpr(sc.getType()));
    			Unit callInitU = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(sfLocal, npc.makeRef()));
    			
    			Unit assignSfU = Jimple.v().newAssignStmt(
    					Jimple.v().newInstanceFieldRef(
    							thisLocal,
    							sf.makeRef()), sfLocal);
    			
    			body.getUnits().add(sfLocalAssignU);
    			body.getUnits().add(callInitU);
    			body.getUnits().add(assignSfU);
    		}
            
            Unit returnVoidU = Jimple.v().newReturnVoidStmt();
            body.getUnits().add(returnVoidU);
    	}
    	
    	body.validate();
    	System.out.println(body);
    	
    	SootMethod npsc = new SootMethod("<cinit>", 
    			new ArrayList<Type>(), 
    			VoidType.v(), 
    			Modifier.PUBLIC | Modifier.STATIC);
    	body = Jimple.v().newBody(npsc);
    	npsc.setActiveBody(body);
    	staticHelper.addMethod(npsc);
    	{
    		LocalGenerator lg = new LocalGenerator(body);
    		Local sfLocal = lg.generateLocal(staticHelper.getType());
			Unit sfLocalAssignU = Jimple.v().newAssignStmt(
	                sfLocal, 
	                Jimple.v().newNewExpr(staticHelper.getType()));
			Unit callInitU = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(sfLocal, npc.makeRef()));
			Unit returnVoidU = Jimple.v().newReturnVoidStmt();
			
			body.getUnits().add(sfLocalAssignU);
			body.getUnits().add(callInitU);
			body.getUnits().add(returnVoidU);
    	}
    	
    	body.validate();
    	System.out.println(body);
	}
	
	public void degradeStaticField()
	{
		for (String clsName : classesContainStatic.keySet())
		{
			List<SootField> sfs = classesContainStatic.get(clsName);
			for (SootField sf : sfs)
			{
				if (! sf.isStatic())
				{
					return;
				}
				
				sf.setModifiers(Modifier.PUBLIC);
				
				/*
				Chain<SootClass> classes = Scene.v().getApplicationClasses();
				
				for (Iterator<SootClass> iter = classes.snapshotIterator(); iter.hasNext(); )
				{
					SootClass sc = iter.next();

					List<SootMethod> methods = sc.getMethods();
					for (SootMethod m : methods)
					{
						Body b = null;
						
						try
						{
							b = m.retrieveActiveBody();
						}
						catch (Exception ex)
						{
							continue;
						}
						
						PatchingChain<Unit> units = b.getUnits();
						for (Iterator<Unit> it = units.snapshotIterator(); it.hasNext(); )
						{
							Stmt stmt = (Stmt) it.next();

							if (stmt.containsFieldRef())
							{
								FieldRef fieldRef = stmt.getFieldRef();
								
								if (fieldRef.toString().equals(sf.toString()))
								{
									fieldRef.setFieldRef(sf.makeRef());
								}
							}
						}
					}
				}*/
			}
		}
	}
	
	public void updateStaticFieldReference()
	{
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		
		for (Iterator<SootClass> iter = sootClasses.snapshotIterator(); iter.hasNext(); )
		{
			SootClass sc = iter.next();

			if (sc.toString().startsWith("android.support") || sc.getName().equals("dummyMainClass"))
			{
				continue;
			}
			
			List<SootMethod> sms = sc.getMethods();
			for (SootMethod sm : sms)
			{
				Body b = null;
				try
				{
					b = sm.retrieveActiveBody();
				}
				catch (Exception ex)
				{
					continue;
				}
				
				for (Iterator<Unit> unitIter = b.getUnits().snapshotIterator(); unitIter.hasNext(); )
				{
					Stmt stmt = (Stmt) unitIter.next();
					if (stmt.containsFieldRef())
					{	
						if (stmt instanceof AssignStmt)
						{
							AssignStmt aStmt = (AssignStmt) stmt;
							FieldRef fr = null;
							SootField sf = null;
							String fieldSignature = "";
							
							if (aStmt.getLeftOp() instanceof FieldRef)
							{
								fr = (FieldRef) aStmt.getLeftOp();
								fieldSignature = fr.toString();
							}
							else
							{
								fr = (FieldRef) aStmt.getRightOp();
								fieldSignature = fr.toString();
							}
							
							try
							{
								String clsName = fieldSignature.split(":")[0].replace("<", "").trim();
								SootClass cls = Scene.v().getSootClass(clsName);
								sf = cls.getField(fieldSignature.split(":")[1].trim().replace(">", ""));
							}
							catch (Exception ex)
							{
								continue;
							}
							
							List<SootField> sfs = classesContainStatic.get(sf.getDeclaringClass().getName());
							if (null == sfs)
							{
								continue;
							}
							
							SootClass staticHelper = Scene.v().getSootClass("StaticHelper");
							for (SootField field : sfs)
							{
								if (field.toString().equals(sf.toString()))
								{
									replaceSootField(staticHelper, b, aStmt, sf);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void replaceSootField(SootClass sc, Body b, AssignStmt aStmt, SootField sf)
	{
		SootClass sfClass = sf.getDeclaringClass();
		
		LocalGenerator lg = new LocalGenerator(b);
		Local sfLocal = lg.generateLocal(sc.getType());
		Unit sfLocalAssignU = Jimple.v().newAssignStmt(
                sfLocal, 
                Jimple.v().newStaticFieldRef(sc.getField("instance", sc.getType()).makeRef()));
		
		Local sfLocal2 = lg.generateLocal(sfClass.getType());
		Unit sfLocalAssignU2 = Jimple.v().newAssignStmt(
                sfLocal2, 
                Jimple.v().newInstanceFieldRef(sfLocal, sc.getField(sfClass.getName(), sfClass.getType()).makeRef()));

		Unit assignU = null;
		
		if (aStmt.getLeftOp() instanceof FieldRef)
		{
			assignU = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(sfLocal2, sf.makeRef()), aStmt.getRightOp());
		}
		else
		{
			assignU = Jimple.v().newAssignStmt(aStmt.getLeftOp(), Jimple.v().newInstanceFieldRef(sfLocal2, sf.makeRef()));
		}
		
		b.getUnits().insertBefore(sfLocalAssignU, aStmt);
		b.getUnits().insertBefore(sfLocalAssignU2, aStmt);
		b.getUnits().insertBefore(assignU, aStmt);
		b.getUnits().remove(aStmt);
		
		System.out.println(b);
	}
	
	public void put(String clsName, SootField sf)
	{
		if (classesContainStatic.containsKey(clsName))
		{
			List<SootField> objs = classesContainStatic.get(clsName);
			objs.add(sf);
			classesContainStatic.put(clsName, objs);
		}
		else
		{
			List<SootField> objs = new ArrayList<SootField>();
			objs.add(sf);
			classesContainStatic.put(clsName, objs);
		}
	}
	
	public void output()
	{
		for (Entry<String, List<SootField>> entry : classesContainStatic.entrySet())
		{
			String key = entry.getKey();
			List<SootField> objs = entry.getValue();
			
			System.out.println(key);
			for (SootField obj : objs)
			{
				System.out.println(obj);
			}
		}
	}
	
	public int getFieldsNumber()
	{
		int rtVal = 0;
		for (Entry<String, List<SootField>> entry : classesContainStatic.entrySet())
		{
			List<SootField> objs = entry.getValue();
			
			rtVal += objs.size();
		}
		
		return rtVal;
	}
}