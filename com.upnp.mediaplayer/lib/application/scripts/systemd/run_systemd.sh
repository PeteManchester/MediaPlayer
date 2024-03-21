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

#andrum993 Generate lib path dynamically, since Java can't do this itself now :(.
arch=`uname -m`
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$SCRIPTPATH/mediaplayer_lib/ohNet/linux/$arch
java -jar "${SCRIPTPATH}"/mediaplayer.jar > /dev/null &
