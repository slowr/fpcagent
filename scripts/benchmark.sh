#!/bin/sh

rm -rf create.log delete.log

./registerClient.sh 1 default &> /dev/null
./addDPN.sh 1 &> /dev/null 

for (( i=1; i<=100; i++)); do
	./configure.sh create $i 1 &> /dev/null &
	if ! (($i % 10)); then
		wait
	fi
done

wait

for (( i=1; i<=100; i++)); do
	./configure.sh delete $i &> /dev/null &
	if ! (($i % 10)); then
		wait
	fi
done

./deleteDPN.sh 1 &> /dev/null
./deregisterClient.sh 1 &> /dev/null
