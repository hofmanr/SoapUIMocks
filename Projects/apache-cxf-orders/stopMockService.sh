#!/bin/bash
# Stop SoapUI Mockservice

# Determine absolute path to this script
SCRIPT=$(readlink -f "$0")
BASE=$(dirname "$SCRIPT")

# Source config
. "${BASE}/config"

declare -i PID

# Stop MockService via Soap Request
result=$(curl --silent --show-error --header "Content-Type: text/xml;charset=UTF-8" --data @"$STOP_REQUEST" http://localhost:${PORT}/${SERVICE_URL} 2>&1)
# TODO : check response ?

sleep 5

# Stop MockService if still running
PID=$(pgrep -u ${USER} -a | grep "java.*${INSTANCE}" | grep -v grep | cut -d ' ' -f1)
if [ "$PID" -gt 0 ] ; then
  kill $PID
fi


