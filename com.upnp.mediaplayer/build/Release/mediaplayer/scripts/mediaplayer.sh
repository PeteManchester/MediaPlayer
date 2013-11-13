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
exec java -jar mediaplayer.jar
