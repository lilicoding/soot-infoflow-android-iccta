package soot.jimple.infoflow.android.iccta.iac;

public class Marker 
{
	private int type;
	
	public Marker() {}
	
	public Marker(int type)
	{
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
