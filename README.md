<<<<<<< HEAD
<<<<<<< HEAD
IccTA: An Inter-Component Communication based Taint Analysis Tool

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
=======
IccTA
=====

The source code of IccTA will be eventurally open source in this project.

For simple usage of IccTA, please refer https://sites.google.com/site/icctawebpage/usage-of-iccta.
The more details will come soon.
>>>>>>> c948859639279d4f88aee837a05a6e046e5cb2b8
=======
soot-infoflow-android-iccta
===========================
>>>>>>> df191d1edcdc42bca2ffc14a5bdad38fbc946dd6
