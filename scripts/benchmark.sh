#!/bin/sh

./addDPN.sh 1 &> /dev/null 

for (( i=1; i<=100; i++)); do
	./configureCreateOrUpdate.sh create $i 1 &> /dev/null &
	if ! (($i % 10)); then
		wait
	fi
done

#for (( i=1; i<=100; i++)); do
#	./configureDeleteOrQuery.sh delete $i &> /dev/null &
#	if ! (($i % 10)); then
#		wait
#	fi
#done

./deleteDPN.sh 1 &> /dev/null 
