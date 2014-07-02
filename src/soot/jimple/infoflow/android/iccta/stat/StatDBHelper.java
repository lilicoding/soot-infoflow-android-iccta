package soot.jimple.infoflow.android.iccta.stat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import soot.jimple.infoflow.android.iccta.db.DB;
import soot.jimple.infoflow.android.iccta.db.DBAdapter;
import soot.jimple.infoflow.android.iccta.util.Constants;

@SuppressWarnings("unchecked")
public class StatDBHelper 
{
	public static void main(String[] args)
	{
		DB.setJdbcPath("res/jdbc.xml");
		//com.example.icctest
		List<ComponentDB> compDBs = fetchComponents(null);
		for (ComponentDB comp : compDBs)
		{
			System.out.println(comp);
		}
	}
	
	
	public static List<ComponentDB> fetchComponents(String pkgName)
	{
		List<ComponentDB> rtVal = null;
		
		//String sql = "select distinct c.id, a.app, b.class, c.exported, c.permission from Applications a, Classes b, Components c where a.id = b.app_id and b.id = c.class_id and a.app=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<ComponentDB> components = new ArrayList<ComponentDB>();
				
				try
				{
					while (rs.next())
					{
						ComponentDB compDB = new ComponentDB();
						
						int id = rs.getInt(1);
						compDB.setComponentId(id);
						
						compDB.setAppName(rs.getString(2));
						compDB.setClsName(rs.getString(3));
						
						compDB.setExported(rs.getBoolean(4));
						
						int permId = rs.getInt(5);
						if (0 != permId)
						{
							String permission = fetchComponentProtectedPermission(id);
							compDB.setProtectedPermission(permission);
						}
						
						List<IntentDB> intents = fetchIntents(id);
						compDB.setIntents(intents);
						
						List<IntentFilterDB> intentFilters = fetchIntentFilters(id);
						compDB.setIntentFilters(intentFilters);
						
						components.add(compDB);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return components;
			}
		};
		
		try 
		{
			String sql = "select distinct c.id, a.app, b.class, c.exported, c.permission from Applications a, Classes b, Components c where a.id = b.app_id and b.id = c.class_id";
			
			if (null != pkgName)
			{
				sql = sql + " and a.app=?";
				rtVal = (List<ComponentDB>) adapter.executeQuery(sql, new Object[] {pkgName}, Constants.DB_NAME);
			}
			else
			{
				rtVal = (List<ComponentDB>) adapter.executeQuery(sql, new Object[] {}, Constants.DB_NAME);
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static String fetchComponentProtectedPermission(int componentId)
	{
		String rtVal = null;
		
		String sql = "select distinct b.st from Components a, PermissionStrings b where a.permission = b.id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				String permission = null;
				try
				{
					while (rs.next())
					{
						permission = rs.getString(1);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return permission;
			}
		};
		
		try 
		{
			rtVal = (String) adapter.executeQuery(sql, new Object[] {componentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static List<IntentFilterDB> fetchIntentFilters(int componentId)
	{
		List<IntentFilterDB> rtVal = null;
		
		String sql = "select distinct d.id, a.app, b.class from Applications a, Classes b, Components c, IntentFilters d where a.id = b.app_id and b.id = c.class_id and b.id = c.class_id and c.id = d.component_id and c.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<IntentFilterDB> intentFilters = new ArrayList<IntentFilterDB>();
				
				try
				{
					while (rs.next())
					{
						IntentFilterDB ifDB = new IntentFilterDB();
						
						int id = rs.getInt(1);
						ifDB.setIntentFilterId(id);
						
						ifDB.setAppName(rs.getString(2));
						ifDB.setClsName(rs.getString(3));
						
						List<String> actions = fetchIntentFilterActions(id);
						ifDB.setActions(actions);
						
						List<String> categories = fetchIntentFilterCategories(id);
						ifDB.setCategories(categories);
						
						DataAndType dataAndType = fetchIntentFilterDataAndType(id);
						ifDB.setDataAndType(dataAndType);
						
						intentFilters.add(ifDB);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return intentFilters;
			}
		};
		
		try 
		{
			rtVal = (List<IntentFilterDB>) adapter.executeQuery(sql, new Object[] {componentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static List<String> fetchIntentFilterActions(int intentFilterId)
	{
		List<String> rtVal = null;
		
		String sql = "select distinct c.st from IntentFilters a, IFActions b, ActionStrings c where a.id = b.filter_id and b.action = c.id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<String> strs = new ArrayList<String>();
				
				try
				{
					while (rs.next())
					{
						String action = rs.getString(1);
						strs.add(action);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return strs;
			}
		};
		
		try 
		{
			rtVal = (List<String>) adapter.executeQuery(sql, new Object[] {intentFilterId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static List<String> fetchIntentFilterCategories(int intentFilterId)
	{
		List<String> rtVal = null;
		
		String sql = "select distinct c.st from IntentFilters a, IFCategories b, CategoryStrings c where a.id = b.filter_id and b.category = c.id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<String> strs = new ArrayList<String>();
				
				try
				{
					while (rs.next())
					{
						String category = rs.getString(1);
						strs.add(category);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return strs;
			}
		};
		
		try 
		{
			rtVal = (List<String>) adapter.executeQuery(sql, new Object[] {intentFilterId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static DataAndType fetchIntentFilterDataAndType(int intentFilterId)
	{
		DataAndType dataAndType = null;
		
		String type = "select distinct b.type, b.subtype, b.scheme, b.host, b.port, b.path from IntentFilters a, IFData b where a.id = b.filter_id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				DataAndType dt = new DataAndType();
				
				try
				{
					while (rs.next())
					{
						dt.setType(rs.getString(1));
						dt.setSubtype(rs.getString(2));
						dt.setScheme(rs.getString(3));
						dt.setHost(rs.getString(4));
						dt.setPort(rs.getString(5));
						dt.setPath(rs.getString(6));
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return dt;
			}
		};
		
		try 
		{
			dataAndType = (DataAndType) adapter.executeQuery(type, new Object[] {intentFilterId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return dataAndType;
	}
	
	public static List<IntentDB> fetchIntents(int componentId)
	{
		List<IntentDB> rtVal = null;
		
		String sql = "select distinct e.id, a.app, b.class, d.method, d.instruction, e.implicit from Applications a, Classes b, Components c, ExitPoints d, Intents e where a.id = b.app_id and b.id = c.class_id and b.id = d.class_id and d.id = e.exit_id and c.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<IntentDB> intents = new ArrayList<IntentDB>();
				
				try
				{
					while (rs.next())
					{
						IntentDB intentDB = new IntentDB();
						
						int id = rs.getInt(1);
						intentDB.setIntentid(id);
						
						intentDB.setAppName(rs.getString(2));
						intentDB.setClsName(rs.getString(3));
						intentDB.setMethod(rs.getString(4));
						intentDB.setInstruction(rs.getInt(5));
						intentDB.setImplicit(rs.getBoolean(6));
						
						if (! intentDB.isImplicit())
						{
							String destCompName = fetchIntentDestComponentName(id);
							intentDB.setDestCompName(destCompName);
						}
						else
						{
							String action = fetchIntentAction(id);
							intentDB.setAction(action);
							
							List<String> strs = StatDBHelper.fetchIntentCategories(id);
							intentDB.setCategories(strs);
							
							strs = StatDBHelper.fetchIntentExtras(id);
							intentDB.setExtras(strs);
							
							DataAndType dt = StatDBHelper.fetchIntentDataAndType(id);
							intentDB.setDataAndType(dt);
						}

						intents.add(intentDB);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return intents;
			}
		};
		
		try 
		{
			rtVal = (List<IntentDB>) adapter.executeQuery(sql, new Object[] {componentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static String fetchIntentDestComponentName(int intentId)
	{
		String rtVal = null;
		
		String sql = "select distinct b.class from Intents a, IClasses b where a.id = b.intent_id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				String compName = null;
				try
				{
					System.out.println("");
					
					while (rs.next())
					{
						compName = rs.getString(1);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				if (compName == null)
				{
					return compName;
				}
				
				
				return compName.replace("/", ".");
			}
		};
		
		try 
		{
			rtVal = (String) adapter.executeQuery(sql, new Object[] {intentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static String fetchIntentAction(int intentId)
	{
		String rtVal = null;
		
		String sql = "select distinct c.st from Intents a, IActions b, ActionStrings c where a.id = b.intent_id and b.action = c.id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				String action = null;
				try
				{
					while (rs.next())
					{
						action = rs.getString(1);
					}
				}			catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return action;
			}
		};
		
		try 
		{
			rtVal = (String) adapter.executeQuery(sql, new Object[] {intentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static List<String> fetchIntentCategories(int intentId)
	{
		List<String> rtVal = null;
		
		String sql = "select distinct c.st from Intents a, ICategories b, CategoryStrings c where a.id = b.intent_id and b.category = c.id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<String> strs = new ArrayList<String>();
				
				try
				{
					while (rs.next())
					{
						String category = rs.getString(1);
						strs.add(category);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return strs;
			}
		};
		
		try 
		{
			rtVal = (List<String>) adapter.executeQuery(sql, new Object[] {intentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static DataAndType fetchIntentDataAndType(int intentId)
	{
		DataAndType dataAndType = new DataAndType();
		
		String type = "select distinct b.type, b.subtype from Intents a, IMimeTypes b where a.id = b.intent_id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			DataAndType dt = null;
			
			public DBAdapter init(DataAndType dt)
			{
				this.dt = dt;
				return this;
			}

			@Override
			protected Object processResultSet(ResultSet rs)
			{
				try
				{
					while (rs.next())
					{
						dt.setType(rs.getString(1));
						dt.setSubtype(rs.getString(2));
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return dt;
			}
		}.init(dataAndType);
		
		try 
		{
			dataAndType = (DataAndType) adapter.executeQuery(type, new Object[] {intentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		
		adapter = new DBAdapter() 
		{
			DataAndType dt = null;
			
			public DBAdapter init(DataAndType dt)
			{
				this.dt = dt;
				return this;
			}

			@Override
			protected Object processResultSet(ResultSet rs)
			{
				try
				{
					while (rs.next())
					{
						dt.setScheme(rs.getString(1));
						dt.setSsp(rs.getString(2));
						dt.setUri(rs.getString(3));
						dt.setPath(rs.getString(4));
						dt.setQuery(rs.getString(5));
						dt.setAuthority(rs.getString(6));
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return dt;
			}
		}.init(dataAndType);
		
		String data = "select distinct c.scheme, c.ssp, c.uri, c.path, c.query, c.authority from Intents a, IData b, UriData c where a.id = b.intent_id and b.data = c.id and a.id=?";
		
		try 
		{
			dataAndType = (DataAndType) adapter.executeQuery(data, new Object[] {intentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return dataAndType;
	}
	
	public static List<String> fetchIntentExtras(int intentId)
	{
		List<String> rtVal = null;
		
		String sql = "select distinct b.extra from Intents a, IExtras b where a.id = b.intent_id and a.id=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<String> strs = new ArrayList<String>();
				
				try
				{
					while (rs.next())
					{
						String extra = rs.getString(1);
						strs.add(extra);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return strs;
			}
		};
		
		try 
		{
			rtVal = (List<String>) adapter.executeQuery(sql, new Object[] {intentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	
	/*
	public List<Intent> fetchIntent(String pkgName)
	{
		//String sql = "select distinct a.app, b.class, i.st, j.st, d.id from Applications a, Classes b, ExitPoints c, Intents d, IActions e, ICategories f, ActionStrings i, CategoryStrings j where a.id=b.app_id and b.id=c.class_id and c.id=d.exit_id and d.implicit=1 and d.id = e.intent_id and d.id = f.intent_id and e.action=i.id and f.category=j.id and d.id not in (select intent_id from IMimeTypes ) and d.id not in (select intent_id from IData ) and a.id <= 3000; "; 
		
		
		String sql = "select distinct a.app, b.class, c.method, c.instruction, i.st, j.st,  from Applications a, Classes b, ExitPoints c, Intents d, IActions e, ICategories f, ActionStrings i, CategoryStrings j, IData k, UriData l";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<Intent> intents = new ArrayList<Intent>();
				try 
				{
					while (rs.next())
					{
						
						IntentDB intentDB = new IntentDB();
						
						intentDB.appName = rs.getString(1);
						intentDB.clsName = rs.getString(2);
						intentDB.action = rs.getString(3);
						intentDB.category = rs.getString(4);
						
						intentDB.intentid = rs.getString(5);
						
						Intent i = intentDB.toIntent();
						
						intents.add(i);
						
						intentsMap.put(i, intentDB.clsName + ":" + intentDB.appName + ":" + intentDB.intentid);
						}
						
					}
				catch (SQLException ex)
				{
					ex.printStackTrace();
				}
				
				return intents;
			}
		};
		
		List<Intent> intents = null;
		
		try 
		{
			intents = (List<Intent>) adapter.executeQuery(sql, new Object[] {}, "cc");
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		
		return intents;
	}*/
}
