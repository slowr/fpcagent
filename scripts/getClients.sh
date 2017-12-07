#!/bin/bash
curl -u onos:rocks http://localhost:8181/onos/restconf/data/fpc:connection-info | python -m json.tool
