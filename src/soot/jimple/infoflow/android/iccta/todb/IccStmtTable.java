package soot.jimple.infoflow.android.iccta.todb;

public class IccStmtTable 
{
	private int id;
	private int exit_id;
	private int stmt_id;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getExit_id() {
		return exit_id;
	}
	public void setExit_id(int exit_id) {
		this.exit_id = exit_id;
	}
	public int getStmt_id() {
		return stmt_id;
	}
	public void setStmt_id(int stmt_id) {
		this.stmt_id = stmt_id;
	}
}
