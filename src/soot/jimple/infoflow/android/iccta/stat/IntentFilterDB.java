package soot.jimple.infoflow.android.iccta.stat;

import java.util.ArrayList;
import java.util.List;

import soot.jimple.infoflow.android.iccta.util.StringUtil;
import android.content.IntentFilter;
import android.os.PatternMatcher;

public class IntentFilterDB 
{
	private String appName;
	private String clsName;
	private int intentFilterId;
	
	private List<String> actions = new ArrayList<String>();
	private List<String> categories = new ArrayList<String>();
	private DataAndType dataAndType;
	
	private int filterType = 1;
	
	public IntentFilter toIntentFilter()
	{
		IntentFilter filter = new IntentFilter();
		
		for (String action : actions)
		{
			filter.addAction(action);
		}
		
		for (String category : categories)
		{
			filter.addCategory(category);
		}
		
		return filter;
	}
	
	public IntentFilter toIntentFilter(int type)
	{
		if (1 == type)
		{
			return toIntentFilter();
		}
		
		IntentFilter filter = toIntentFilter();
		
		try
		{
			//for mimetype
			if (type >= 1)
			{
				if (! StringUtil.isEmpty(dataAndType.getType()))
				{
					if (! StringUtil.isEmpty(dataAndType.getSubtype()))
					{
						filter.addDataType(dataAndType.getType() + "/" + dataAndType.getSubtype());
						filterType = 2;
					}
					else
					{
						filter.addDataType(dataAndType.getType());
						filterType = 2;
					}
				}
			}
			
			//for data
			if (type >= 2)
			{
				if (! StringUtil.isEmpty(dataAndType.getHost()))
				{
					if (! StringUtil.isEmpty(dataAndType.getPort()))
					{
						filter.addDataAuthority(dataAndType.getHost(), dataAndType.getPort());
					}
					else
					{
						filter.addDataAuthority(dataAndType.getHost(), null);
					}
					
					
					filterType = 3;
				}
				
				if (! StringUtil.isEmpty(dataAndType.getPath()))
				{
					filter.addDataPath(dataAndType.getPath(), PatternMatcher.PATTERN_LITERAL);
					
					filterType = 3;
				}
				
				if (! StringUtil.isEmpty(dataAndType.getScheme()))
				{
					filter.addDataScheme(dataAndType.getScheme());
					
					filterType = 3;
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return filter;
	}
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getClsName() {
		return clsName;
	}
	public void setClsName(String clsName) {
		this.clsName = clsName;
	}
	public int getIntentFilterId() {
		return intentFilterId;
	}
	public void setIntentFilterId(int intentFilterId) {
		this.intentFilterId = intentFilterId;
	}
	public List<String> getActions() {
		return actions;
	}
	public void setActions(List<String> actions) {
		this.actions = actions;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public DataAndType getDataAndType() {
		return dataAndType;
	}
	public void setDataAndType(DataAndType dataAndType) {
		this.dataAndType = dataAndType;
	}

	public int getFilterType() {
		return filterType;
	}
}
