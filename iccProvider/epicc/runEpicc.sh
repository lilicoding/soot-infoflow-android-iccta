#! /bin/sh

HOST=localhost
DB=cc
USERNAME=li
PASSWORD=changeme

ANDROID_JARS=../../android-platforms
APP=$1

rm -rf testspace/*

java -Xmx8192m -jar Epicc.jar $ANDROID_JARS $APP $HOST $DB $USERNAME $PASSWORD

rm -rf testspace/*
