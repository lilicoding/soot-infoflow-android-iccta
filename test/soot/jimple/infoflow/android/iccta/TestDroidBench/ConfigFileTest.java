package soot.jimple.infoflow.android.iccta.TestDroidBench;

import org.junit.Test;

public class ConfigFileTest {

	public final static String droidBenchDir;
	public final static String androidJars;
	
	//Load test.properties
	static
	{
		PropertiesHelper.init("test/test.properties");
		droidBenchDir = PropertiesHelper.getPropertiesValue("DroidBenchDir");
		androidJars = PropertiesHelper.getPropertiesValue("AndroidJars");
	}
	
	private String[] toArgs(String appPath, String androidJars)
	{
	
		String[] args = {
			"-apkPath", appPath,
			"-androidJars", androidJars,
			"-iccProvider", "configfile"
		};
		
		return args;
	}
	
	@Test
	public void testSendBroadcast1()
	{
		String apkPath = droidBenchDir + "/apk/InterCompCommunication_sendBroadcast1/InterCompCommunication_sendBroadcast1.apk";
	
		soot.jimple.infoflow.android.iccta.TestApps.Test.main(toArgs(apkPath, androidJars));
	}
}
