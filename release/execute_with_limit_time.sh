#! /bin/bash

date
$* &
pid=$!
# 300 means 300 seconds, 5 minuates is enough to run Soot-based all programmings.
((lim = 1000))
while [[ $lim -gt 0 ]] ; do
    sleep 1
    proc=$(ps -ef | awk -v pid=$pid '$2==pid{print}{}')
    echo $proc
    ((lim = lim - 1))
    if [[ -z "$proc" ]] ; then
            ((lim = -9))
    fi
done
date
if [[ $lim -gt -9 ]] ; then
    kill -9 $pid
fi
date
