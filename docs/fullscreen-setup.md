# Fullscreen Plugin

To use the Fullscreen plugin, some additional steps to the already described
[installation instructions](/docs/archlinux-setup.html) are required.

Since I am using ArchLinux this installation instruction covers this distribution.

## Setup X11

```
[root@alarmpi]# pacman -Syu
[root@alarmpi]# pacman -S packer
[root@alarmpi]# pacman -S xorg-server xorg-xinit xorg-utils xorg-server-utils
[root@alarmpi]# pacman -S xf86-video-fbdev xterm
[root@alarmpi]# pacman -S gtk2
[root@alarmpi]# packer -S ttf-ms-fonts
```

## Setup Auto-Login

follow https://wiki.archlinux.org/index.php/Systemd/User#D-Bus

For the next steps, you need to install some additional packages:

```
[root@alarmpi]# pacman -S make
[root@alarmpi]# packer -S xlogin-git
```

follow https://wiki.archlinux.org/index.php/Systemd/User#Automatic_login_into_Xorg_without_display_manager

The .xinitrc-file has to be created in the /root-folder and should look something
like the following:

```
#!/bin/sh
#
# ~/.xinitrc
#
# Executed by startx (run your window manager from here)

if [ -d /etc/X11/xinit/xinitrc.d ]; then
  for f in /etc/X11/xinit/xinitrc.d/*; do
    [ -x "$f" ] && . "$f"
  done
  unset f
fi

exec xterm
```

You could run 'startx' to check, if everything up until now is working.

Adopt, like described in the mentioned wiki-entry, your .bashrc inside the /root-folder
and enable the systemctl service with

```
systemctl enable xlogin@root
```

Do not forget to enable the lingering:

```
loginctl enable-linger root
```

After a reboot, X should be loaded and the XTerm Window should be shown on the screen.

## Enable Fullscreen Plugin

```
[root@alarmpi]# cd /opt/mediaplayer
[root@alarmpi]# mkdir -p plugins/Fullscreen
[root@alarmpi]# cd plugins/Fullscreen
[root@alarmpi]# wget -O Fullscreen.jar https://github.com/PeteManchester/MediaPlayer/raw/master/com.upnp.mediaplayer/plugins/Fullscreen/Fullscreen.jar
[root@alarmpi]# wget -O fullscreen.properties https://github.com/PeteManchester/MediaPlayer/raw/master/com.upnp.mediaplayer/plugins/Fullscreen/fullscreen.properties
```

Some minor adoptions on the run_systemd.sh-file is required:

```
#! /bin/sh
# Absolute path to this script, e.g. /home/pi/mediaplayer/run.sh
SCRIPT=$(readlink -f "$0")
# Absolute path this script is in, thus /home/pi/mediaplayer
SCRIPTPATH=$(dirname "$SCRIPT")

cd "${SCRIPTPATH}"
echo ${SCRIPTPATH}

# if the fullscreen plugin is used, please uncomment the following
# lines
export DISPLAY=:0.0
xset s off
xset -dpms

java -jar "${SCRIPTPATH}"/mediaplayer.jar > /dev/null &
```
