#!/bin/sh

# Absolute path to this script, e.g. /home/pi/mediaplayer/run.sh
SCRIPT=$(readlink -f "$0")
# Absolute path this script is in, thus /home/pi/mediaplayer
SCRIPTPATH=$(dirname "$SCRIPT")

TURNOFF_WLAN=true

DIRNAME="$( dirname "$0" )"
cd "${DIRNAME}"
#export LD_LIBRARY_PATH=/usr/local/lib/
java -jar $SCRIPTPATH/mediaplayer.jar &
_wlanexist=$(ifconfig | grep wlan) || true
if [ "$_wlanexist" ] && [ "$TURNOFF_WLAN" ]; then
	iwconfig wlan0 power off
fi
exit 0
