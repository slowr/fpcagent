#!/bin/bash

if [ "$#" -eq 1 ]; then
    echo ""
    curl -i --header "Content-type: application/json" --request POST -u onos:rocks --data '{
        "tenants": [
            {
                "fpc-mobility": {},
                "fpc-policy": {},
                "fpc-topology": {},
                "tenant-id": "'$1'"
            }
        ]
    }' 'http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent'
    echo ""
else
    echo "usage: "$0" tenantId"
fi
