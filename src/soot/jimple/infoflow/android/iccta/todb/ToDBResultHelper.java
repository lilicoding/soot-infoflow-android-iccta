package soot.jimple.infoflow.android.iccta.todb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.iccta.db.DB;
import soot.jimple.infoflow.android.iccta.jimpleupdater.ExtraExtractor;
import soot.jimple.infoflow.android.iccta.util.Constants;
import soot.jimple.infoflow.android.iccta.util.ICCHelper;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class ToDBResultHelper {

	public static void write(List<List<StmtTable>> paths, String pkgName)
	{
		try
		{
			int appId = ToDBHelper.getAppId(pkgName);
			
			for (int i = 0; i < paths.size(); i++)
			{
				List<StmtTable> path = paths.get(i);
				
				PathTable pathTable = new PathTable();
				
				boolean inflow = false;
				boolean outflow = false;
				boolean icc = false;
				StringBuilder sb = new StringBuilder();
				
				for (int j = 0; j < path.size(); j++)
				{
					StmtTable stmtTable = path.get(j);
					DB.executeUpdate(Constants.TABLE_NAME_STMTS, stmtTable, Constants.DB_NAME);
					
					int id = ToDBHelper.getStmtId(stmtTable.getStmt(), stmtTable.getMethod(), stmtTable.getClass_id());
					
					if (j == 0)
					{
						sb.append(id);
					}
					else
					{
						sb.append(":" + id);
					}
					
					if (j == 0)
					{
						pathTable.setSource(id);
						inflow = stmtTable.isIcc();
					}
					else if (j == path.size()-1)
					{
						pathTable.setSink(id);
						outflow = stmtTable.isIcc();
					}
					
					if (stmtTable.getStmt().contains("IpcSC") && stmtTable.getStmt().contains("redirector"))
					{
						icc = true;
					}
				}
				
				pathTable.setPaths(sb.toString());
				
				if (inflow && outflow)
				{
					pathTable.setType(Constants.PATH_TYPE_PBCL);
				}
				else if (inflow && (!outflow))
				{
					pathTable.setType(Constants.PATH_TYPE_PPCL);
				}
				else if ((!inflow) && outflow)
				{
					pathTable.setType(Constants.PATH_TYPE_PACL);
				}
				else
				{
					pathTable.setType(Constants.PATH_TYPE_NORMAL);
				}

				if (icc)
				{
					pathTable.setIcc(1);
				}
				
				pathTable.setApp_id(appId);
				
				DB.executeUpdate(Constants.TABLE_NAME_PATHS, pathTable, Constants.DB_NAME);
				
				putIccStmt(pathTable);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void putIccStmt(PathTable pathTable) throws Exception
	{
		if (pathTable.getType().equals(Constants.PATH_TYPE_PACL) ||
			pathTable.getType().equals(Constants.PATH_TYPE_PBCL))
		{
			String sql = "select * from Stmts where id=?;";
			
			List<Object> obj = DB.executeQuery(StmtTable.class, sql, new Object[] {pathTable.getSink()}, Constants.DB_NAME);
			
			StmtTable stmtTable = (StmtTable) obj.get(0);
			
			int id = (int) ToDBHelper.getExitId(stmtTable.getClass_id(), stmtTable.getMethod(), stmtTable.getJimpleIndex());
		
			IccStmtTable iccStmtTable = new IccStmtTable();
			iccStmtTable.setExit_id(id);
			iccStmtTable.setStmt_id(pathTable.getSink());
			
			DB.executeUpdate(Constants.TABLE_NAME_ICCSTMTS, iccStmtTable, Constants.DB_NAME);
		}
	}
	
	/*
	public static void toDB(InfoflowResults results, IInfoflowCFG cfg, String pkgName) throws Exception
	{
		List<List<StmtTable>> paths = new ArrayList<List<StmtTable>>();
		
		if (results != null) 
		{

			for (ResultSinkInfo sink : results.getResults().keySet()) 
			{
				List<StmtTable> onePath = new ArrayList<StmtTable>();
				
				for (ResultSourceInfo source : results.getResults().get(sink)) 
				{
					List<Stmt> stmts = source.getPath();
					
					for (int i = 0; i < stmts.size(); i++)
					{
						Stmt stmt = stmts.get(i);
						SootMethod sm = cfg.getMethodOf(stmt);
						SootClass sc = sm.getDeclaringClass();
						
						StmtTable stmtTable = new StmtTable();
						stmtTable.setStmt(stmt.toString());
						stmtTable.setMethod(sm.getSignature());
						
						int classId = ToDBHelper.getClassId(sc.getName(), pkgName);
						stmtTable.setClass_id(classId);
						
						try
						{
							int jimpleIndex = Integer.parseInt(stmt.getTag(Constants.TAG_JIMPLE_INDEX_NUMBER).toString());
							stmtTable.setJimpleIndex(jimpleIndex);
						}
						catch (Exception ex)
						{
						}
						
						stmtTable.setIcc(false);
						
						if (i == 0)
						{
							//source
							stmtTable.setType(Constants.STMT_TYPE_SOURCE);
							if (ICCHelper.isIccStmt(stmt, true))
							{
								stmtTable.setIcc(true);
							}
						}
						else if (i == stmts.size()-1)
						{
							//sink
							stmtTable.setType(Constants.STMT_TYPE_SINK);
							if (ICCHelper.isIccStmt(stmt, false))
							{
								stmtTable.setIcc(true);
							}
						}
						else if (i == stmts.size()-2)
						{
							//Templately removing the duplicated sink stmt
							Stmt sinkStmt = stmts.get(i+1);
							if (stmt.toString().equals(sinkStmt.toString()))
							{
								continue;
							}
						}
						else
						{
							stmtTable.setType(Constants.STMT_TYPE_NORMAL);
						}
						
						onePath.add(stmtTable);
					}
				}
				
				paths.add(onePath);
			}
		}
		
		write(paths, pkgName);
	}*/

	public static void toDBForExtras() throws Exception
	{
		List<Map<String, List<String>>> maps = new ArrayList<Map<String, List<String>>>();
		maps.add(ExtraExtractor.getExtras);
		maps.add(ExtraExtractor.putExtras);
		
		for (int i = 0; i < maps.size(); i++)
		{
			for (Map.Entry<String, List<String>> entry : maps.get(i).entrySet())
			{
				String method = entry.getKey();
				List<String> value = entry.getValue();
				for (String extra : value)
				{
					ExtraTable extraTable = new ExtraTable();
					extraTable.setMethod(method);
					extraTable.setExtra(extra);
					
					if (i == 0)
					{
						extraTable.setType(Constants.EXTRA_TYPE_GET);
					}
					else
					{
						extraTable.setType(Constants.EXTRA_TYPE_PUT);
					}
					
					DB.executeUpdate(Constants.TABLE_NAME_EXTRAS, extraTable, Constants.DB_NAME);
				}
			}
		}
	}
}
