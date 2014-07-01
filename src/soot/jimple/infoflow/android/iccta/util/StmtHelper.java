package soot.jimple.infoflow.android.iccta.util;

import java.util.List;

import soot.Unit;
import soot.tagkit.Tag;


public class StmtHelper 
{
	public static String getClazzName(String stmt)
	{
		int startPos = stmt.toString().indexOf('<');
		int endPos = stmt.toString().indexOf(':');
		
		return stmt.toString().substring(startPos+1, endPos);
	}
	
	public static String getMethodName(String stmt)
	{
		
		int endPos = stmt.toString().indexOf('(');
		int startPos = stmt.toString().lastIndexOf(' ', endPos);
		
		return stmt.toString().substring(startPos+1, endPos);

	}
	
	/**
	 * Copy all the tags of {from} to {to}, if {to} already contain the copied tag, then overwrite it.
	 * 
	 * @param from
	 * @param to
	 */
	public static void copyTags(Unit from, Unit to)
	{
		List<Tag> tags = from.getTags();
		
		for (Tag tag : tags)
		{
			to.removeTag(tag.getName());  //exception??
			
			to.addTag(tag);
		}
	}
	
	public static void main(String[] args)
	{
		String stmt = "specialinvoke $r1.<lu.uni.serval.icc_startactivity1.InFlowActivity: void <init>(android.content.Intent)>($r0)";
		System.out.println(getClazzName(stmt));
		System.out.println(getMethodName(stmt));
	}
}
