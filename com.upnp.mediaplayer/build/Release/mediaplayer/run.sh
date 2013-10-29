#!/bin/sh


DIRNAME="$( dirname "$0" )"
cd "${DIRNAME}"
export LD_LIBRARY_PATH=/usr/local/lib/
java -jar /home/pi/mediaplayer/mediaplayer.jar &
_wlanexist=$(ifconfig | grep wlan) || true
if [ "$_wlanexist" ]; then
	iwconfig wlan0 power off
fi
exit 0
