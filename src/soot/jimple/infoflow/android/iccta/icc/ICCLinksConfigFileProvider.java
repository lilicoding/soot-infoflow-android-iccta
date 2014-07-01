package soot.jimple.infoflow.android.iccta.icc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soot.jimple.infoflow.android.data.parsers.ICCLinksConfigFileParser;


public class ICCLinksConfigFileProvider implements IICCLinksProvider 
{
	static Map<String, List<ICCLink>> iccLinks = null;
	
	static
	{
		iccLinks = new ICCLinksConfigFileParser("res/iccLinksConfigFile.txt").parse();
	}
	
	@Override
	public List<ICCLink> getICCLinks(String[] appNames) 
	{
		List<ICCLink> results = new ArrayList<ICCLink>();
		
		for (String app : appNames)
		{
			results.addAll(iccLinks.get(app));
		}
		
		return results;
	}

}
