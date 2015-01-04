package soot.jimple.infoflow.android.iccta.TestApps;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import soot.jimple.infoflow.android.iccta.AndroidIPCManager;
import soot.jimple.infoflow.android.iccta.IccTAPrintStream;
import soot.jimple.infoflow.android.iccta.db.DB;
import soot.jimple.infoflow.android.iccta.jimpleupdater.ApplicationClassSet;
import soot.jimple.infoflow.android.iccta.jimpleupdater.ExtraExtractor;
import soot.jimple.infoflow.android.iccta.jimpleupdater.InfoStatistic;
import soot.jimple.infoflow.android.iccta.jimpleupdater.JimpleIndexNumberTransformer;
import soot.jimple.infoflow.android.iccta.links.ICCLinker;
import soot.jimple.infoflow.android.iccta.stat.Statistics;
import soot.jimple.infoflow.android.iccta.todb.ToDBResultHelper;
import soot.jimple.infoflow.android.iccta.util.Constants;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;

public class Test {

	public static String[] appPackageNames = null;
	public static String appPackageName = "";
	private static String iccProviderStr = null;
	private static String apkPath = null;
	private static String androidJars = null;
	private static boolean enableDB = false;
	
	private static final String APK_COMBINER = "-ac-";
	
	public static void main(final String[] args)
	{
		Options options = initOptions(args);
		validatingParameters(options, args);
		System.setOut(new IccTAPrintStream(System.out));
		
		DB.setJdbcPath("res/jdbc.xml");
		Constants.DB_NAME = extractDBName("res/jdbc.xml");
		
		try
		{
			ProcessManifest processMan = new ProcessManifest(apkPath);
			Test.appPackageName = processMan.getPackageName();
			
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
			
			FDHelper.setIpcManager(ipcManager);
			FDHelper.runAnalysis(apkPath, androidJars);
			
			//OUTPUT Statistics
			outputStatistics();
			
			if (enableDB)
			{
				ToDBResultHelper.toDB(FDHelper.results, FDHelper.cfg, Test.appPackageName);
				ToDBResultHelper.toDBForExtras();
			}
			
			//ResultOutputter.output(FDHelper.cfg, FDHelper.results);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private static String extractDBName(String jdbcPath)
	{
		String rtVal = "cc";
		
		try {
			InputStream inStream = new FileInputStream(new File(jdbcPath));
			
			SAXBuilder sax = new SAXBuilder();
			Document document = sax.build(inStream);
			Element root = document.getRootElement();
			List<Element> dbs = root.getChildren();
			for (Element e : dbs) 
			{
				rtVal = e.getChildText("name");
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return rtVal;
	}
	
	public static void outputStatistics()
	{
		Statistics.output(Test.appPackageName);
		
		for (Map.Entry<String, List<String>> entry : ExtraExtractor.getExtras.entrySet())
		{
			System.out.println("GET: " + entry.getKey());
			System.out.println(entry.getValue());
		}
		for (Map.Entry<String, List<String>> entry : ExtraExtractor.putExtras.entrySet())
		{
			System.out.println("PUT: " + entry.getKey());
			System.out.println(entry.getValue());
		}
	}
	
	@SuppressWarnings("static-access")
	public static Options initOptions(String[] args)
	{
		Options options = new Options();
		
		Option help = new Option("help", "Print this message");
		Option enableDB = new Option("enableDB", "Put the result to db");
		Option apkPath = OptionBuilder.withArgName("apkPath").hasArg().withDescription("Specify the apk path that you want to analyze").create("apkPath");
		Option androidJars = OptionBuilder.withArgName("androidJars").hasArg().withDescription("Spedivy the android jars, e.g., the dir of android-platforms usually used by Soot").create("androidJars");
		Option iccProvider = OptionBuilder.withArgName("iccProvider").hasArg().withDescription("Specify the icc provider, default is Epicc").create("iccProvider");
		Option intentMatchLevel = OptionBuilder.withArgName("intentMatchLevel").hasArg().withDescription("Specify the intent match level: 0 means only explicit Intents, 1 means 0+action/categories, 2 means 1+mimetype and the default 3 means everything; ").create("intentMatchLevel");
		
		
		options.addOption(help);
		options.addOption(enableDB);
		options.addOption(apkPath);
		options.addOption(androidJars);
		options.addOption(iccProvider);
		options.addOption(intentMatchLevel);
		
		options.addOption(new Option(FDHelper.ALIAS_FLOWINS, FDHelper.ALIAS_FLOWINS_DESC));
		options.addOption(new Option(FDHelper.NO_STATIC, FDHelper.NO_STATIC_DESC));
		options.addOption(new Option(FDHelper.NO_CALLBACKS, FDHelper.NO_CALLBACKS_DESC));
		options.addOption(new Option(FDHelper.NO_PATHS, FDHelper.NO_PATHS_DESC));
		Option aplength = OptionBuilder.withArgName(FDHelper.APLENGTH).hasArg().withDescription(FDHelper.APLENGTH_DESC).create(FDHelper.APLENGTH);
		options.addOption(aplength);
		Option pathalgo = OptionBuilder.withArgName(FDHelper.PATH_ALGO).hasArg().withDescription(FDHelper.PATH_ALGO_DESC).create(FDHelper.PATH_ALGO);
		options.addOption(pathalgo);
		//printHelp(options);
		
		return options;
	}
	
	public static void validatingParameters(Options options, String[] args)
	{
		String[] tmp = new String[args.length];
		for (int i = 0; i < args.length; i++)
		{
			tmp[i] = args[i];
		}
		
		try {
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);
			
			if (cmd.hasOption("help"))
			{
				printHelp(options);
			}
			
			if (cmd.hasOption("enableDB"))
			{
				enableDB = true;
			}
			
			if (cmd.hasOption("intentMatchLevel"))
			{
				Constants.INTENT_MATCH_LEVEL = Integer.parseInt(cmd.getOptionValue("intentMatchLevel"));
			}
			
			if (! cmd.hasOption("apkPath"))
			{
				printHelp(options);
			}
			apkPath = cmd.getOptionValue("apkPath");
			
			if (! cmd.hasOption("androidJars"))
			{
				printHelp(options);
			}
			androidJars = cmd.getOptionValue("androidJars");
			
			if (! cmd.hasOption("iccProvider"))
			{
				printHelp(options);
			}
			iccProviderStr = cmd.getOptionValue("iccProvider");
			
			if (cmd.hasOption(FDHelper.NO_CALLBACKS))
			{
				FDHelper.enableCallbacks = false;
			}
			
			if (cmd.hasOption(FDHelper.NO_STATIC))
			{
				FDHelper.staticTracking = false;
			}
			
			if (cmd.hasOption(FDHelper.NO_PATHS))
			{
				FDHelper.computeResultPaths = false;
			}
			
			if (cmd.hasOption(FDHelper.APLENGTH))
			{
				String len = cmd.getOptionValue(FDHelper.APLENGTH);
				FDHelper.accessPathLength = Integer.parseInt(len);
			}
			
			if (cmd.hasOption(FDHelper.PATH_ALGO))
			{
				String algo = cmd.getOptionValue(FDHelper.PATH_ALGO);
				setPathAlgo(algo);
			}
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
	}
	
	static void setPathAlgo(String algo) throws ParseException
	{
		if (algo.equalsIgnoreCase("CONTEXTSENSITIVE"))
			FDHelper.pathBuilder = PathBuilder.ContextSensitive;
		else if (algo.equalsIgnoreCase("CONTEXTINSENSITIVE"))
			FDHelper.pathBuilder = PathBuilder.ContextInsensitive;
		else if (algo.equalsIgnoreCase("SOURCESONLY"))
			FDHelper.pathBuilder = PathBuilder.ContextInsensitiveSourceFinder;
		else {
			System.err.println("Invalid path reconstruction algorithm");
			throw new ParseException("Invalid path reconstruction algorithm");
		}
	}
	
	public static void printHelp(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(200, 
			"java -jar IccTA.jar -apkPath path_to_apk_file -androidJar path_to_android_jar -iccProvider path_to_icc_provider [-help] [-db] [all_params_of_FlowDroid]", 
			"", options, "", false);
		System.exit(1);
	}
}
