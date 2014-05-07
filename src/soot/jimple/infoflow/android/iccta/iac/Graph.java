package soot.jimple.infoflow.android.iccta.iac;


public class Graph implements IGraphDump
{
	//virtual node
	private Node root = null;
	
	public Graph()
	{
		root = new Node("^_^_ROOT_^_^");
	}
	
	public Node getRoot() {
		return root;
	}

	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		
		for (Node node : root.getChildren())
		{
			sb.append(node.getName() + "\n");
			
			for (Node n : node.getChildren())
			{
				sb.append("--->" + n.getName() + "\n");
			}
			
			sb.append("***>" + node.getParent() + "\n");
		}
		
		return sb.toString();
	}

	@Override
	public void dump() 
	{
		System.out.println(this.toString());
	}
}
