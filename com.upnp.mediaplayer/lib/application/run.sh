#!/bin/sh

# Absolute path to this script, e.g. /home/pi/mediaplayer/run.sh
SCRIPT=$(readlink -f "$0")
# Absolute path this script is in, thus /home/pi/mediaplayer
SCRIPTPATH=$(dirname "$SCRIPT")

TURNOFF_WLAN=true

DIRNAME="$( dirname "$0" )"
cd "${DIRNAME}"
#andrum993 Generate lib path dynamically, since Java can't do this itself now :(
ARCH=$(uname -m)
#mightyoakbob tidy up arch variable to ensure we use an available library
case $ARCH in
    armv5*)
    ARCH="armv5sf"
    ;;
    armv6hf*)
    ARCH="armv6hf"
    ;;
    armv6*)
    ARCH="armv6sf"
    ;;
    armv7*)
    ARCH="armv7"
    ;;
    x86_64*)
    ARCH="amd64"
esac
echo "$ARCH"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SCRIPTPATH/mediaplayer_lib/ohNet/linux/$ARCH
echo Java LibPath: $LD_LIBRARY_PATH
java -jar $SCRIPTPATH/mediaplayer.jar &
_wlanexist=$(ifconfig | grep wlan) || true
if [ "$_wlanexist" ] && [ "$TURNOFF_WLAN" ]; then
	iwconfig wlan0 power off
fi
exit 0
