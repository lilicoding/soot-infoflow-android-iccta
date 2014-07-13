package soot.jimple.infoflow.android.iccta.stat;

public class ProviderLinkDB 
{
	private int id;
	private int src_component_id;
	private int dest_component_id;
	private String reserved;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSrc_component_id() {
		return src_component_id;
	}
	public void setSrc_component_id(int src_component_id) {
		this.src_component_id = src_component_id;
	}
	public int getDest_component_id() {
		return dest_component_id;
	}
	public void setDest_component_id(int dest_component_id) {
		this.dest_component_id = dest_component_id;
	}
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
		this.reserved = reserved;
	}
}
