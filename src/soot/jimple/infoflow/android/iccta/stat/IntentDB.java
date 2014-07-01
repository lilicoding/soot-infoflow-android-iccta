package soot.jimple.infoflow.android.iccta.stat;

import java.util.ArrayList;
import java.util.List;

import soot.jimple.infoflow.android.iccta.util.StringUtil;

import android.content.Intent;
import android.net.Uri;


public class IntentDB 
{
	private int intentid;
	private String appName;
	private String clsName;
	private String method;
	private int instruction; //jimpleIndex
	private boolean implicit;
	
	private String destCompName;
	
	private String action;
	private List<String> categories = new ArrayList<String>();
	private DataAndType dataAndType;
	private List<String> extras = new ArrayList<String>();
	
	/*
	 * 0: explicit Intents
	 * 1: 0 + action/categories
	 * 2: 1 + mimetype
	 * 3: 2 + data
	 */
	private int intentType = 1;
	
	public Intent toIntent()
	{
		Intent intent = new Intent();
		
		intent.setAction(action);
		
		for (String category : categories)
		{
			intent.addCategory(category);
		}
		
		return intent;
	}
	
	public Intent toIntent(int type)
	{
		if (1 == type)
		{
			return toIntent();
		}
		
		Intent intent = toIntent();
		
		//for mimetype
		if (type >= 1)
		{
			if (! StringUtil.isEmpty(dataAndType.getType()))
			{
				if (! StringUtil.isEmpty(dataAndType.getSubtype()))
				{
					intent.setType(dataAndType.getType() + "/" + dataAndType.getSubtype());
					intentType = 2;
				}
				else
				{
					intent.setType(dataAndType.getType());
					intentType = 2;
				}
			}
		}
		
		//for data
		if (type >= 2)
		{
			if (! StringUtil.isEmpty(dataAndType.getUri()))
			{
				intent.setData(Uri.parse(dataAndType.getUri()));
				
				intentType = 3;
			}
		}
		
		return intent;
	}
	
	public int getIntentid() {
		return intentid;
	}
	public void setIntentid(int intentid) {
		this.intentid = intentid;
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
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public int getInstruction() {
		return instruction;
	}
	public void setInstruction(int instruction) {
		this.instruction = instruction;
	}
	public String getDestCompName() {
		return destCompName;
	}
	public void setDestCompName(String destCompName) {
		this.destCompName = destCompName;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> category) {
		this.categories = category;
	}
	public DataAndType getDataAndType() {
		return dataAndType;
	}
	public void setDataAndType(DataAndType data) {
		this.dataAndType = data;
	}
	public boolean isImplicit() {
		return implicit;
	}
	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
	}
	public List<String> getExtras() {
		return extras;
	}
	public void setExtras(List<String> extras) {
		this.extras = extras;
	}

	public int getIntentType() {
		return intentType;
	}
}
