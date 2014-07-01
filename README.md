=======
IccTA
=====
IccTA: An Inter-Component Communication based Taint Analysis Tool

Usage: java -jar IccTA.jar -apkPath path_to_apk_file -androidJar path_to_android_jar -iccProvider path_to_icc_provider [-help] [-db] [all_params_of_FlowDroid]
 -aliasflowins                          This option makes the alias search flow-insensitive and may generate more false positives, but on the other hand can greatly reduce runtime for large
                                        applications
 -androidJars <androidJars>             Spedivy the android jars, e.g., the dir of android-platforms usually used by Soot
 -apkPath <apkPath>                     Specify the apk path that you want to analyze
 -aplength <aplength>                   Sets the maximum access path length to n. The default is 5. In general, larger values make the analysis more precise, but also more expensive
 -enableDB                              Put the result to db
 -help                                  Print this message
 -iccProvider <iccProvider>             Specify the icc provider, default is Epicc
 -intentMatchLevel <intentMatchLevel>   Specify the intent match level: 0 means only explicit Intents, 1 means 0+action/categories, 2 means 1+mimetype and the default 3 means everything;
 -nocallbacks                           Disables the emulation of Android callbacks (button clicks, GPS location changes, etc.) This option reduces the runtime, but may miss some leaks
 -nopaths                               Just shows which sources are connected to which sinks, but does not reconstruct exact propagation paths. Note that this option does not affect precision. It
                                        just disables the additional path processing
 -nostatic                              Disables tracking static fields. Makes the analysis faster, but may also miss some leaks


The simple guide to run the source code of IccTA.

1> Using the source code of FlowDroid from:

* https://github.com/secure-software-engineering/soot-infoflow-android.git
* https://github.com/secure-software-engineering/soot-infoflow.git

2> The best practice to run IccTA is importing all the source project to eclipse (including infoflow, infoflow-android, Soot, Heros, Jasmin).

3> In the IccTA's directory, there is a directory named iccProvider. Under this directory, two tools are available currently (epicc and ice). 
In their specific directory, you can find their launch script, just feel free to launch them first. 
Either epicc or ic3 is OK for the current IccTA version.

4> Launch IccTA, you can always follow the help script of IccTA to analyse Android applications.
An example is shown as follows:

-apkPath path_of_app.apk  -androidJars path_of_android-platforms -iccProvider epicc -enableDB -intentMatchLevel 3

* Please note that there are some hard-code in the execution script when you launch Epicc or IC3.
* And before launch IccTA, check the configuration files under 'res' directory to custom appreciate parameters.
