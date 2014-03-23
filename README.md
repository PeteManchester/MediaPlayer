---
template: home
---

# MediaPlayer - Java Based Open Home UPnP Media Renderer

A full open-source java project that uses the [OpenHome API](http://www.openhome.org/wiki/Oh:Overview) to
implement a UPNP Media Renderer.

## Features

MediaPlayer does provide a large support for OpenHome specific devices as well as
standard UPnP devices.

To playback Media files, [mplayer](http://www.mplayerhq.hu/)
as well as [mpd](http://www.musicpd.org/) is used (based on your choice, this is
[configurable](https://github.com/PeteManchester/MediaPlayer/wiki/MediaPlayer-Options)).

The combination of OpenHome as well as mpd does allow MediaPlayer to provide gapless
playback (yes, gapless, for beneath 0€ ;-)).

We do support the following UPnP and OpenHome Devices:

* [PlayLists](http://www.openhome.org/wiki/Av:Developer:PlaylistService)
* [Radio](http://www.openhome.org/wiki/Av:Developer:RadioService)
* [SongCast](http://www.linn.co.uk/software#songcast)

We are currently implementing a http-daemon to make it possible to configure and
control the whole application via a web-interface.

The MediaPlayer is easily extensible via [Plugins](https://github.com/PeteManchester/MediaPlayer/wiki).
Right now, we do offer the following Plugins:

* [AlarmClock](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-AlarmClock)
* [LastFM](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-LastFM)
* [LCDDisplay](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-Display)
* [Fullscreen](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-Fullscreen)
* [LIRCIntegration](https://github.com/PeteManchester/MediaPlayer/wiki/Plugins-LIRCIntegration)

## UPnP Support

MediaPlayer does support the following UPNP devices:

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

If you are interested, please fork the project and apply your changes. Every Pull
Request is greatly welcome.
