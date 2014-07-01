#! /bin/sh

mysql -uli -pchangeme cc < schema

for name in `cat $1`;
do
	echo $name
	appName=`basename $name`

	./runIccTA.sh $name > output/$appName.txt 2>&1

done
