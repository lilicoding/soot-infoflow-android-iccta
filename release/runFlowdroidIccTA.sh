#! /bin/sh

android_jars=`cat res/iccta.properties | grep android_jars | cut -d '=' -f2`

name=$1
appName=`basename $name`
# change the directory path of android-platforms
./execute_with_limit_time.sh java -jar Flowdroid-IccTA.jar $name $android_jars leaked_apps leaked_icc_apps > output/$appName.txt  2>&1
