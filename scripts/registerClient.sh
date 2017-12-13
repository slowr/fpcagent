#!/bin/bash

if [ "$#" -eq 2 ]; then
    echo ""
    curl -i -s \
    --header "Content-type: application/json" \
    --request POST \
    -u onos:rocks \
    --data '{
        "input": {
            "client-id": "'$1'",
            "tenant-id": "'$2'",
            "supported-features": [
                "urn:ietf:params:xml:ns:yang:fpcagent:fpc-bundles",
                "urn:ietf:params:xml:ns:yang:fpcagent:operation-ref-scope",
                "urn:ietf:params:xml:ns:yang:fpcagent:fpc-agent-assignments",
                "urn:ietf:params:xml:ns:yang:fpcagent:instruction-bitset"
            ],
    	"endpoint-uri": "http://127.0.0.1:9997/"
        }
    }' 'http://localhost:8181/onos/restconf/operations/fpc:register-client'
    echo ""
elif [ "$#" -eq 1 ]; then
    echo ""
    curl -i -s \
    --header "Content-type: application/json" \
    --request POST \
    -u onos:rocks \
    --data '{
        "input": {
            "client-id": "'$1'",
            "tenant-id": "default",
            "supported-features": [
                "urn:ietf:params:xml:ns:yang:fpcagent:fpc-bundles",
                "urn:ietf:params:xml:ns:yang:fpcagent:operation-ref-scope",
                "urn:ietf:params:xml:ns:yang:fpcagent:fpc-agent-assignments",
                "urn:ietf:params:xml:ns:yang:fpcagent:instruction-bitset"
            ],
        "endpoint-uri": "http://127.0.0.1:9997/"
        }
    }' 'http://localhost:8181/onos/restconf/operations/fpc:register-client'
    echo ""
else
    echo "usage: "$0" clientId tenantId"
fi
