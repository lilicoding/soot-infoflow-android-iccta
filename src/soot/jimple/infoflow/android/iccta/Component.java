package soot.jimple.infoflow.android.iccta;

import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointConstants;

public class Component 
{
    String name;
    
    public Component(String name) {
        this.name = name;
    }

    public static ComponentType getComponentType(SootClass currentClass) 
    {
    	ComponentType type = ComponentType.Plain;
		List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
		for(SootClass sc : extendedClasses) {
			if(sc.getName().equals(AndroidEntryPointConstants.APPLICATIONCLASS))
				type = ComponentType.Application;
			else if(sc.getName().equals(AndroidEntryPointConstants.ACTIVITYCLASS))
				type = ComponentType.Activity;
			else if(sc.getName().equals(AndroidEntryPointConstants.SERVICECLASS))
				type = ComponentType.Service;
			else if(sc.getName().equals(AndroidEntryPointConstants.BROADCASTRECEIVERCLASS))
				type = ComponentType.BroadcastReceiver;
			else if(sc.getName().equals(AndroidEntryPointConstants.CONTENTPROVIDERCLASS))
				type = ComponentType.ContentProvider;
		}
		
		return type;
	}
    
    public enum ComponentType {
		Application,
		Activity,
		Service,
		BroadcastReceiver,
		ContentProvider,
		Plain
	}
}
