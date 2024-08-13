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
#andrum993 Generate lib path dynamically, since Java can't do this itself now :(
ARCH=`uname -m`
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
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$MEDIAPLAYER_DIR/mediaplayer_lib/ohNet/linux/$arch
echo Java LibPath: $LD_LIBRARY_PATH
exec java -jar mediaplayer.jar
