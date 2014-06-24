---
template: home
---

# MediaPlayer - Java Based Open Home UPnP Media Renderer

A fully open-source java project that uses the [OpenHome API](http://www.openhome.org/wiki/Oh:Overview) to
implement a UPNP Media Renderer.

The MediaPlayer is Java based and so will work on a wide range of hardware and Operating Systems without the need to provide different install packages or recompile the code.  

## Features

MediaPlayer provides a large support of OpenHome specific devices as well as
standard UPnP devices.

To playback Media files, [mplayer](http://www.mplayerhq.hu/)
as well as [mpd](http://www.musicpd.org/) is used (based on your choice, this is
[configurable](https://github.com/PeteManchester/MediaPlayer/wiki/MediaPlayer-Options)).

The combination of OpenHome as well as mpd does allow MediaPlayer to provide gapless
playback (yes, gapless, for beneath 0€ ;-)).

We support the following UPnP and OpenHome Functionality:

* [PlayLists](http://www.openhome.org/wiki/Av:Developer:PlaylistService)
* [Radio](http://www.openhome.org/wiki/Av:Developer:RadioService)
* [SongCast](http://www.linn.co.uk/software#songcast)

Also supported is Apples AirPlay Receiver. 

We have implemented a http-daemon to make it possible to:  

* Configure and control the whole application via a web-interface. 
* View the last 50 log events.
* Display info about the currently playing track, providing info such as lyrics and info about the artist.
* And if using the AlarmClock plugin you can also set a sleep timer from the web page.

![](http://i.imgur.com/2J1CLQZ.png)

![](http://i.imgur.com/sg5hXFX.png)

![](http://i.imgur.com/olsOZBr.png)

The MediaPlayer is easily extendable via [Plugins](https://github.com/PeteManchester/MediaPlayer/wiki).

Right now, we have the following Plugins:

* [AlarmClock](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-AlarmClock)
* [LastFM](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-LastFM)
* [Fullscreen](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-Fullscreen)
* [LIRCIntegration](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-LIRCIntegration)
* [LCDDisplay](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-Display)

  
![](http://i.imgur.com/DPVST6T.jpg)  

## Wiki

We have a [Wiki](https://github.com/PeteManchester/MediaPlayer/wiki) where you can find the documentation

## Installation Instructions

* [Raspberry Pi / Debian](https://github.com/PeteManchester/MediaPlayer/wiki/Install-Raspberry-Pi)
* [Raspberry Pi / ArchLinux](/MediaPlayer/docs/archlinux-setup.html)
* [Fullscreen Plugin / ArchLinux](/MediaPlayer/docs/fullscreen-setup.html)

## UPnP Support

MediaPlayer supports the following UPNP devices:

### Media Servers

* [Asset UPNP](http://www.dbpoweramp.com/asset-upnp-dlna.htm)
* [MinimServer](http://minimserver.com/)

### Control Points

* Kinsky
* Assent Control
* PlugPlayer
* eLyric
* Lumin
* BubbleUPnP/BubbleDS

# Contributors

MediaPlayer is made possible due to the support of the following great human beings.

<div class="table-responsive">
  <table class="table table-striped table-condensed">
    <thead>
      <tr>
        <th>Name</th>
        <th>Contribution</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>Pete Hoyle</td>
        <td>Original Source Code</td>
      </tr>
      <tr>
        <td>Ian Grant</td>
        <td>Service Scripts</td>
      </tr>
      <tr>
        <td>Markus M May</td>
        <td>OSManager, LastFM Plugin, Fullscreen Plugin, this webpage</td>
      </tr>
    </tbody>
  </table>
</div>

The AirPlay implementation is based on the [RPlay](https://github.com/bencall/RPlay) source code by Benjamin de Callatay

If you are interested, we always welcome feedback or suggestions or if you want you could fork the project and apply your own changes.

Feedback and suggestions can be submitted here:

https://github.com/PeteManchester/MediaPlayer/issues?state=open

If you appreciate the Java MediaPlayer please support:

[Linn] (http://www.linn.co.uk/) Who produce top end Audio equipment whilst providing OpenSource Software. They also have their own record label. 

[OpenHome] (http://www.openhome.org/wiki/OhNet) Who provide the ohNet API on which this MediaPlayer is based and who have provided great support whilst I have been developing the MediaPlayer.

