#!/bin/bash

if [ "$#" -eq 2 ]; then
    echo ""
    json='{
            "input": {
                "op-id": "3",
                "targets": [
                    {
                        "target": "/ietf-dmm-fpcagent:tenants/tenant=default/fpc-mobility/contexts='$2'"
                    }
                ],
                "client-id": "1",
                "session-state": "complete",
                "admin-state": "enabled",
                "op-type": "'$1'",
                "op-ref-scope": "none"
            }
        }'

    curl -X POST \
        --header 'Content-Type: application/json' \
        -u onos:rocks \
        --header 'Accept: application/json' \
        -d "$json" \
        'http://localhost:8181/onos/restconf/operations/ietf-dmm-fpcagent:configure' | python -m json.tool
else
    echo "usage: "$0" type contextId"
fi