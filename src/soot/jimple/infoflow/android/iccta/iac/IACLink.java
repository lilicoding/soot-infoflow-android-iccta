package soot.jimple.infoflow.android.iccta.iac;


public class IACLink
{
	private String srcApp;
	private String destApp;
	
	public IACLink(String srcApp, String destApp)
	{
		this.srcApp = srcApp;
		this.destApp = destApp;
	}

	public String getSrcApp() {
		return srcApp;
	}

	public String getDestApp() {
		return destApp;
	}
}
