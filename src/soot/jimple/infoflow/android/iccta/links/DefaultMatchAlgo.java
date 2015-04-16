package soot.jimple.infoflow.android.iccta.links;

import java.util.List;

import soot.jimple.infoflow.android.iccta.stat.DataAndType;
import soot.jimple.infoflow.android.iccta.stat.IntentDB;
import soot.jimple.infoflow.android.iccta.stat.IntentFilterDB;
import soot.jimple.infoflow.android.iccta.util.StringUtil;


/*
 * Design another table especially for Content Provider Links
 */
public class DefaultMatchAlgo implements IMatchAlgo
{

	/*
	public boolean match(ComponentDB src, ComponentDB dest, int level) 
	{
		if (dest.getType().equals("p"))
		{
			if (! StringUtil.isEmpty(dest.getContentProviderAuthority()))
			{
				for (String uri : src.getContentResolverURIs())
				{
					if (uri.contains("://"))
					{
						int startPos = uri.indexOf(":") + 2;
						int endPos = uri.indexOf("/", startPos);
						
						String authority = uri.substring(startPos, endPos);
						
						System.out.println(authority);
						
						if (authority.equals(src.getContentProviderAuthority()))
						{
							return true;
						}
					}
				}
			}
		}
		else
		{
			for (IntentDB intentDB : src.getIntents())
			{
				//explicit Intent
				if (! intentDB.isImplicit())
				{
					
				}
			}
		}
		
		
		return false;
	}*/
	
	/**
	 * The idea here is that we can give up some links, but we had better not introduce false positives
	 * level = 1
	 * 	* Links without mime-type and data (ignore such links containing mime-type data or other data info)
	 * level = 2
	 * 	* level1
	 * 	* Links with mime-type but withoug other data info
	 * level = 3
	 * 	* level2
	 * 	* Links with everything matched
	 * 
	 * @param intent
	 * @param intentFilter
	 * @param level
	 * @return
	 */
	@Override
	public boolean match(IntentDB intent, IntentFilterDB intentFilter, int level)
	{
		switch (level)
		{
		case 1:
			return validateLevel1(intent, intentFilter);
		case 2:
			return validateLevel2(intent, intentFilter);
		case 3:
			return validateLevel3(intent, intentFilter);
		}
		
		return false;
	}
	
	private boolean twoSameActionAndCategories(IntentDB intent, IntentFilterDB intentFilter)
	{
		String action = intent.getAction();
		if (StringUtil.isEmpty(action))
		{
			return true;
		}
		
		List<String> actions = intentFilter.getActions();
		if (! actions.contains(action))
		{
			return false;
		}
		
		List<String> srcCategories = intent.getCategories();
		List<String> destCategories = intentFilter.getCategories();
		
		for (String cat : srcCategories)
		{
			if (! destCategories.contains(cat))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * 
	 * @param src != null
	 * @param dest != null
	 * @return
	 */
	private boolean twoSameMimeTypes(DataAndType src, DataAndType dest)
	{
		String srcType = src.getType();
		String destType = dest.getType();
		String srcSubtype = src.getSubtype();
		String destSubtype = dest.getSubtype();
		
		if (null == srcType)
		{
			srcType = "*";
		}
		if (null == destType)
		{
			destType = "*";
		}
		if (null == srcSubtype)
		{
			srcSubtype = "*";
		}
		if (null == destSubtype)
		{
			destSubtype = "*";
		}
		
		if (StringUtil.isEmpty(srcType) && StringUtil.isEmpty(destType))
		{
			if (StringUtil.isEmpty(srcSubtype) && StringUtil.isEmpty(destSubtype))
			{
				return true;
			}
			
			return false;
		}
		else
		{
			if (StringUtil.isEmpty(srcSubtype) && StringUtil.isEmpty(destSubtype))
			{
				return false;
			}
			
			//all attributes are not empty
			
			boolean typeMatched = false;
			boolean subtypeMatched = false;
			
			if (srcType.equals("*") || destType.equals("*"))
			{
				typeMatched = true;
			}
			else if (twoSameStrings(srcType, destType))
			{
				typeMatched = true;
			}
			
			if (srcSubtype.equals("*") || destSubtype.equals("*"))
			{
				typeMatched = true;
			}
			else if (twoSameStrings(srcSubtype, destSubtype))
			{
				subtypeMatched = true;
			}
			
			return typeMatched && subtypeMatched;
		}
	}
	
	private boolean twoSameData(DataAndType src, DataAndType dest)
	{
		boolean resultOfMimeType = twoSameMimeTypes(src, dest);
		
		//containing data except mimeType
		//1) comparing the #scheme attribute
		//2) comparing the #host attribute
		//3) then comparing the port and path
		//currently, ignore the prefix and suffix of path and ignore the query conditions
		
		if (twoEmptyStrings(src.getScheme(), dest.getScheme()))
		{
			return true && resultOfMimeType;
		}
		else 
		{
			// Scheme
			if (twoSameStrings(src.getScheme(), dest.getScheme()))
			{
				if (twoEmptyStrings(src.getHost(), dest.getHost()))
				{
					return true && resultOfMimeType;
				}
				else
				{
					//Host & Port
					if (twoSameStrings(src.getHost(), dest.getHost()))
					{
						boolean resultOfPort = twoEmptyStrings(src.getPort(), dest.getPort()) || twoSameStrings(src.getPort(), dest.getPort());
						//boolean resultOfPath = twoEmptyStrings(src.getPath(), dest.getPath()) || twoSameStrings(src.getPath(), dest.getPath());
						
						if (true && resultOfPort)
						{
							if (twoEmptyStrings(src.getPath(), dest.getPath()))
							{
								return true;
							}
							else
							{
								System.out.println(src.getPath() + " | " + dest.getPath());
								
								if (src.getPath().matches(dest.getPath()))
								{
									return true;
								}
							}
						}
					}
				}
				
			}
		}
		
		return false;
	}
	
	private boolean validateLevel1(IntentDB intent, IntentFilterDB intentFilter)
	{
		DataAndType src = intent.getDataAndType();
		DataAndType dest = intentFilter.getDataAndType();
		
		if (src.containingNothing() && dest.containingNothing())
		{
			return twoSameActionAndCategories(intent, intentFilter);
		}

		return false;
	}
	
	private boolean validateLevel2(IntentDB intent, IntentFilterDB intentFilter)
	{
		DataAndType src = intent.getDataAndType();
		DataAndType dest = intentFilter.getDataAndType();
		
		if (src.containingNothing() && dest.containingNothing())
		{
			return twoSameActionAndCategories(intent, intentFilter);
		}
		else if (src.containingOnlyMimeType() && dest.containingOnlyMimeType())
		{
			return twoSameMimeTypes(src, dest);
		}
		
		return false;
	}
	
	//for level 3
	private boolean validateLevel3(IntentDB intent, IntentFilterDB intentFilter)
	{
		DataAndType src = intent.getDataAndType();
		DataAndType dest = intentFilter.getDataAndType();
		src.parseUri();
		
		if (src.containingNothing() && dest.containingNothing())
		{
			return twoSameActionAndCategories(intent, intentFilter);
		}
		else if (src.containingOnlyMimeType() && dest.containingOnlyMimeType())
		{
			return twoSameMimeTypes(src, dest);
		}
		else
		{
			return twoSameData(src, dest);
		}
	}
	
	private boolean twoEmptyStrings(String src, String dest)
	{
		if (StringUtil.isEmpty(src) && StringUtil.isEmpty(dest))
		{
			return true;
		}
		
		return false;
	}
	
	private boolean twoSameStrings(String src, String dest)
	{
		if (! StringUtil.isEmpty(src) && ! StringUtil.isEmpty(dest) && src.equals(dest))
		{
			return true;
		}
		
		return false;
	}
}
