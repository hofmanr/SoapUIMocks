#!/bin/bash
# Start SoapUI Mockservice

# Determine absolute path to this script
SCRIPT=$(readlink -f "$0")
BASE=$(dirname "$SCRIPT")

# Source config
. "${BASE}/config"

# Declare PID as integer
declare -i PID

# Allows all userID's to modfify/delete logfiles
umask 000

# Get PID of SoupUI MockServer process
getPID()
{
  PID=$(pgrep -u ${USER} -a | grep "java.*${INSTANCE}" | grep -v grep | cut -d ' ' -f1)
}

# Create logfile directory
LOGDIR="${LOGBASE}/$(date '+%Y%m%d%H%M%S')"
if [ ! -d "$LOGDIR" ] ; then
  mkdir -p "$LOGDIR"
fi

# PATH to logfile
logfile="${LOGDIR}/MockRunner.log"

# Unset DISPLAY variable
export DISPLAY=''

# SoapUI HOME and CLASSPATH
export SOAPUI_HOME=${HOME}/Software/SoapUI-5.6.0
SOAPUI_CLASSPATH=$SOAPUI_HOME/bin/soapui-5.6.0.jar:$SOAPUI_HOME/lib/*
export SOAPUI_CLASSPATH

if [ -f "$SOAPUI_HOME/jre/bin/java" ]
then
  JAVA=$SOAPUI_HOME/jre/bin/java
else
    if [ -f "$SOAPUI_HOME/../../PlugIns/jre.bundle/Contents/Home/jre/bin/java" ]
    then
        JAVA=$SOAPUI_HOME/../../PlugIns/jre.bundle/Contents/Home/jre/bin/java
    else
        JAVA=java
    fi
fi

# JVM options
JAVA_OPTS="-Xms128m -Xmx1024m -Dsoapui.properties=soapui.properties -Dsoapui.home=$SOAPUI_HOME -Djavax.net.ssl.trustStore=${PROJECT_HOME}/cacerts"
if [ $SOAPUI_HOME != "" ]
then
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.libraries=$SOAPUI_HOME/bin/ext"
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.listeners=$SOAPUI_HOME/bin/listeners"
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.actions=$SOAPUI_HOME/bin/actions"
fi
export JAVA_OPTS


# Stop previously started MockService
cd "$BASE"
./stopMockService.sh

# Navigate to log directory
cd "$LOGDIR"

# Start MockServer
nohup $JAVA $JAVA_OPTS -cp $SOAPUI_CLASSPATH com.eviware.soapui.tools.SoapUIMockServiceRunner -p "$PORT" -DINSTANCE="${INSTANCE}" -m "${MOCKSERVICE}" "${BASE}/${PROJECT_XML}" &>  "$logfile" &

# Check if MockServer is started succesfully
sleep 5
getPID
if [ "$PID" -gt 0 ] ; then
  exit 0
else
  exit 1
fi


