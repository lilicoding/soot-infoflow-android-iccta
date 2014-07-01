package soot.jimple.infoflow.android.iccta.test;

import java.io.File;

import soot.jimple.infoflow.android.iccta.TestApps.Test;

public class TestWrapper {

	public static void main(String[] args) 
	{
		File dir = new File("testapps");

        File[] files = dir.listFiles();

        for (File file : files)
        {
            String path = file.getAbsolutePath();
            String jar = "/Users/li.li/Project/github/android-platforms/android-18/android.jar";
            
            String[] args2 = 
        	{
        		"-apkPath", path,
        		"-androidJars", jar,
        		"-iccProvider", "epicc",
        		"-enableDB",
        	};
            
            try
            {
            	Test.main(args2);
            }
            catch (Exception ex)
            {
                System.out.println(path + " exception.");
            }
        }
	}

}
