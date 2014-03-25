# Setup MediaPlayer on ArchLinux (Raspberry Pi)

Since I am using ArchLinux this installation instruction covers this distribution.

## Setup ArchLinux

```
[Downloads]$ sudo dd bs=1M if=ArchLinuxARM-2014.03-rpi.img of=/dev/mmcblk0
```

see http://archlinuxarm.org/platforms/armv6/raspberry-pi for additional hints

```
ssh root@alarmpi
```

## Setup Java

If you do not like to use the fullscreen display, the package jre7-openjdk-headless
is enough ;-)

```
[root@alarmpi]# pacman -S jre7-openjdk
```

## Setup MPD

```
[root@alarmpi]# pacman -S mpd
```

An example configuration is shown here.

```
user "mpd"
pid_file "/run/mpd/mpd.pid"
#db_file "/var/lib/mpd/mpd.db"
state_file "/var/lib/mpd/mpdstate"
playlist_directory "/var/lib/mpd/playlists"

audio_output {
    type "alsa"
    name "PiALSA"
    device "hw:0,0"
}
```

Please note, that the default db is disabled, and MPD is configured to use the
"usual" audio output of the Raspberry Pi. If you are using a different USB
Audio Device, this should most probably be adopted.

Enable automatic startup of mpd after a reboot.

```
systemctl enable mpd.service
```

## Setup MediaPlayer

We are going to use the latest beta version in this install. If you would like to
use a "stable" version, please replace "beta" with "release".

```
[root@alarmpi]# pacman -S binutils
[root@alarmpi]# pacman -S unzip
[root@alarmpi]# wget -O mediaplayer.zip https://github.com/PeteManchester/MediaPlayer/blob/master/com.upnp.mediaplayer/download/beta/mediaplayer.zip?raw=true
[root@alarmpi]# unzip mediaplayer.zip
[root@alarmpi]# mv mediaplayer /opt
[root@alarmpi]# cd /opt/mediaplayer
[root@alarmpi]# chmod +x run.s
```

The app.properties needs to get adopted, so that it fits your needs. An example is
shown beneath.

```
friendly.name=Raspberry Pi (Wohnzimmer)
#player can be either mpd or mplayer
player=mpd
#playlist.max, maximum items allow in playlist. Maximum is 1000
playlist.max=1000
save.local.playlist=true
enableAVTransport=true
enableReceiver=false
#mplayer settings
mplayer.playlist=asx,b4s,kpl,m3u,pls,ram,rm,smil,wax,wvx
mplayer.path=/usr/bin/mplayer
mplayer.cache=520
mplayer.cache_min=80
#mpd settings
mpd.host=localhost
mpd.port=6600
mpd.preload.timer=3
#log settings
log.file=/var/log/mediaplayer.log
#off,debug,info,warn,error,fatal
log.file.level=warn
log.console.level=off
#OpenHome settings
#Log Level can be: None, Trace, Thread, Network, Timer, SsdpMulticast, SsdpUnicast, Http, Device,
#XmlFetch, Service, Event, Topology, DvInvocation, DvEvent, DvWebSocket, Bonjour, DvDevice, Error, All, Verbose
openhome.debug.level=Error
openhome.port=52821
```

The above configuration uses MPD as the media playing device.

### Enable MediaPlayer on Boot

```
[root@alarmpi]# cd /opt/mediaplayer/scripts/systemd
[root@alarmpi]# cp mediaplayer.service /etc/systemd/system/
[root@alarmpi]# cp run_systemd.sh /opt/mediaplayer
[root@alarmpi]# chmod +x /opt/mediaplayer/run_systemd.sh
```

Adopt the mediaplayer.service in /etc/systemd/system/multi-user.target.wants to
reflect your local environment:

```
[Unit]
Description=MediaPlayer
Requires=mpd.service
After=network.target mpd.service

[Service]
Type=forking
ExecStart=/opt/mediaplayer/run_systemd.sh

[Install]
WantedBy=multi-user.target
```

Now start the mediaplayer service:

```
[root@alarmpi]# systemctl start mediaplayer.service
```

## Enable Plugins

MediaPlayer provides a lot of [Plugins](https://github.com/PeteManchester/MediaPlayer/wiki)
which need to be installed separately and on request. In the following we enable the
LastFM Plugin to scrobble the played tracks.

```
[root@alarmpi]# cd /opt/mediaplayer
[root@alarmpi]# mkdir -p plugins/LastFM
[root@alarmpi]# cd plugins/LastFM
[root@alarmpi]# wget -O LastFM.jar https://github.com/PeteManchester/MediaPlayer/raw/master/com.upnp.mediaplayer/plugins/LastFM/LastFM.jar
[root@alarmpi]# wget -O LastFM.xml https://github.com/PeteManchester/MediaPlayer/raw/master/com.upnp.mediaplayer/plugins/LastFM/LastFM.xml
```

For the LastFM plugin to work, you need to adopt the LastFM.xml according to your
personal preferences. For a detailed description, please see the
[LastFM Documentation](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-LastFM).

## Adjust Date/Time Settings

The timezone of the default install of ArchlinuxARM is America/Denver. If you are
in a different timezone, you need to adjust this.

```
[root@alarmpi]# timedatectl set-timezone Europe/Berlin
```

For detailled information, please take a look on the [ArchWiki](https://wiki.archlinux.org/index.php/time).
