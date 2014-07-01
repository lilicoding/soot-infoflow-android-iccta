#! /bin/sh

HOST=localhost
DB=cc
USERNAME=li
PASSWORD=changeme

ANDROID_JARS=/Users/li.li/Project/github/android-platforms/android-18/android.jar
APP=$1

rm -rf testspace/*

java -Xmx8192m -jar Epicc.jar $ANDROID_JARS $APP $HOST $DB $USERNAME $PASSWORD

rm -rf testspace/*
