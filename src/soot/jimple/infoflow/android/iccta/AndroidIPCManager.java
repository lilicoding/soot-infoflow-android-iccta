package soot.jimple.infoflow.android.iccta;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.infoflow.android.data.AndroidMethod;
import soot.jimple.infoflow.android.data.parsers.IPCMethodParser;
import soot.jimple.infoflow.android.iccta.icc.ICCLink;
import soot.jimple.infoflow.android.iccta.icc.ICCLinksConfigFileProvider;
import soot.jimple.infoflow.android.iccta.icc.ICCLinksEpiccProvider;
import soot.jimple.infoflow.android.iccta.icc.IICCLinksProvider;
import soot.jimple.infoflow.android.iccta.jimpleupdater.JimpleUpdater;
import soot.jimple.infoflow.android.iccta.util.ICCMethodHelper;
import soot.jimple.infoflow.ipc.MethodBasedIPCManager;

public class AndroidIPCManager extends MethodBasedIPCManager {
    
    public static Set<AndroidMethod> ipcAMethods = new HashSet<AndroidMethod>();;
    String[] appPackageNames = null;
    
    private IICCLinksProvider iccProvider = null;
    private String iccProviderStr = null;
    
    private List<JimpleUpdater> jimpleUpdaters = new ArrayList<JimpleUpdater>();
    private List<JimpleUpdater> postJimpleUpdaters = new ArrayList<JimpleUpdater>();
    
    public AndroidIPCManager(Set<AndroidMethod> ipcAMethods,
            String appPackageName) {
    	AndroidIPCManager.ipcAMethods = ipcAMethods;
        
        this.appPackageNames = new String[1];
        this.appPackageNames[0] = appPackageName;
        
        System.out.println("Created a AndroidIPCManager with "
                + AndroidIPCManager.ipcAMethods.size() + " IPC methods for "
                + " app. package name '" + appPackageNames[0] + "'");
    }

    public AndroidIPCManager(String ipcFile, String appPackageName)
    {
    	//String[] appPackageNames = new String[1];
        //appPackageNames[0] = appPackageName;
    	
    	this(ipcFile, new String[] {appPackageName});
    	
    }
    
    public void addJimpleUpdater(JimpleUpdater jimpleUpdater)
    {
    	this.jimpleUpdaters.add(jimpleUpdater);
    }
    
    public void addPostJimpleUpdater(JimpleUpdater jimpleUpdater)
    {
    	this.postJimpleUpdaters.add(jimpleUpdater);
    }
    
    public void setIccProvider(String iccProvider) {
    	this.iccProviderStr = iccProvider;
	}

	public AndroidIPCManager(String ipcFile, String[] appPackageNames)
    {
    	this.appPackageNames = appPackageNames;
    	
    	try {
			setIPCMethods(ipcFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static String[] androidComponents = {
    	"android.content.Context",    //Activity and Service
    	"android.content.BroadcastReceiver",
    	"android.content.ContentProvider"
    };
    
    public void setIPCMethods(String ipcFile) throws IOException {
	    IPCMethodParser ipc_parser = IPCMethodParser.fromFile(ipcFile);
	    System.out.println("add ipc methods!");
	    for (AndroidMethod am: ipc_parser.parse()) {
	        System.out.println("add "+ am.getSignature());
	        ipcAMethods.add(am);
        }
	}
    
    
    //disable the option: enableCallbackSources for AndroidSourceSinkManager
    /*@Override
	public boolean enableCallbackSources() 
    {
		return false;
	}*/

	public Set<AndroidMethod> getIPCMethods() 
    {
	   return ipcAMethods;
	}
    
    @Override
    public boolean isIPCMethod(SootMethod method) {
    	return ICCMethodHelper.isIccMethod(method);
    }

    public void updateJimpleForICC() 
    {
    	for (JimpleUpdater jimpleUpdater : jimpleUpdaters)
    	{
    		jimpleUpdater.updateJimple();
    	}
    	
        try 
        {
        	if (iccProviderStr == null || iccProviderStr.isEmpty())
        	{
        		//System.out.println("epicc provider is used.");
        		iccProvider = new ICCLinksEpiccProvider();
        	}
        	else if (iccProviderStr.equals("configfile"))
        	{
        		System.out.println("config-file provider is used.");
        		iccProvider = new ICCLinksConfigFileProvider();
        	}
        	else
        	{
        		//System.out.println("epicc provider is used.");
        		iccProvider = new ICCLinksEpiccProvider();
        	}

            List<ICCLink> links = iccProvider.getICCLinks(appPackageNames);
            
            for (ICCLink l : links)
            {
            	System.out.println(l);
            }
            
            links = preProcess(links); 	//PRE-PROCESS
            links = process(links);
            postProcess(links);			//POST-PROCESS
            
        } catch (Exception e) 
        {
            System.out.println("exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }

        for (JimpleUpdater postJimpleUpdater : postJimpleUpdaters)
    	{
        	postJimpleUpdater.updateJimple();
    	}
    }

    public List<ICCLink> preProcess(List<ICCLink> links)
    {
    	return links;
    }
    
    public List<ICCLink> process(List<ICCLink> links)
    {
    	for (ICCLink l : links) {
            ICCRedirectionCreator.v().redirectToDestination(l);
        }
    	
    	return links;
    }
    
	public void postProcess(List<ICCLink> links)
    {
    	for (ICCLink link : links)
    	{
    		//removing ICC methods, we need to remove them at the last step, 
    		//otherwise, it may cause some exceptions
    		//ex: one ICC methods belong to multiple ICCLink 
    		//(the first one will delete it, and the next one will get NullPointException)
    		
    		
    		// If the activeBody does not contain the removing statement, the next code does nothing
    		link.getFromSM().retrieveActiveBody().getUnits().remove(link.getFromU());
    		System.out.println(link.getFromSM().retrieveActiveBody());
    	}
    }
}