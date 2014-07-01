package soot.jimple.infoflow.android.iccta.TestDroidBench;

import org.junit.Test;

public class EpiccTest {

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
			"-iccProvider", "epicc"
		};
		
		return args;
	}
	
	@Test
	public void testStartActivity()
	{
		String apkPath = droidBenchDir + "/apk/InterCompCommunication_startActivity1/InterCompCommunication_startActivity1.apk";
		soot.jimple.infoflow.android.iccta.TestApps.Test.main(toArgs(apkPath, androidJars));
	}

	
	
}
