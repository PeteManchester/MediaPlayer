#!/bin/sh


DIRNAME="$( dirname "$0" )"
cd "${DIRNAME}"
export LD_LIBRARY_PATH=/usr/local/lib/
java -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=192.168.1.240 -jar /home/pi/mediaplayer/mediaplayer.jar
exit 0