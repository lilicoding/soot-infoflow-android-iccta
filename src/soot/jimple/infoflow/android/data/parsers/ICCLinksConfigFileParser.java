package soot.jimple.infoflow.android.data.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.jimple.infoflow.android.iccta.icc.ICCLink;
import soot.jimple.infoflow.android.iccta.links.UnreasonableLinksRemover;

public class ICCLinksConfigFileParser 
{
	private String configFilePath = null;
	
	public ICCLinksConfigFileParser(String configFilePath)
	{
		this.configFilePath = configFilePath;
	}
	
	@SuppressWarnings("unchecked")
	private void put(Map<String, List<ICCLink>> map, String pkg, ICCLink link)
	{
		Object obj = map.get(pkg);
		if (null == obj)
		{
			List<ICCLink> links = new ArrayList<ICCLink>();
			links.add(link);
			
			map.put(pkg, links);
		}
		else
		{
			List<ICCLink> links = (List<ICCLink>) obj;
			links.add(link);
			
			map.put(pkg, links);
		}
	}
	
	public Map<String, List<ICCLink>> parse()
	{
		Map<String, List<ICCLink>> pkg2links = new HashMap<String, List<ICCLink>>();

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(configFilePath));
			
			String line = null;
			
			while ((line = br.readLine()) != null)
			{
				if (line.isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				
				int endPos = line.indexOf(':');
				String pkg = line.substring(0, endPos);
				
				int startPos = line.indexOf('<');
				endPos = line.lastIndexOf('>');
				String fromSMString = line.substring(startPos, endPos+1);
				
				startPos = line.indexOf('[');
				endPos = line.indexOf(']');
				String lineNumberAndExitKind = line.substring(startPos+1, endPos);
				String lineNumber = lineNumberAndExitKind.split("-")[0];
				String exitKind = lineNumberAndExitKind.split("-")[1];
				
				startPos = endPos+1;
				endPos = line.indexOf('{');
				String destinationC = line.substring(startPos, endPos).trim();
				
				startPos = endPos+1;
				endPos = line.lastIndexOf('}');
				String numbers = line.substring(startPos, endPos);
				
				List<Integer> instructions = new ArrayList<Integer>();
				String[] ss = numbers.split(",");
				for (String s : ss) 
				{
					Integer i = Integer.parseInt(s.trim());
					instructions.add(i);
				}
				
				ICCLink link = new ICCLink(fromSMString, Integer.parseInt(lineNumber), exitKind, destinationC, instructions);
				
				if (! UnreasonableLinksRemover.isUnreasonable(link))
				{
					put(pkg2links, pkg, link);
				}
			}
			
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return pkg2links;
	}
}
