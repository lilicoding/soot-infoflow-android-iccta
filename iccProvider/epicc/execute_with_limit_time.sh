#! /bin/bash

num=300

echo "RUN $* within $num seconds" >> timer.txt
echo "START: `date`" >> timer.txt

time $* &
pid=$!

((lim = $num))
while [[ $lim -gt 0 ]] ; do
    sleep 1
    proc=$(ps -ef | awk -v pid=$pid '$2==pid{print}{}')
    #echo $proc
    ((lim = lim - 1))
    if [[ -z "$proc" ]] ; then
            ((lim = -9))
    fi
done

if [[ $lim -gt -9 ]] ; then
    kill -9 $pid
fi

echo "END: `date`" >> timer.txt
