#! /bin/sh
# Absolute path to this script, e.g. /home/pi/mediaplayer/run.sh
SCRIPT=$(readlink -f "$0")
# Absolute path this script is in, thus /home/pi/mediaplayer
SCRIPTPATH=$(dirname "$SCRIPT")

cd "${SCRIPTPATH}"
echo ${SCRIPTPATH}

# if the fullscreen plugin is used, please uncomment the following
# lines
#export DISPLAY=:0
#xset s off
#xset -dpms

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SCRIPTPATH/mediaplayer_lib/ohNet/linux/armv6hf
echo Java LibPath: $LD_LIBRARY_PATH
java -jar "${SCRIPTPATH}"/mediaplayer.jar > /dev/null &
