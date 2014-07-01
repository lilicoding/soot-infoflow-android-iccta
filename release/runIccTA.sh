#! /bin/sh

#mysql -uli -pchangeme cc < schema

cp=.
cp=$cp:IccTA_lib/apktool-cli.jar
cp=$cp:IccTA_lib/android.jar
cp=$cp:IccTA_lib/axml-1.0.jar
cp=$cp:IccTA_lib/baksmali-1.3.2.jar
cp=$cp:IccTA_lib/c3p0-0.9.1.2.jar
cp=$cp:IccTA_lib/commons-io-2.4.jar
cp=$cp:IccTA_lib/cos.jar
cp=$cp:IccTA_lib/dexlib2-2.0b5-dev.jar
cp=$cp:IccTA_lib/guava-16.0.1.jar
cp=$cp:IccTA_lib/j2ee.jar
cp=$cp:IccTA_lib/java_cup.jar
cp=$cp:IccTA_lib/jdom-1.0.jar
cp=$cp:IccTA_lib/jdom-2.0.5.jar
cp=$cp:IccTA_lib/jsch-0.1.49.jar
cp=$cp:IccTA_lib/junit.jar
cp=$cp:IccTA_lib/kxml2-2.3.0.jar
cp=$cp:IccTA_lib/log4j-1.2.16.jar
cp=$cp:IccTA_lib/mysql-connector-java-5.1.22-bin.jar
cp=$cp:IccTA_lib/org.hamcrest.core_1.1.0.v20090501071000.jar
cp=$cp:IccTA_lib/polyglot.jar
cp=$cp:IccTA_lib/polyglot_2.jar
cp=$cp:IccTA_lib/slf4j-api-1.7.5.jar
cp=$cp:IccTA_lib/slf4j-simple-1.7.5.jar
cp=$cp:IccTA_lib/AXMLPrinter2.jar
cp=$cp:IccTA.jar

./execute_with_limit_time.sh java -cp $cp lu.uni.serval.iccta.TestApps.Main $1
