package soot.jimple.infoflow.android.iccta.icc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import soot.jimple.infoflow.android.iccta.db.DBAdapter;
import soot.jimple.infoflow.android.iccta.iac.IACLink;
import soot.jimple.infoflow.android.iccta.links.UnreasonableLinksRemover;
import soot.jimple.infoflow.android.iccta.util.Constants;


@SuppressWarnings("unchecked")
public class ICCLinksEpiccProvider implements IICCLinksProvider 
{
	boolean filterRemovedComponents = false;
	String removedCompsPath = "res/RemovedComponents.txt";
	public static List<String> removedComps = null;
	//static
	//{
		//ConnectionProviderManager.setJdbcPath("res/jdbc.xml");
	//}
	
	@Override
	public List<ICCLink> getICCLinks(String[] appNames) 
	{
		if (filterRemovedComponents)
		{
			return filterICCLinks(fetchICCLinks(appNames));
		}
		
		return fetchICCLinks(appNames);
	}

	public List<Integer> fetchInstructions(String SMString, String[] appNames)
	{
		String param = "?";
		String[] value = new String[appNames.length+1];
		
		value[0] = SMString;
		
		for (int i = 0; i < appNames.length; i++)
		{
			if (i == 0)
			{
				value[i+1] = appNames[i];
			}
			else
			{
				param =  param + ",?";
				value[i+1] = appNames[i];
			}
		}
		
		
		String sql = "select a.instruction from ExitPoints a, Classes b, Applications c where a.method = ? and a.class_id=b.id and b.app_id=c.id and c.app in (" + param + ") group by instruction";
		
		
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<Integer> instructions = new ArrayList<Integer>();
				try 
				{
					while (rs.next())
					{
						instructions.add(rs.getInt(1));
					}
				}
				catch (SQLException ex)
				{
					ex.printStackTrace();
				}
				
				return instructions;
			}
		};
		
		List<Integer> instructions = null;
		
		try 
		{
			instructions = (List<Integer>) adapter.executeQuery(sql, value, Constants.DB_NAME);
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return instructions;
	}
	
	//when analyzing only one application, using this method to fetch the ICC links
	/*public List<ICCLink> fetchICCLink(String appName)
	{
		String sql = "select distinct e.method, e.instruction, e.exit_kind, f.class, d.class from Links a, Intents b, Components c, Classes d, ExitPoints e, Classes f, Applications g" +
				"where a.intent_id=b.id and a.component_id=c.id and c.class_id=d.id and b.exit_id=e.id and f.id=e.class_id and " +
				"d.app_id=g.id and f.app_id=g.id and g.app=?;";
	}
	*/
	
	
	public List<ICCLink> fetchICCLinks(String[] appNames) 
	{
		String params = "?";
		String[] paramValues = new String[appNames.length*2];
		paramValues[0] = paramValues[appNames.length] = appNames[0];
		for (int i = 1; i < appNames.length; i++)
		{
			params += ",?";
			paramValues[i] = paramValues[appNames.length+i] = appNames[i];
		}
		
		String sql = "select distinct e.method, e.instruction, e.exit_kind, f.class, d.class from Links a, Intents b, Components c, Classes d, ExitPoints e, Classes f, Applications g, Applications h " +
				"where a.intent_id=b.id and a.component_id=c.id and c.class_id=d.id and b.exit_id=e.id and f.id=e.class_id and " +
				"d.app_id=g.id and f.app_id=h.id and g.app in (" + params + ") and h.app in (" + params + ")";
		
		//this is for intentMatchLevel option
		sql = sql + " and a.type <=" + Constants.INTENT_MATCH_LEVEL + ";";
		
		
		final String[] names = appNames;
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<ICCLink> links = new ArrayList<ICCLink>();
				try {
					while (rs.next())
					{
						String fromSMString = rs.getString(1);
						int instruction = rs.getInt(2);
						String exitKind = rs.getString(3);
						String destinationC = rs.getString(5);
						
						List<Integer> instructions = fetchInstructions(fromSMString, names);
						
						ICCLink link = new ICCLink(fromSMString, instruction, exitKind, destinationC, instructions);
						
						if (! UnreasonableLinksRemover.isUnreasonable(link))
						{
							links.add(link);
						}
						
					}
				} catch (SQLException e) 
				{
					e.printStackTrace();
				}
				
				return links;
			}
			
		};
		
		List<ICCLink> links = null;
		
		try 
		{
			links = (List<ICCLink>) adapter.executeQuery(sql, paramValues, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		List<ICCLink> providerLinks = fetchProviderLinks(appNames);
		links.addAll(providerLinks);
		
		return links;
	}

	public List<ICCLink> fetchProviderLinks(String[] appNames) 
	{
		String params = "?";
		String[] paramValues = new String[appNames.length*2];
		paramValues[0] = paramValues[appNames.length] = appNames[0];
		for (int i = 1; i < appNames.length; i++)
		{
			params += ",?";
			paramValues[i] = paramValues[appNames.length+i] = appNames[i];
		}
		
		String sql = "select distinct d.method, d.instruction, d.exit_kind, f.class from Applications a, Classes b, Components c, ExitPoints d, Applications e, Classes f, Components g, ProviderLinks h " +
					 "where a.id=b.app_id and b.id=c.class_id and b.id=d.class_id and e.id=f.app_id and f.id=g.class_id and c.id=h.src_component_id and g.id=h.dest_component_id and d.exit_kind='p' and a.app in (" + params + ") and e.app in (" + params + ");";
		
		final String[] names = appNames;
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<ICCLink> links = new ArrayList<ICCLink>();
				try {
					while (rs.next())
					{
						String fromSMString = rs.getString(1);
						int instruction = rs.getInt(2);
						String exitKind = rs.getString(3);
						String destinationC = rs.getString(4);
						
						List<Integer> instructions = fetchInstructions(fromSMString, names);
						
						ICCLink link = new ICCLink(fromSMString, instruction, exitKind, destinationC, instructions);
						
						if (! UnreasonableLinksRemover.isUnreasonable(link))
						{
							links.add(link);
						}
					}
				} catch (SQLException e) 
				{
					e.printStackTrace();
				}
				
				return links;
			}
			
		};
		
		List<ICCLink> links = null;
		
		try 
		{
			System.out.println(sql);
			
			links = (List<ICCLink>) adapter.executeQuery(sql, paramValues, Constants.DB_NAME);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return links;
	}
	
	public List<IACLink> fetchIACLinks()
	{
		String sql = "select distinct a.app, b.app from " +
				"Applications a, Applications b, Classes c, Classes d, " +
				"ExitPoints e, Intents f, Components g, Links h " +
				"where a.id=c.app_id and c.id=e.class_id and e.id=f.exit_id and " +
				"f.id=h.intent_id and h.component_id=g.id and g.class_id=d.id and " +
				"d.app_id=b.id and a.app!=b.app;";
	
		
		DBAdapter adapter = new DBAdapter() 
		{
			@Override
			protected Object processResultSet(ResultSet rs)
			{
				List<IACLink> links = new ArrayList<IACLink>();
				try {
					while (rs.next())
					{
						String srcApp = rs.getString(1);
						String destApp = rs.getString(2);
						
						IACLink link = new IACLink(srcApp, destApp);
						links.add(link);
					}
				} catch (SQLException e) 
				{
					e.printStackTrace();
				}
				
				return links;
			}
			
		};
		
		List<IACLink> links = null;
		try
		{
			links = (List<IACLink>) adapter.executeQuery(sql, null, Constants.DB_NAME);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return links;
	}
	
	public List<ICCLink> filterICCLinks(List<ICCLink> links) 
	{
		loadFile(removedCompsPath);
		
		for (ICCLink link : links)
		{
			if (removedComps.contains(link.destinationC))
			{
				links.remove(link);
			}
		}
		
		return links;
	}
	
	public void loadFile(String filepath)
	{
		if (null != removedComps)
		{
			return;
		}
		
		try 
		{
			removedComps = new ArrayList<String>();
			
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			
			String line = "";
			while ((line = br.readLine()) != null)
			{
				if (line.startsWith("%"))
				{
					continue;
				}
				
				if (line.equals(""))
				{
					continue;
				}
				
				removedComps.add(line);
				
				
			}
			
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void setFilterRemovedComponents(boolean filterRemovedComponents) {
		this.filterRemovedComponents = filterRemovedComponents;
	}

	public void setRemovedCompsPath(String removedCompsPath) {
		this.removedCompsPath = removedCompsPath;
	}
}
