#!/sbin/runscript
# Copyright 1999-2011 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: /var/cvsroot/gentoo-x86/media-video/motion/files/motion.initd-r2,v 1.1 2011/10/02 18:48:35 ssuominen Exp $

DESC="OpenHome MediaPlayer"
NAME=mediaplayer.sh
DAEMON=/usr/local/bin/$NAME
PIDFILE=/var/run/$NAME.pid
LOGFILE=/var/log/$NAME.log
SCRIPTNAME=/etc/init.d/$NAME
USER=mpd
GROUP=audio


extra_started_commands="reload"

_create_file_structure() {
        touch $PIDFILE $LOGFILE                                                                                                       
        chown root:$GROUP $PIDFILE $LOGFILE                                                                                           
        chmod 660 $PIDFILE $LOGFILE
}

depend() {
        need modules
        after mpd
}

start() {
        _create_file_structure

        ebegin "Starting openhome mediaplayer"
        start-stop-daemon --start -u ${USER} -g ${GROUP} --quiet --make-pidfile --pidfile $PIDFILE --background --exec ${DAEMON}
        eend $?
}

stop() {
        ebegin "Stopping openhome mediaplayer"
        start-stop-daemon --stop --quiet --retry=TERM/30/KILL/5 --pidfile $PIDFILE
        eend $?                                                                                                                       
} 