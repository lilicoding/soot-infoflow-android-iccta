#! /bin/sh

DIR=$1
DUR=600
AndroidJars=/Users/li.li/Project/github/android-platforms

for app in `ls $DIR`
do
	echo $DIR/$app

	gtimeout $DUR ./runIC3.sh $DIR/$app

done
