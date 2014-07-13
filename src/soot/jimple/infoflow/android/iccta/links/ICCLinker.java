package soot.jimple.infoflow.android.iccta.links;

import java.util.List;

import soot.jimple.infoflow.android.iccta.db.DB;
import soot.jimple.infoflow.android.iccta.stat.ComponentDB;
import soot.jimple.infoflow.android.iccta.stat.IntentDB;
import soot.jimple.infoflow.android.iccta.stat.IntentFilterDB;
import soot.jimple.infoflow.android.iccta.stat.LinkDB;
import soot.jimple.infoflow.android.iccta.stat.LinkDBHelper;
import soot.jimple.infoflow.android.iccta.stat.ProviderLinkDB;
import soot.jimple.infoflow.android.iccta.stat.StatDBHelper;
import soot.jimple.infoflow.android.iccta.util.Constants;
import soot.jimple.infoflow.android.iccta.util.StringUtil;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

/**
 * Building the links after IC3 is run.
 * 
 * @author li.li
 *
 */
public class ICCLinker 
{	
	public static void main(String[] args) 
	{
		DB.setJdbcPath("res/jdbc.xml");

		String apkPath = args[0];
		
		try 
		{
			ProcessManifest processMan = new ProcessManifest(apkPath);
			String pkgName = processMan.getPackageName();
			
			buildLinks(pkgName);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	public static void buildLinks(String pkgName)
	{
		try 
		{
			List<ComponentDB> allCompDBs = StatDBHelper.fetchComponents(null);
			List<ComponentDB> targetCompDBs = StatDBHelper.fetchComponents(pkgName);
			
			for (ComponentDB src : targetCompDBs)
			{
				//System.out.println(src);
				
				//for Explicit links
				buildExplicitLinks(src);
				
				for (ComponentDB dest : allCompDBs)
				{
					//for Implicit links
					buildImplicitLinks(src, dest);
					buildImplicitLinks(dest, src);
					
					
					//for Provider links
					if (dest.getType().equals("p"))
					{
						buildProviderLinks(src, dest);
					}
					
					if (src.getType().equals("p"))
					{
						buildProviderLinks(dest, src);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void setMatchLevel(int level)
	{
		LinkDBHelper.INTENT_MATCH_LEVEL = level;
	}

	public static void buildExplicitLinks(ComponentDB comp)
	{
		List<IntentDB> intentDBs = comp.getIntents();
		
		//explicit Intent, the intentType is equal to 5
		for (IntentDB intentDB : intentDBs)
		{
			if (! intentDB.isImplicit())    
			{
				List<Integer> componentIds = LinkDBHelper.getComponentIds(intentDB.getDestCompName());
				
				for (int id : componentIds)
				{
					LinkDB linkDB = new LinkDB();
					linkDB.setIntent_id(intentDB.getIntentid());
					linkDB.setComponent_id(id);
					linkDB.setType(getIntentLinkType(intentDB));
					
					if (LinkDBHelper.existLink(linkDB.getIntent_id(), linkDB.getComponent_id()))
					{
						System.out.println("link from intent id " + linkDB.getIntent_id() + " to component id " + linkDB.getComponent_id() + " is already exist.");
						continue;
					}
					
					try {
						System.out.println("link from intent id " + linkDB.getIntent_id() + " to component id " + linkDB.getComponent_id());
						DB.executeUpdate(Constants.TABLE_NAME_LINKS, linkDB, Constants.DB_NAME);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void buildImplicitLinks(ComponentDB src, ComponentDB dest)
	{
		if (src.getClsName().equals(dest.getClsName()) && src.getAppName().equals(dest.getAppName()))
		{
			return;
		}
		
		List<IntentDB> intentDBs = src.getIntents();
		List<IntentFilterDB> filterDBs = dest.getIntentFilters();
		
		for (IntentDB intentDB : intentDBs)
		{
			if (! intentDB.isImplicit())
			{
				continue;
			}
			
			for (IntentFilterDB filterDB : filterDBs)
			{
				if (LinkDBHelper.match(intentDB, filterDB))
				{
					LinkDB linkDB = new LinkDB();
					linkDB.setIntent_id(intentDB.getIntentid());
					linkDB.setComponent_id(dest.getComponentId());
					
					int intentType = getIntentLinkType(intentDB);
					int intentFilterType = getIntentFilterLinkType(filterDB);
					linkDB.setType(intentType > intentFilterType ? intentType : intentFilterType);
					
					if (LinkDBHelper.existLink(linkDB.getIntent_id(), linkDB.getComponent_id()))
					{
						System.out.println("link from intent id " + linkDB.getIntent_id() + " to component id " + linkDB.getComponent_id() + " is already exist.");
						continue;
					}
					
					try {
						System.out.println("link from intent id " + linkDB.getIntent_id() + " to component id " + linkDB.getComponent_id());
						DB.executeUpdate(Constants.TABLE_NAME_LINKS, linkDB, Constants.DB_NAME);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static void buildProviderLinks(ComponentDB src, ComponentDB dest)
	{
		if (src.getClsName().equals(dest.getClsName()) && src.getAppName().equals(dest.getAppName()))
		{
			return;
		}
		
		if (! StringUtil.isEmpty(dest.getContentProviderAuthority()))
		{
			for (String uri : src.getContentResolverURIs())
			{
				if (uri.contains("://"))
				{
					int startPos = uri.indexOf(":") + 3;
					int endPos = uri.indexOf("/", startPos);
					
					String authority = uri.substring(startPos, endPos);
					
					System.out.println(authority);
					
					if (authority.equals(dest.getContentProviderAuthority()))
					{
						if (LinkDBHelper.existProviderLink(src.getComponentId(), dest.getComponentId()))
						{
							System.out.println("link from intent id " + src.getComponentId() + " to component id " + dest.getComponentId() + " is already exist.");
						}
						else
						{
							ProviderLinkDB pLinkDB = new ProviderLinkDB();
							pLinkDB.setSrc_component_id(src.getComponentId());
							pLinkDB.setDest_component_id(dest.getComponentId());
							try 
							{
								DB.executeUpdate(Constants.TABLE_NAME_PROVIDER_LINKS, pLinkDB, Constants.DB_NAME);
							} 
							catch (Exception e) {
								e.printStackTrace();
							}
							
							System.out.println("link from component id " + src.getComponentId() + " to component id " + dest.getComponentId());
						}
					}
				}
			}
		}
	}
	
	public static int getIntentLinkType(IntentDB intent)
	{
		int rtVal = 0;
		
		if (intent.getDataAndType().containingNothing())
		{
			rtVal = 0;
		}
		else if (intent.getDataAndType().containingOnlyMimeType())
		{
			rtVal = 1;
		}
		else
		{
			rtVal = 2;
		}
		
		return rtVal;
	}
	
	public static int getIntentFilterLinkType(IntentFilterDB intentFilter)
	{
		int rtVal = 0;
		
		if (intentFilter.getDataAndType().containingNothing())
		{
			rtVal = 0;
		}
		else if (intentFilter.getDataAndType().containingOnlyMimeType())
		{
			rtVal = 1;
		}
		else
		{
			rtVal = 2;
		}
		
		return rtVal;
	}
}
