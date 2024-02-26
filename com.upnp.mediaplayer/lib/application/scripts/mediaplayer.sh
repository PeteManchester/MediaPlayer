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
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$$MEDIAPLAYER_DIR/mediaplayer_lib/ohNet/linux/armv6hf
echo Java LibPath: $LD_LIBRARY_PATH
exec java -jar mediaplayer.jar
