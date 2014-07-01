package soot.jimple.infoflow.android.iccta.stat;

public class LinkDB 
{
	private int id;
	private int intent_id;
	private int component_id;
	private int type;
	private String reserved;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIntent_id() {
		return intent_id;
	}
	public void setIntent_id(int intent_id) {
		this.intent_id = intent_id;
	}
	public int getComponent_id() {
		return component_id;
	}
	public void setComponent_id(int component_id) {
		this.component_id = component_id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
		this.reserved = reserved;
	}
}
