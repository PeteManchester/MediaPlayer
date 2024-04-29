#!/bin/sh
#
# Wrapper script for Pete Mancherster's MediaPlayer,
# an implementation of an OpenHome+UPnP/DLNA media
# renderer.
#
# https://github.com/PeteManchester/MediaPlayer
#

# Change this to where the mediaplayer JAR is located
MEDIAPLAYER_DIR=/home/pi/mediaplayer

cd $MEDIAPLAYER_DIR
#andrum993 Generate lib path dynamically, since Java can't do this itself now :(.
arch=`uname -m`
#mightyoakbob tidy up arch variable to ensure we use an available library
case $arch in
    armv5*)
    arch="armv5sf"
    ;;
    armv6hf*)
    arch="armv6hf"
    ;;
    armv6*)
    arch="armv6sf"
    ;;
    armv7*)
    arch="armv7"
    ;;
esac
echo "$arch"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$MEDIAPLAYER_DIR/mediaplayer_lib/ohNet/linux/$arch
echo Java LibPath: $LD_LIBRARY_PATH
exec java -jar mediaplayer.jar
