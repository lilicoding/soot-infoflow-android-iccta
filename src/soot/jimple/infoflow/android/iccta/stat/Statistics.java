package soot.jimple.infoflow.android.iccta.stat;

import java.util.List;

public class Statistics 
{
	public static int numberOfComponentsExported = 0;
	public static int numberOfComponentsProtectedByPermission = 0;
	public static int numberOfIntents = 0;
	public static int numberOfImplicitIntents = 0;
	public static int numberOfExplicitIntents = 0;
	public static int numberOfIntentsContainingMimeType = 0;
	public static int numberOfIntentsContainingDataExceptMimeType = 0;
	
	public static int numberOfIntentFilters = 0;
	public static int numberOfIntentFiltersContainingMimeType = 0;
	public static int numberOfIntentFiltersContainingDataExceptMimeType = 0;
	
	public static void output(String pkgName)
	{
		List<ComponentDB> compDBs = StatDBHelper.fetchComponents(pkgName); 
		
		String stat = toStat(compDBs);
		System.out.println(stat);
		
		System.out.println("The details of this app are listed as follows:");
		for (ComponentDB comp : compDBs)
		{
			System.out.println(comp);
		}
	}
	
	public static String toStat(List<ComponentDB> compDBs)
	{
		for (ComponentDB compDB : compDBs)
		{
			if (compDB.isExported())
			{
				numberOfComponentsExported++;
			}
			
			if (null != compDB.getProtectedPermission())
			{
				numberOfComponentsProtectedByPermission++;
			}
			
			for (IntentDB intentDB : compDB.getIntents())
			{
				numberOfIntents++;
				
				if (intentDB.isImplicit())
				{
					numberOfImplicitIntents++;
				}
				else
				{
					numberOfExplicitIntents++;
				}
				
				DataAndType dataAndType = intentDB.getDataAndType();
				if (dataAndType == null)
				{
					continue;
				}
				
				if (dataAndType.containingMimeType())
				{
					numberOfIntentsContainingMimeType++;
				}
				
				if (dataAndType.containingDataExceptMimeType())
				{
					numberOfIntentsContainingDataExceptMimeType++;
				}
			}
			
			for (IntentFilterDB intentFilterDB : compDB.getIntentFilters())
			{
				numberOfIntentFilters++;
				
				DataAndType dataAndType = intentFilterDB.getDataAndType();
				if (dataAndType.containingMimeType())
				{
					numberOfIntentFiltersContainingMimeType++;
				}
				
				if (dataAndType.containingDataExceptMimeType())
				{
					numberOfIntentFiltersContainingDataExceptMimeType++;
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("The statistics of this app are listed as follows:" + "\n");
		sb.append("====>" + "numberOfComponentsExported: " + numberOfComponentsExported + "\n");
		sb.append("====>" + "numberOfComponentsProtectedByPermission: " + numberOfComponentsProtectedByPermission + "\n");
		sb.append("====>" + "numberOfIntents: " + numberOfIntents + "\n");
		sb.append("====>" + "numberOfImplicitIntents: " + numberOfImplicitIntents + "\n");
		sb.append("====>" + "numberOfExplicitIntents: " + numberOfExplicitIntents + "\n");
		sb.append("====>" + "numberOfIntentsContainingMimeType: " + numberOfIntentsContainingMimeType + "\n");
		sb.append("====>" + "numberOfIntentsContainingDataExceptMimeType: " + numberOfIntentsContainingDataExceptMimeType + "\n");
		sb.append("====>" + "numberOfIntentFilters: " + numberOfIntentFilters + "\n");
		sb.append("====>" + "numberOfIntentFiltersContainingMimeType: " + numberOfIntentFiltersContainingMimeType + "\n");
		sb.append("====>" + "numberOfIntentFiltersContainingDataExceptMimeType: " + numberOfIntentFiltersContainingDataExceptMimeType + "\n");
		
		return sb.toString();
	}
}
