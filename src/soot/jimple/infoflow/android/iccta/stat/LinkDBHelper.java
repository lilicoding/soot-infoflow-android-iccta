package soot.jimple.infoflow.android.iccta.stat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import soot.jimple.infoflow.android.iccta.db.DB;
import soot.jimple.infoflow.android.iccta.db.DBAdapter;
import soot.jimple.infoflow.android.iccta.util.Constants;
import android.content.Intent;
import android.content.IntentFilter;

@SuppressWarnings("unchecked")
public class LinkDBHelper 
{
	public static void main(String[] args)
	{
		
		DB.setJdbcPath("res/jdbc.xml");
		//com.example.icctest
		List<ComponentDB> compDBs = StatDBHelper.fetchComponents(null);
		for (ComponentDB comp : compDBs)
		{
			System.out.println(comp);
		}
		
		buildLinks();
	}
	
	/*
	 * 1: only action and categories
	 * 2: 1 + mimetype
	 * 3: 2 + data
	 */
	public static int INTENT_MATCH_LEVEL = 3;
	
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
						linkDB.setType(5);
						
						if (existLink(linkDB.getIntent_id(), linkDB.getComponent_id()))
						{
							System.out.println("link from intent id " + linkDB.getIntent_id() + " to component id " + linkDB.getComponent_id() + " is already exist.");
							continue;
						}
						
						try {
							DB.executeUpdate(Constants.TABLE_NAME_LINKS, linkDB, Constants.DB_NAME);
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
						if (match(intentDB, filterDB) > 0)
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
								DB.executeUpdate(Constants.TABLE_NAME_LINKS, linkDB, Constants.DB_NAME);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
	}
	
	public static void setINTENT_MATCH_LEVEL(int iNTENT_MATCH_LEVEL) {
		INTENT_MATCH_LEVEL = iNTENT_MATCH_LEVEL;
	}

	public static int match(IntentDB intentDB, IntentFilterDB filterDB)
	{
		Intent intent = intentDB.toIntent(INTENT_MATCH_LEVEL);
		IntentFilter filter = filterDB.toIntentFilter(INTENT_MATCH_LEVEL);
		
		return filter.match(intent.getAction(), intent.getType(), intent.getScheme(), intent.getData(), intent.getCategories(), "IccTA");
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
}
