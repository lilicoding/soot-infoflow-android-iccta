package soot.jimple.infoflow.android.iccta.iac;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NodeUtil 
{
	public static Set<Marker> getMarkers(Node node, int type)
	{
		Set<Marker> results = null;
		
		if (null == node)
		{
			results = Collections.emptySet();
		}
		else
		{
			results = new HashSet<Marker>();
			
			for (Marker m : node.getMarkers())
			{
				if (m.getType() == type)
				{
					results.add(m);
				}
			}
		}
		
		return results;
	}
	
	
	public static Node getNode(Node root, String nodeName)
	{
		Set<Node> nodes = root.getChildren();
		for (Node node : nodes)
		{
			if (nodeName.equals(node.getName()))
			{
				return node;
			}
		}
		
		return null;
	}
	
	/*public static void main(String[] args)
	{
		Node<TAMarker> node = new Node<TAMarker>("haha");
		TAMarker t = new TAMarker(1);
		t.setPath(null);
		node.getMarkers().add(t);
		
		NodeUtil.getMarkers(node, 1);
	}*/
}
