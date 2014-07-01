package soot.jimple.infoflow.android.iccta.iac;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IACGraph extends Graph 
{
	private Map<String, Node> nodes = null;
	
	public IACGraph()
	{
		super();
		nodes = new HashMap<String, Node>();
	}
	
	
	public void build(List<IACLink> links)
	{
		for (IACLink link : links)
		{
			String srcApp = link.getSrcApp();
			String destApp = link.getDestApp();
			
			Node srcNode = getNode(srcApp);
			Node destNode = getNode(destApp);
			
			destNode.setParent(srcNode);
			srcNode.getChildren().add(destNode);
		}
	}
	
	public void traverse()
	{
		Node root = getRoot();
		Set<Node> nodes = root.getChildren();
		
		for (Node node : nodes)
		{
			System.out.println("***************************");
			printChildren(node, "");
		}
		
	}
	
	private void printChildren(Node node, String space)
	{
		System.out.println(space + node.getName());
		
		Set<Node> nodes = node.getChildren();
		
		for (Node n : nodes)
		{
			System.out.println(space + "    " + n.getName());
			for (Node n2 : n.getChildren())
			{
				printChildren(n2, space + "        ");
			}
		}
	}
	
	public Node getNode(String appName)
	{
		Node node = nodes.get(appName);
		if (null == node)
		{
			node = new Node(appName);
			node.setParent(getRoot());
			getRoot().getChildren().add(node);
			nodes.put(appName, node);
		}
		
		return node;
	}
	/*
	public static void main(String[] args) 
	{
		IACGraph iacGraph = new IACGraph();
		
		ICCLinksThroughServerDB fetcher = new ICCLinksThroughServerDB();
		
		
		List<IACLink> links = fetcher.fetchIACLinks();
		
		System.out.println("start");
		
		Set<String> removed = new HashSet<String>();
		
		for (IACLink link : links)
		{
			//System.out.println(link.getSrcApp() + ":" + link.getDestApp());
			
			if (link.getDestApp().equals("com.example.accept_action_view"))
			{
				removed.add(link.getSrcApp());
			}
		}
		
		Map<String, Integer> maps = new HashMap<String, Integer>();
		
		for (IACLink link : links)
		{
			if (! removed.contains(link.getSrcApp()))
			{
				//System.out.println(link.getSrcApp() + ":" + link.getDestApp());
				
				Object p = maps.get(link.getSrcApp());
				if (p == null)
				{
					maps.put(link.getSrcApp(), 1);
				}
				else
				{
					int p1 = (Integer)p;
					maps.put(link.getSrcApp(), p1+1);
				}
				
			}
		}
		
		for (IACLink link : links)
		{
			if (! removed.contains(link.getSrcApp()))
			{
				//Object o = maps.get(link.getSrcApp());
				
				
				if (maps.get(link.getSrcApp()) < 5)
				{
					System.out.println(link.getSrcApp() + ":" + link.getDestApp());
				}
			}
		}
		
		/*for (Entry entry : maps.entrySet())
		{
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}* /
		
		//System.out.println(links);
		
		//iacGraph.build(links);
		
		//iacGraph.traverse();
		
		//iacGraph.dump();
	}*/

}
