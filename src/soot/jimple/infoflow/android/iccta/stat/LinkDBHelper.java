package soot.jimple.infoflow.android.iccta.stat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import soot.jimple.infoflow.android.iccta.db.DBAdapter;
import soot.jimple.infoflow.android.iccta.links.DefaultMatchAlgo;
import soot.jimple.infoflow.android.iccta.util.Constants;

@SuppressWarnings("unchecked")
public class LinkDBHelper 
{

	/*
	 * 1: only action and categories
	 * 2: 1 + mimetype
	 * 3: 2 + data
	 */
	public static int INTENT_MATCH_LEVEL = 3;
	
	/*
	public static void buildLinks()
	{
		List<ComponentDB> compDBs = StatDBHelper.fetchComponents(null);
		
		for (ComponentDB comp : compDBs)
		{
			List<IntentDB> intentDBs = comp.getIntents();
			
			//explicit Intent, the intentType is equal to 5
			for (IntentDB intentDB : intentDBs)
			{
				if (! intentDB.isImplicit())    
				{
					List<Integer> componentIds = getComponentIds(intentDB.getDestCompName());
					
					for (int id : componentIds)
					{
						LinkDB linkDB = new LinkDB();
						linkDB.setIntent_id(intentDB.getIntentid());
						linkDB.setComponent_id(id);
						linkDB.setType(0);
						
						if (existLink(linkDB.getIntent_id(), linkDB.getComponent_id()))
						{
							System.out.println("link from intent id " + linkDB.getIntent_id() + " to component id " + linkDB.getComponent_id() + " is already exist.");
							continue;
						}
						
						try {
							System.out.println("link from intent id " + linkDB.getIntent_id() + " to component id " + linkDB.getComponent_id());
							//DB.executeUpdate(Constants.TABLE_NAME_LINKS, linkDB, Constants.DB_NAME);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			//only for implicit Intents
			for (ComponentDB comp2 : compDBs)
			{
				if (comp.getClsName().equals(comp2.getClsName()) && comp.getAppName().equals(comp2.getAppName()))
				{
					continue;
				}
				
				List<IntentFilterDB> filterDBs = comp2.getIntentFilters();
				
				for (IntentDB intentDB : intentDBs)
				{
					if (! intentDB.isImplicit())
					{
						continue;
					}
					
					for (IntentFilterDB filterDB : filterDBs)
					{
						if (match(intentDB, filterDB))
						{
							LinkDB linkDB = new LinkDB();
							linkDB.setIntent_id(intentDB.getIntentid());
							linkDB.setComponent_id(comp2.getComponentId());
							linkDB.setType(intentDB.getIntentType());
							
							if (existLink(linkDB.getIntent_id(), linkDB.getComponent_id()))
							{
								continue;
							}
							
							try {
								System.out.println("link from intent id " + linkDB.getIntent_id() + " to component id " + linkDB.getComponent_id());
								//DB.executeUpdate(Constants.TABLE_NAME_LINKS, linkDB, Constants.DB_NAME);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					
					if (! StringUtil.isEmpty(comp2.getContentProviderAuthority()))
					{
						for (String uri : comp.getContentResolverURIs())
						{
							if (uri.contains("://"))
							{
								int startPos = uri.indexOf(":") + 2;
								int endPos = uri.indexOf("/", startPos);
								
								String authority = uri.substring(startPos, endPos);
								
								System.out.println(authority);
								
								if (authority.equals(comp2.getContentProviderAuthority()))
								{
									System.out.println("link from component id " + comp.getClsName() + " to component id " + comp2.getClsName());
								}
							}
						}
					}
				}
			}
		}
		
	}
	*/

	public static boolean match(IntentDB intentDB, IntentFilterDB filterDB)
	{
		DefaultMatchAlgo defaultAlgo = new DefaultMatchAlgo();
		return defaultAlgo.match(intentDB, filterDB, INTENT_MATCH_LEVEL);
	}
	
	public static List<Integer> getComponentIds(String clsName)
	{
		List<Integer> rtVal = null;
		
		String sql = "select distinct b.id from Classes a, Components b where a.id = b.class_id and a.class=?";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<Integer> ids = new ArrayList<Integer>();
				
				try
				{
					while (rs.next())
					{
						int id = rs.getInt(1);
						ids.add(id);
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return ids;
			}
		};
		
		try 
		{
			rtVal = (List<Integer>) adapter.executeQuery(sql, new Object[] {clsName}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static boolean existLink(int intentId, int componentId)
	{
		boolean rtVal = false;
		
		String sql = "select distinct id from Links where intent_id=? and component_id=?;";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				boolean isExitLink = false;
				try
				{
					if (rs.next())
					{
						isExitLink = true;
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return isExitLink;
			}
		};
		
		try 
		{
			rtVal = (Boolean) adapter.executeQuery(sql, new Object[] {intentId, componentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static boolean existProviderLink(int srcComponentId, int destComponentId)
	{
		boolean rtVal = false;
		
		String sql = "select distinct id from ProviderLinks where src_component_id=? and dest_component_id=?;";
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				boolean isExitLink = false;
				try
				{
					if (rs.next())
					{
						isExitLink = true;
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
				return isExitLink;
			}
		};
		
		try 
		{
			rtVal = (Boolean) adapter.executeQuery(sql, new Object[] {srcComponentId, destComponentId}, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
}
