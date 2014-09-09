package soot.jimple.infoflow.android.iccta.todb;

public class PathTable 
{
	private int id;
	private int app_id;
	private int source;
	private int sink;
	private String paths;
	private String type;
	private int icc;
	private String reserved = "";
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getSink() {
		return sink;
	}
	public void setSink(int sink) {
		this.sink = sink;
	}
	public String getPaths() {
		return paths;
	}
	public void setPaths(String paths) {
		this.paths = paths;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
		this.reserved = reserved;
	}
	public int getApp_id() {
		return app_id;
	}
	public void setApp_id(int app_id) {
		this.app_id = app_id;
	}
	public int getIcc() {
		return icc;
	}
	public void setIcc(int icc) {
		this.icc = icc;
	}
}
