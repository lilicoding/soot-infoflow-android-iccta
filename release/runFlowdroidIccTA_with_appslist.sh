#! /bin/sh

android_jars=`cat res/iccta.properties | grep android_jars | cut -d '=' -f2`

for name in `cat $1`;
do
	echo $name
	appName=`basename $name`
	./execute_with_limit_time.sh java -jar Flowdroid-IccTA.jar $name $android_jars leaked_apps leaked_icc_apps > output/$appName.txt  2>&1
	
done
