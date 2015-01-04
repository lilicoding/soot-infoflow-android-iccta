package soot.jimple.infoflow.android.iccta.TestApps;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.xmlpull.v1.XmlPullParserException;

import soot.jimple.infoflow.android.AndroidSourceSinkManager.LayoutMatchingMode;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.ipc.IIPCManager;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.solver.IInfoflowCFG;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.infoflow.taintWrappers.TaintWrapperSet;

public class FDHelper 
{
	public static final String ALIAS_FLOWINS = "aliasflowins";
	public static final String ALIAS_FLOWINS_DESC = "This option makes the alias search flow-insensitive and may generate more false positives, but on the other hand can greatly reduce runtime for large applications";
	
	public static final String APLENGTH = "aplength";
	public static final String APLENGTH_DESC = "Sets the maximum access path length to n. The default is 5. In general, larger values make the analysis more precise, but also more expensive";
	
	public static final String NO_STATIC = "nostatic";
	public static final String NO_STATIC_DESC = "Disables tracking static fields. Makes the analysis faster, but may also miss some leaks";
	
	public static final String NO_CALLBACKS = "nocallbacks";
	public static final String NO_CALLBACKS_DESC = "Disables the emulation of Android callbacks (button clicks, GPS location changes, etc.) This option reduces the runtime, but may miss some leaks";
	
	public static final String NO_PATHS = "nopaths";
	public static final String NO_PATHS_DESC = "Just shows which sources are connected to which sinks, but does not reconstruct exact propagation paths. Note that this option does not affect precision. It just disables the additional path processing";
	
	public static final String PATH_ALGO = "pathalgo";
	public static final String PATH_ALGO_DESC = "Specifies the path reconstruction algorithm to be used. Please referring to FlowDroid for more details about this parameter";
	
	public static boolean enableCallbacks = true;
	public static boolean staticTracking = true;
	public static boolean computeResultPaths = true;
	public static int accessPathLength = 5;
	public static PathBuilder pathBuilder = PathBuilder.ContextSensitive;
	
	public static IInfoflowCFG cfg = null;
	public static InfoflowResults results = null;
	
	
	private static boolean stopAfterFirstFlow = false;
	private static boolean implicitFlows = false;
	private static boolean enableExceptions = true;
	private static LayoutMatchingMode layoutMatchingMode = LayoutMatchingMode.MatchSensitiveOnly;
	private static boolean flowSensitiveAliasing = true;
	private static boolean aggressiveTaintWrapper = false;
	private static boolean librarySummaryTaintWrapper = false;
	private static String summaryPath = "";
	
	
	private static IIPCManager ipcManager = null;
	public static void setIpcManager(IIPCManager ipcManager) {
		FDHelper.ipcManager = ipcManager;
	}

	public static InfoflowResults runAnalysis(final String fileName, final String androidJar) {
		try {
			final long beforeRun = System.nanoTime();
			
			//IccTA
			final SetupApplication app;
			if (null == ipcManager)
			{
				app = new SetupApplication(androidJar, fileName);
			}
			else
			{
				app = new SetupApplication(androidJar, fileName, ipcManager);
			}

			app.setStopAfterFirstFlow(stopAfterFirstFlow);
			app.setEnableImplicitFlows(implicitFlows);
			app.setEnableStaticFieldTracking(staticTracking);
			app.setEnableCallbacks(enableCallbacks);
			app.setEnableExceptionTracking(enableExceptions);
			app.setAccessPathLength(accessPathLength);
			app.setLayoutMatchingMode(layoutMatchingMode);
			app.setFlowSensitiveAliasing(flowSensitiveAliasing);
			app.setComputeResultPaths(computeResultPaths);
			app.setEnableCallbackSources(false);
			app.setPathBuilder(pathBuilder);
			
			final ITaintPropagationWrapper taintWrapper;
			if (librarySummaryTaintWrapper) {
				taintWrapper = createLibrarySummaryTW();
			}
			else {
				//final EasyTaintWrapper easyTaintWrapper;
				//if (new File("../soot-infoflow/EasyTaintWrapperSource.txt").exists())
					//easyTaintWrapper = new EasyTaintWrapper("../soot-infoflow/EasyTaintWrapperSource.txt");
				//else
					//easyTaintWrapper = new EasyTaintWrapper("EasyTaintWrapperSource.txt");
				EasyTaintWrapper easyTaintWrapper = new EasyTaintWrapper("res/EasyTaintWrapperSource.txt");
				easyTaintWrapper.setAggressiveMode(aggressiveTaintWrapper);
				taintWrapper = easyTaintWrapper;
			}
			app.setTaintWrapper(taintWrapper);
			app.calculateSourcesSinksEntrypoints("res/SourcesAndSinks.txt");
			
				
			System.out.println("Running data flow analysis...");
			final InfoflowResults res = app.runInfoflow(new ResultHelper());
			System.out.println("Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");
			return res;
		} catch (IOException ex) {
			System.err.println("Could not read file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (XmlPullParserException ex) {
			System.err.println("Could not read Android manifest file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ITaintPropagationWrapper createLibrarySummaryTW() throws IOException 
	{
		try {
			Class clzLazySummary = Class.forName("soot.jimple.infoflow.methodSummary.data.impl.LazySummary");
			
			Object lazySummary = clzLazySummary.getConstructor(File.class).newInstance(new File(summaryPath));
			
			ITaintPropagationWrapper summaryWrapper = (ITaintPropagationWrapper) Class.forName
					("soot.jimple.infoflow.methodSummary.taintWrappers.SummaryTaintWrapper").getConstructor
					(clzLazySummary).newInstance(lazySummary);
			
			final TaintWrapperSet taintWrapperSet = new TaintWrapperSet();
			taintWrapperSet.addWrapper(summaryWrapper);
			taintWrapperSet.addWrapper(new EasyTaintWrapper("EasyTaintWrapperConversion.txt"));
			return taintWrapperSet;
		}
		catch (ClassNotFoundException | NoSuchMethodException ex) {
			System.err.println("Could not find library summary classes: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex) {
			System.err.println("Could not initialize library summaries: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException | InstantiationException ex) {
			System.err.println("Internal error in library summary initialization: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
	
	static class ResultHelper implements ResultsAvailableHandler
	{
		@Override
		public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results) 
		{
			FDHelper.cfg = cfg;
			FDHelper.results = results;
			
		}
	}
}
