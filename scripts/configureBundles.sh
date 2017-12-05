#!/bin/bash

if [ "$#" -eq 3 ]; then
    echo ""
    curl -X POST --header 'Content-Type: application/json' -u onos:rocks --header 'Accept: application/json' -d '{
        "input": {
            "client-id": "1",
            "highest-op-ref-scope": "bundle",
            "bundles": [
                {
                    "admin-state": "enabled",
                    "client-id": "1",
                    "contexts": [
                        {
                            "context-id": '$2',
                            "delegating-ip-prefixes": [
                                "192.168.1.5/32"
                            ],
                            "dl": {
                                "dpn-parameters": {},
                                "mobility-tunnel-parameters": {
                                    "tunnel-identifier": "2222",
                                    "tunnel-type": "gtpv1"
                                },
                                "tunnel-local-address": "192.168.1.1",
                                "tunnel-remote-address": "10.1.1.1"
                            },
                            "dpn-group": "foo",
                            "dpns": [
                                {
                                    "direction": "uplink",
                                    "dpn-id": '$3',
                                    "dpn-parameters": {}
                                }
                            ],
                            "ebi": "5",
                            "imsi": "9135551234",
                            "instructions": {
                                "instr-3gpp-mob": "session uplink"
                            },
                            "lbi": "5",
                            "ul": {
                                "dpn-parameters": {},
                                "mobility-tunnel-parameters": {
                                    "tunnel-identifier": "1111",
                                    "tunnel-type": "gtpv1"
                                },
                                "tunnel-local-address": "192.168.1.1",
                                "tunnel-remote-address": "10.1.1.1"
                            }
                        }
                    ],
                    "op-id": "1",
                    "op-ref-scope": "op",
                    "op-type": '$1',
                    "session-state": "complete"
                }
            ]
        }
    }' 'http://localhost:8181/onos/restconf/operations/ietf-dmm-fpcagent:configure-bundles' | python -m json.tool
    echo ""
else
    echo "usage: "$0" type contextId dpnId"
fi