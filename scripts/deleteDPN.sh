#!/bin/bash
if [ "$#" -eq 1 ]; then
    echo ""
	curl -X DELETE -u onos:rocks 'http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent:tenants/tenant=default/fpc-topology/dpns='$1
	./getTenants.sh
elif [ "$#" -eq 2 ]; then
    echo ""
	curl -X DELETE -u onos:rocks 'http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent:tenants/tenant='$2'/fpc-topology/dpns='$1
	./getTenants.sh
else
    echo "usage: "$0" dpnId (tenantId)"
fi