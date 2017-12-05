#!/bin/bash
curl -u onos:rocks http://localhost:8181/onos/restconf/data/ietf-dmm-fpcagent:fpc-agent-info | python -m json.tool
