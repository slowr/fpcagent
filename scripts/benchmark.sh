#!/bin/sh

./addDPN.sh 1 &> /dev/null 

for (( i=1; i<=1000; i++)); do
	./configureCreateOrUpdate.sh create $i 1 &> /dev/null &
	if ! (($i % 100)); then
		wait
	fi
done

for (( i=1; i<=1000; i++)); do
	./configureDeleteOrQuery.sh delete $i &> /dev/null &
	if ! (($i % 100)); then
		wait
	fi
done

./deleteDPN.sh 1 &> /dev/null 