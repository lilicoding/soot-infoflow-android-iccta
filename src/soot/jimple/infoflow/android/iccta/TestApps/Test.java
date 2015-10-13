package soot.jimple.infoflow.android.iccta.TestApps;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import soot.jimple.infoflow.android.iccta.AndroidIPCManager;
import soot.jimple.infoflow.android.iccta.db.DB;
import soot.jimple.infoflow.android.iccta.jimpleupdater.ApplicationClassSet;
import soot.jimple.infoflow.android.iccta.jimpleupdater.ExtraExtractor;
import soot.jimple.infoflow.android.iccta.jimpleupdater.InfoStatistic;
import soot.jimple.infoflow.android.iccta.jimpleupdater.JimpleIndexNumberTransformer;
import soot.jimple.infoflow.android.iccta.links.ICCLinker;
import soot.jimple.infoflow.android.iccta.sharedpreferences.SharedPreferencesUpdater;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class Test 
{
	public static IInfoflowCFG cfg = null;
	public static InfoflowResults results = null;
	
	public static String[] appPackageNames = null;
	public static String appPackageName = "";
	private static String iccProviderStr = "epicc";
	private static String apkPath = null;
	private static String androidJars = null;
	
	private static final String APK_COMBINER = "-ac-";
	
	public static void main(final String[] args)
	{
		apkPath = args[0];
		androidJars =args[1];
		System.out.println("[IccTA]" + apkPath + ", " + androidJars);
		
		parseConfig();
		
		try
		{
			DB.setJdbcPath("res/jdbc.xml");
			
			ProcessManifest processMan = new ProcessManifest(apkPath);
			Test.appPackageName = processMan.getPackageName();
			
			System.out.println("[IccTA]" + "ICC Provider is " + iccProviderStr);
			
			if (iccProviderStr.equals("ic3"))
			{
				ICCLinker.buildLinks(Test.appPackageName);
			}
			
			if (apkPath.contains(APK_COMBINER))
			{
				if (apkPath.contains("/"))
				{
					int startPos = apkPath.lastIndexOf('/');
					String filename = apkPath.substring(startPos+1);
					
					filename = filename.replace(".apk", "");
					
					Test.appPackageNames = filename.split(Test.APK_COMBINER);
				}
			}
			
			AndroidIPCManager ipcManager = new AndroidIPCManager("res/IPCMethods.txt", Test.appPackageName);
			if (Test.appPackageNames != null)
			{
				ipcManager = new AndroidIPCManager("res/IPCMethods.txt", Test.appPackageNames);
			}
			
			ipcManager.setIccProvider(iccProviderStr);
			
			InfoStatistic mostBeginning = new InfoStatistic("Beginning");
			ipcManager.addJimpleUpdater(mostBeginning);
			
			InfoStatistic mostEnding = new InfoStatistic("Ending");
			ipcManager.addPostJimpleUpdater(mostEnding);
			
			SharedPreferencesUpdater sharedPreferencesUpdater = new SharedPreferencesUpdater();
			ipcManager.addJimpleUpdater(sharedPreferencesUpdater);
			
			//JimpleReduceStaticFieldsTransformer jrsf = new JimpleReduceStaticFieldsTransformer();
			//ipcManager.addJimpleUpdater(jrsf);
			
			JimpleIndexNumberTransformer jinTransformer = new JimpleIndexNumberTransformer();
			ipcManager.addJimpleUpdater(jinTransformer);
			
			ApplicationClassSet acs = new ApplicationClassSet();
			ipcManager.addJimpleUpdater(acs);
			
			//ExtraMapping extraMapping = new ExtraMapping(ApplicationClassSet.applicationClasses);
			//ipcManager.addJimpleUpdater(extraMapping);
			
			ExtraExtractor extraExtractor = new ExtraExtractor();
			ipcManager.addJimpleUpdater(extraExtractor);
			
			FlowDroidLauncher.setIPCManager(ipcManager);
			FlowDroidLauncher.main(args);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	static final String PROP_ICC_PROVIDER = "iccProvider";
	
	static void parseConfig()
	{
		Properties prop = new Properties();
		InputStream inStream = null;
		
		try
		{
			inStream = new FileInputStream("res/iccta.properties");
			prop.load(inStream);
			
			iccProviderStr = prop.getProperty(PROP_ICC_PROVIDER);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
