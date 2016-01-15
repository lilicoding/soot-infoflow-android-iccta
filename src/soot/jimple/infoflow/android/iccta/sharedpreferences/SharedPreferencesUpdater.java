package soot.jimple.infoflow.android.iccta.sharedpreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.IdentityStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.iccta.jimpleupdater.JimpleUpdater;
import soot.util.Chain;

public class SharedPreferencesUpdater implements JimpleUpdater
{
	@Override
	public void updateJimple() 
	{
        Set<String> fieldNames = new HashSet<String>();
        Map<StmtWrapper, String> fieldStmts = new HashMap<StmtWrapper, String>();
        
		Map<Map<Stmt, SootMethod>, String> spMapping = extractSharedReferencesName();
		
		for (Entry<Map<Stmt, SootMethod>, String> entry : spMapping.entrySet())
		{
			Map<Stmt, SootMethod> stmtMapping = entry.getKey();
			String prefsName = entry.getValue();
			
			for (Stmt stmt : stmtMapping.keySet())
			{
				SootMethod stmtMethod = stmtMapping.get(stmt);
				
				if (stmt.containsInvokeExpr())
				{
					SootMethod sm = stmt.getInvokeExpr().getMethod();
					
					if (sm.getDeclaringClass().getName().equals("android.content.SharedPreferences"))
					{
						if (sm.getName().equals("edit"))
						{
							Value value = stmt.getUseAndDefBoxes().get(0).getValue();
							
							Map<Stmt, SootMethod> editRelatedStmtMapping = forwardValueMapping(stmtMapping.get(stmt), stmt, value);
							
							for (Stmt editStmt : editRelatedStmtMapping.keySet())
							{
								SootMethod editStmtMethod = editRelatedStmtMapping.get(editStmt);
								
								SootMethod editRelatedMethod = editStmt.getInvokeExpr().getMethod();
								if (editRelatedMethod.getDeclaringClass().getName().equals("android.content.SharedPreferences$Editor"))
								{
									if (editRelatedMethod.getName().equals("clear"))
									{
										
										fieldStmts.put(new StmtWrapper(editStmt, editStmtMethod), prefsName);
									}
									else if (editRelatedMethod.getName().equals("remove"))
									{
										fieldStmts.put(new StmtWrapper(editStmt, editStmtMethod), prefsName);
										
										String str = stmt.getInvokeExpr().getArg(0).toString();
										fieldNames.add(prefsName + "_" + str);
									}
									else if (editRelatedMethod.getName().startsWith("put"))
									{
										fieldStmts.put(new StmtWrapper(editStmt, editStmtMethod), prefsName);
										
										System.out.println(editStmt);
										System.out.println(editRelatedMethod.getName());
																			
										String str = editStmt.getInvokeExpr().getArg(0).toString();
										fieldNames.add(prefsName + "_" + str);
									}
								}
							}
						}
						else if (sm.getName().equals("getAll"))
						{
							fieldStmts.put(new StmtWrapper(stmt, stmtMethod), prefsName);
						}
						else if (sm.getName().startsWith("get"))
						{
							fieldStmts.put(new StmtWrapper(stmt, stmtMethod), prefsName);
							
							String str = stmt.getInvokeExpr().getArg(0).toString();
							fieldNames.add(prefsName + "_" + str);
						}
					}
				}
			}
		}
		
		//System.out.println(fieldNames);
		//System.out.println(fieldStmts);
		
		SharedPreferencesHelper spHelper = new SharedPreferencesHelper();
		spHelper.fieldGenerater(fieldNames);
		spHelper.fieldInstrumentation(fieldStmts);
	}

	
	public String backwardStringExtraction(SootMethod sootMethod, Stmt valueStmt, Value value)
	{
		if (value.toString().contains("\""))
		{
			return value.toString();
		}
		
		//backwardStringExtraction
		/*
		Body body = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> units = body.getUnits();
		
		List<Stmt> stmts = new ArrayList<Stmt>();
		
		boolean start = false;
		for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); )
		{
			Stmt stmt = (Stmt) unitIter.next();
			if (! stmt.equals(valueStmt))
			{
				stmts.add(stmt);
			}
		}*/
		
		
		return "";
	}
	
	public Map<Stmt, SootMethod> forwardValueMapping(SootMethod sootMethod, Stmt valueStmt, Value value)
	{
		Map<Stmt, SootMethod> stmtMapping = new HashMap<Stmt, SootMethod>();
		
		Body body = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> units = body.getUnits();
		
		boolean start = false;
		for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); )
		{
			Stmt stmt = (Stmt) unitIter.next();
			
			if (! stmt.equals(valueStmt))
			{
				continue;
			}
			else
			{
				if (unitIter.hasNext())
				{
					stmt = (Stmt) unitIter.next();
				}
				
				start = true;
			}
			
			if (start)
			{
				List<ValueBox> valueBoxes = stmt.getUseBoxes();
				for (ValueBox vb : valueBoxes)
				{
					if (value.equals(vb.getValue()))
					{
						stmtMapping.put(stmt, sootMethod);
						
						int argsIndex = getArgsIndex(stmt, value);
						if (-1 != argsIndex)
						{
							forward(stmtMapping, stmt.getInvokeExpr().getMethod(), argsIndex);
						}
					}
				}
			}
		}
		
		
		return stmtMapping;
	}
	
	public int getArgsIndex(Stmt stmt, Value value)
	{
		int argsIndex = -1;
		
		if (stmt.containsInvokeExpr())
		{
			List<Value> values = stmt.getInvokeExpr().getArgs();
			for (int i = 0; i < values.size(); i++)
			{
				if (value.equals(values.get(i)))
				{
					argsIndex = i;
					break;
				}
			}
		}
		
		return argsIndex;
	}
	
	//argsIndex starts from 0
	public void forward(Map<Stmt, SootMethod> stmtMapping, SootMethod sootMethod, int argsIndex)
	{	
		Body body = sootMethod.retrieveActiveBody();
		PatchingChain<Unit> units = body.getUnits();
		
		int index = -1;
		if (sootMethod.isStatic())
		{
			//No this statement
			index++;
		}
		
		Value value = null;
		
		for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); )
		{
			Stmt stmt = (Stmt) unitIter.next();
			
			if (index < argsIndex)
			{
				index++;
			}
			else if (index == argsIndex)
			{
				if (stmt instanceof IdentityStmt)
				{
					value = stmt.getDefBoxes().get(0).getValue();
				}
				else
				{
					throw new RuntimeException("Wrong argsIndex (" + argsIndex + ") number for IdentityStmt");
				}
				
				index++;
			}
			else
			{
				List<ValueBox> valueBoxes = stmt.getUseBoxes();
				for (ValueBox vb : valueBoxes)
				{
					if (value.equals(vb.getValue()))
					{
						stmtMapping.put(stmt, sootMethod);
						
						int newArgsIndex = getArgsIndex(stmt, value);
						if (-1 != newArgsIndex)
						{
							forward(stmtMapping, stmt.getInvokeExpr().getMethod(), newArgsIndex);
						}
					}
				}
			}
			
		}
	}
	
	public Map<Map<Stmt, SootMethod>, String> extractSharedReferencesName()
	{
		Map<Map<Stmt, SootMethod>, String> spMapping = new HashMap<Map<Stmt, SootMethod>, String>();
		
		
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		
		for (Iterator<SootClass> iter = sootClasses.snapshotIterator(); iter.hasNext(); )
		{
			SootClass sc = (SootClass) iter.next();
			
			if (sc.getName().startsWith("android.support"))
			{
				continue;
			}
			
			List<SootMethod> sootMethods = sc.getMethods();
			for (SootMethod sootMethod : sootMethods)
			{
				try
				{
					Body body = sootMethod.retrieveActiveBody();
					
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); )
					{
						Stmt stmt = (Stmt) unitIter.next();
						
						if (! stmt.containsInvokeExpr())
						{
							continue;
						}
						
						SootMethod sm = stmt.getInvokeExpr().getMethod();
						
						if (sm.getName().equals("getSharedPreferences"))
						{
							Value prefsNameValue = stmt.getInvokeExpr().getArgs().get(0);
							String prefsName = backwardStringExtraction(sootMethod, stmt, prefsNameValue);
							
							Value value = stmt.getUseAndDefBoxes().get(0).getValue();
							Map<Stmt, SootMethod> stmtMapping = forwardValueMapping(sootMethod, stmt, value);
							
							System.out.println(stmtMapping);
							spMapping.put(stmtMapping, prefsName);
						}
					}
					
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		
		return spMapping;
	}

	
}
