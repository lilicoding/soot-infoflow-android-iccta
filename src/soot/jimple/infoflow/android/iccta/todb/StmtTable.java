package soot.jimple.infoflow.android.iccta.todb;

public class StmtTable 
{
	private int id;
	private String stmt;
	private String method;
	private int class_id;
	private int jimpleIndex;
	private boolean isIcc;
	private String type;
	private String reserved = "";
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStmt() {
		return stmt;
	}
	public void setStmt(String stmt) {
		this.stmt = stmt;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public int getClass_id() {
		return class_id;
	}
	public void setClass_id(int class_id) {
		this.class_id = class_id;
	}
	public int getJimpleIndex() {
		return jimpleIndex;
	}
	public void setJimpleIndex(int jimpleIndex) {
		this.jimpleIndex = jimpleIndex;
	}
	public boolean isIcc() {
		return isIcc;
	}
	public void setIcc(boolean isIcc) {
		this.isIcc = isIcc;
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
}
