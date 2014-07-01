package soot.jimple.infoflow.android.iccta.iac;

import java.util.HashSet;
import java.util.Set;

public class Node
{
	private String name;
	private Node parent;    //problem: one node may contains multiple parent nodes
	private Set<Node> children; 
	private Set<Marker> markers;
	
	public Node(String name)
	{
		this.name = name;
		children = new HashSet<Node>();
		markers = new HashSet<Marker>();
	}

	public String getName() {
		return name;
	}

	public Set<Node> getChildren() {
		return children;
	}

	public Set<Marker> getMarkers() {
		return markers;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node clone(String newName)
	{
		Node n = new Node(newName);
		
		for (Node node : children)
		{
			n.getChildren().add(node);
		}
		
		for (Marker marker : markers)
		{
			n.getMarkers().add(marker);
		}
		
		n.setParent(getParent());
		
		return n;
	}
	
	@Override
	public int hashCode() 
	{
		return name.hashCode();
	}

	@Override
	public String toString() 
	{
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Node)
		{
			Node node = (Node) o;
			return name.equals(node.getName());
		}
		
		return false;
	}
	
	
}
