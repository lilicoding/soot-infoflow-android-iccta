#! /bin/sh

DIR=$1
DUR=600

for path in `ls $DIR`
do
	file=$DIR/$path
	echo $file
	gtimeout $DUR ./runEpicc.sh $file > output_iccta/$path.txt 2>&1
done
