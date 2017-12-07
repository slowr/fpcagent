#!/bin/bash

if [ "$#" -eq 1 ]; then
    echo ""
    curl -i -s \
    --header "Content-type: application/json" \
    --request POST \
    -u onos:rocks \
    --data '{
        "input": {
            "client-id": "'$1'"
        }
    }' 'http://localhost:8181/onos/restconf/operations/fpc:deregister-client'
    echo ""
else
    echo "usage: "$0" clientId"
fi
