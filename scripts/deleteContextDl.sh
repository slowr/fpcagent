#!/bin/bash
curl -X DELETE -u onos:rocks 'http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent:tenants/tenant=default/fpc-mobility/contexts=202374887/dl'
./getTenants.sh