#!/bin/sh

rm -rf create.log delete.log

./addDPN.sh 1 &> /dev/null 

for (( i=1; i<=100; i++)); do
	./configure.sh create $i 1 >> create.log 2> /dev/null &
	if ! (($i % 10)); then
		wait
	fi
done

wait

for (( i=1; i<=100; i++)); do
	./configure.sh delete $i >> delete.log 2> /dev/null &
	if ! (($i % 10)); then
		wait
	fi
done

./deleteDPN.sh 1 &> /dev/null 
