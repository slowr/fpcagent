#!/bin/bash
curl -u onos:rocks http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent:tenants | python -m json.tool
