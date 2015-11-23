#!/bin/bash
set -e

EXPECTED_RESPONSE="1"
RESPONSE=`wget --output-document=/dev/null --quiet --server-response --timeout=5 http://localhost:8800/lbstatus 2>&1 | grep -c '200 OK'`

if [ "$EXPECTED_RESPONSE" = "$RESPONSE" ] && [ $? -eq 0 ] ; then
    exit 0
else
    exit 1
fi
