#!/bin/bash
curl -X POST --header 'Content-Type: application/json' -u onos:rocks --header 'Accept: application/json' -d '{
    "input": {
        "op-id": "3",
        "targets": [
            {
                "target": "/ietf-dmm-fpcagent:tenants/tenant/default/fpc-mobility/contexts/202374885"
            }
        ],
        "client-id": "1",
        "session-state": "complete",
        "admin-state": "enabled",
        "op-type": "delete",
        "op-ref-scope": "none"
    }
}' 'http://localhost:8181/onos/restconf/operations/ietf-dmm-fpcagent:configure'
