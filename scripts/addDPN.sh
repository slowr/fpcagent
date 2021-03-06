#!/bin/bash

if [ "$#" -eq 1 ]; then
    echo ""
    curl -i --header "Content-type: application/json" --request POST -u onos:rocks --data '{
        "dpns": [
            {
                "dpn-id": '$1',
                "dpn-name": "site1-anchor1",
                "dpn-groups": [
                    "foo"
                ],
                "node-id": "node'$1'",
                "network-id": "network'$1'",
                "control-protocol": "zmq-dpn-control-protocol",
                "abstract": false
            }
        ]
    }' 'http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent:tenants/tenant=default/fpc-topology'
    ./getTenants.sh
    echo ""
elif [ "$#" -eq 2 ]; then
    echo ""
    curl -i --header "Content-type: application/json" --request POST -u onos:rocks --data '{
        "dpns": [
            {
                "dpn-id": '$1',
                "dpn-name": "site1-anchor1",
                "dpn-groups": [
                    "foo"
                ],
                "node-id": "node'$1'",
                "network-id": "network'$1'",
                "control-protocol": "zmq-dpn-control-protocol",
                "abstract": false
            }
        ]
    }' 'http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent:tenants/tenant='$2'/fpc-topology'
    ./getTenants.sh
    echo ""
else
    echo "usage: "$0" dpnId (tenantId)"
fi
