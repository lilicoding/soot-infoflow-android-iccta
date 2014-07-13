package soot.jimple.infoflow.android.iccta.util;

public class Constants 
{
	//update the db name when upload jdbc.xml configuration 
	public static String DB_NAME = "cc";
	
	public static final String TABLE_NAME_STMTS = "Stmts";
	public static final String TABLE_NAME_PATHS = "Paths";
	public static final String TABLE_NAME_ICCSTMTS = "IccStmts";
	public static final String TABLE_NAME_EXTRAS = "Extras";
	public static final String TABLE_NAME_LINKS = "Links";
	public static final String TABLE_NAME_PROVIDER_LINKS = "ProviderLinks";
	
	public static final String STMT_TYPE_NORMAL = "normal";
	public static final String STMT_TYPE_SOURCE = "source";
	public static final String STMT_TYPE_SINK = "sink";
	
	public static final String PATH_TYPE_NORMAL = "normal";
	public static final String PATH_TYPE_PACL = "pacl";
	public static final String PATH_TYPE_PPCL = "ppcl";
	public static final String PATH_TYPE_PBCL = "pbcl";
	
	public static final String EXTRA_TYPE_GET = "get";
	public static final String EXTRA_TYPE_PUT = "put";
	
	public static final String TAG_JIMPLE_INDEX_NUMBER = "JimpleIndexNumberTag";
	
	
	public static int INTENT_MATCH_LEVEL = 3;
}
