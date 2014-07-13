package soot.jimple.infoflow.android.iccta.TestApps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import soot.Unit;
import soot.jimple.infoflow.InfoflowResults;
import soot.jimple.infoflow.InfoflowResults.SinkInfo;
import soot.jimple.infoflow.InfoflowResults.SourceInfo;
import soot.jimple.infoflow.android.iccta.util.Constants;
import soot.jimple.infoflow.solver.IInfoflowCFG;
import soot.tagkit.Tag;

public class ResultOutputter
{
	public static void output(IInfoflowCFG cfg, InfoflowResults results) 
	{
		try
		{
			if (! results.getResults().isEmpty())
			{
				List<List<String>> leaks = new ArrayList<List<String>>();
				
				for (Entry<SinkInfo, Set<SourceInfo>> entry : results.getResults().entrySet()) 
				{
					for (SourceInfo source : entry.getValue()) 
					{
						List<String> items = new ArrayList<String>();
						
						if (source.getPath() != null && ! source.getPath().isEmpty())
						{
							for (Unit stmt : source.getPath())
							{
								StringBuilder sb = new StringBuilder();
								
								sb.append(cfg.getMethodOf(stmt).getSignature() + "#");
								
								Tag tag = stmt.getTag(Constants.TAG_JIMPLE_INDEX_NUMBER);
								sb.append(tag.toString() + "#");
								
								sb.append(stmt);
								
								items.add(sb.toString());
							}
						}
						
						leaks.add(items);
					}
				}
				
				for (List<String> leak : leaks)
				{
					System.out.println("============================");
					for (String item : leak)
					{
						System.out.println(item);
					}
				}
			}
		}
		catch (Exception ex)
		{
			//
		}
	}

	
}
