#!/bin/sh


DIRNAME="$( dirname "$0" )"
cd "${DIRNAME}"
export LD_LIBRARY_PATH=/usr/local/lib/
java  -jar /home/pi/mediaplayer/mediaplayer.jar &

exit 0
