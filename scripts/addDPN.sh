curl -i --header "Content-type: application/json" --request POST -u onos:rocks --data '{
    "dpns": [
        {
            "dpn-id": "1",
            "dpn-name": "site1-anchor1",
            "dpn-groups": [
                "foo"
            ],
            "node-id": "node1",
            "network-id": "network1"
        }
    ]
}' 'http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent:tenants/tenant=default/fpc-topology'
