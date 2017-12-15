#!/bin/sh

echo "Register Client.."
./registerClient.sh 1 &> /dev/null
echo "Add DPN.."
./addDPN.sh 1 &> /dev/null 

echo "Running 100 configure create.."
for (( i=1; i<=100; i++)); do
	./configure.sh create $i 1 &> /dev/null &
done

wait

echo "Running 100 configure delete.."
for (( i=1; i<=100; i++)); do
	./configure.sh delete $i &> /dev/null &
done

wait 

echo "Delete DPN.."
./deleteDPN.sh 1 &> /dev/null
echo "Deregister Client.."
./deregisterClient.sh 1 &> /dev/null
