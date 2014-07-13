package soot.jimple.infoflow.android.iccta.stat;

import java.util.ArrayList;
import java.util.List;

public class ComponentDB 
{
	private int componentId;
	private boolean isExported;
	
	private String appName;
	private String clsName;
	private String protectedPermission = null;
	
	private List<IntentDB> intents = new ArrayList<IntentDB>();
	private List<IntentFilterDB> intentFilters = new ArrayList<IntentFilterDB>();
	private List<String> contentResolverURIs = new ArrayList<String>();
	
	//1 a, 2 s, 3 r, 4 p
	private String type = "a";
	private String contentProviderAuthority = "";
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Application: " + appName + "\n");
		sb.append("Type: " + type + "\n");
		sb.append("Class: " + clsName + "\n");
		sb.append("Exported: " + isExported + "\n");
		sb.append("ProtectedPermission: " + protectedPermission + "\n");
		sb.append("Intents: " + "\n");
		for (int i = 0; i < intents.size(); i++)
		{
			IntentDB intent = intents.get(i);
			
			sb.append("  Intent" + (i+1) + ": " + "\n");
			sb.append("    Implicit: " + intent.isImplicit() + "\n");
			sb.append("    DestComponentName: " + intent.getDestCompName() + "\n");
			sb.append("    Action: " + intent.getAction() + "\n");
			for (String category : intent.getCategories())
			{
				sb.append("    Category: " + category + "\n");
			}
			for (String extra : intent.getExtras())
			{
				sb.append("    Extra: " + extra + "\n");
			}
			sb.append("    Data:" + "\n");
			if (intent.getDataAndType() != null)
			{
				sb.append(intent.getDataAndType().setIndent(8));
			}
		}
		
		sb.append("IntentFilters: " + "\n");
		for (int i = 0; i < intentFilters.size(); i++)
		{
			IntentFilterDB intentFilter = intentFilters.get(i);
			
			sb.append("  IntentFilter" + (i+1) + ": " + "\n");
			for (String action : intentFilter.getActions())
			{
				sb.append("    Action: " + action + "\n");
			}
			for (String category : intentFilter.getCategories())
			{
				sb.append("    Category: " + category + "\n");
			}
			sb.append("    Data:" + "\n");
			sb.append(intentFilter.getDataAndType().setIndent(8));
		}
		
		sb.append("ContentResolver URIs: " + "\n");
		for (int i = 0; i < contentResolverURIs.size(); i++)
		{
			sb.append("    URI: " + contentResolverURIs.get(i) + "\n");
		}
		if (type.equals("p"))
		{
			sb.append("ContentProvider authority: " + contentProviderAuthority + "\n");
		}
		
		return sb.toString();
	}
	public int getComponentId() {
		return componentId;
	}
	public void setComponentId(int componentId) {
		this.componentId = componentId;
	}
	public boolean isExported() {
		return isExported;
	}
	public void setExported(boolean isExported) {
		this.isExported = isExported;
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
	public String getProtectedPermission() {
		return protectedPermission;
	}
	public void setProtectedPermission(String protectedPermission) {
		this.protectedPermission = protectedPermission;
	}
	public List<IntentDB> getIntents() {
		return intents;
	}
	public void setIntents(List<IntentDB> intents) {
		this.intents = intents;
	}
	public List<IntentFilterDB> getIntentFilters() {
		return intentFilters;
	}
	public void setIntentFilters(List<IntentFilterDB> intentFilters) {
		this.intentFilters = intentFilters;
	}
	public List<String> getContentResolverURIs() {
		return contentResolverURIs;
	}
	public void setContentResolverURIs(List<String> contentResolverURIs) {
		this.contentResolverURIs = contentResolverURIs;
	}
	public String getContentProviderAuthority() {
		return contentProviderAuthority;
	}
	public void setContentProviderAuthority(String contentProviderAuthorities) {
		this.contentProviderAuthority = contentProviderAuthorities;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
