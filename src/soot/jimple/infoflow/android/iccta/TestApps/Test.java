package soot.jimple.infoflow.android.iccta.TestApps;

import soot.jimple.infoflow.android.iccta.AndroidIPCManager;
import soot.jimple.infoflow.android.iccta.IccTAPrintStream;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class Test {

	public static String appPackageName = "";
	
	/**
	 * @param args
		[0]: apkLocation
		[1]: androidJars
		[2]: -icc-provider=epicc / config-file
	 */
	public static void main(final String[] args)
	{
		if (! validatedParameters(args))
		{
			usage();
			System.exit(1);
		}
		
		System.setOut(new IccTAPrintStream(System.out));
		
		try
		{
			ProcessManifest processMan = new ProcessManifest(args[0]);
			Test.appPackageName = processMan.getPackageName();
			
			System.out.println("app pckage name is " + Test.appPackageName);
			
			AndroidIPCManager ipcManager = new AndroidIPCManager("res/IPCMethods.txt", Test.appPackageName);
			ipcManager.setIccProvider(iccProviderStr);
			soot.jimple.infoflow.android.TestApps.Test.setIPCManager(ipcManager);
			soot.jimple.infoflow.android.TestApps.Test.main(infoflowAndroidArgs);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private static String[] infoflowAndroidArgs = null;
	private static String iccProviderStr = null;
	
	public static boolean validatedParameters(String[] args)
	{
		if (args.length < 3)
		{
			return false;
		}
		
		
		if (! args[2].startsWith("-icc-provider="))
		{
			return false;
		}
		
		iccProviderStr = args[2].replace("-icc-provider=", ""); 
		infoflowAndroidArgs = new String[args.length-1];
		for (int i = 0; i < infoflowAndroidArgs.length; i++)
		{
			if (i >= 2)
			{
				infoflowAndroidArgs[i] = args[i+1];
			}
			else
			{
				infoflowAndroidArgs[i] = args[i];
			}
		}
		
		return true;
	}
	
	public static void usage()
	{
		System.out.println("java -jar IccTA.jar <apkPath> <androidJars> <-icc-provider=VALUE> <all flowdroid's parameters>");
		System.out.println("-icc-provider=VALUE is mandatory, the default icc provider is epicc");
	}
}
