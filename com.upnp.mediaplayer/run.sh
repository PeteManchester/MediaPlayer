#!/bin/sh

# Absolute path to this script, e.g. /home/pi/mediaplayer/run.sh
SCRIPT=$(readlink -f "$0")
# Absolute path this script is in, thus /home/pi/mediaplayer
SCRIPTPATH=$(dirname "$SCRIPT")

TURNOFF_WLAN=true

DIRNAME="$( dirname "$0" )"
cd "${DIRNAME}"
#andrum993 Generate lib path dynamically, since Java can't do this itself now :(
arch=`uname -m`
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SCRIPTPATH/mediaplayer_lib/ohNet/linux/$arch
echo Java LibPath: $LD_LIBRARY_PATH
java -jar $SCRIPTPATH/mediaplayer.jar &
_wlanexist=$(ifconfig | grep wlan) || true
if [ "$_wlanexist" ] && [ "$TURNOFF_WLAN" ]; then
	iwconfig wlan0 power off
fi
exit 0
