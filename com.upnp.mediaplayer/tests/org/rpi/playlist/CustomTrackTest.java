package org.rpi.playlist;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by triplem on 24.02.14.
 */
public class CustomTrackTest {

    @Test
    public void testGetTrackDetails() {
        String metadata = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
                "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\" xmlns:sec=\"http://www.sec.co.kr/\">" +
                "<item id=\"/external/audio/albums/1/138\" parentID=\"/external/audio/albums/1\" restricted=\"1\">" +
                "<upnp:class>object.item.audioItem.musicTrack</upnp:class><dc:title>Captain Future</dc:title>" +
                "<dc:creator>Christian Bruhn</dc:creator><upnp:artist>Christian Bruhn</upnp:artist>" +
                "<upnp:albumArtURI>http://somehost:57645/external/audio/albums/1.jpg</upnp:albumArtURI>" +
                "<upnp:albumArtURI dlna:profileID=\"JPEG_TN\">http://somehost:57645/external/audio/albums/1.jpg</upnp:albumArtURI>" +
                "<upnp:genre>Soundtrack</upnp:genre><dc:date>1994-01-01</dc:date><upnp:album>Captain Future</upnp:album>" +
                "<upnp:originalTrackNumber>1</upnp:originalTrackNumber><ownerUdn>2a5fbfbd-ba05-c32c-0000-0000559321ae</ownerUdn>" +
                "<res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000\" " +
                "bitrate=\"8000\" size=\"1264962\" duration=\"0:02:34.000\">http://somehost:57645/external/audio/media/138.mp3</res></item></DIDL-Lite>";

        CustomTrack ct = new CustomTrack("anURI", metadata, 4711);

        assertEquals("Captain Future", ct.getAlbum());
        assertEquals("Christian Bruhn", ct.getArtist());
        assertEquals("http://somehost:57645/external/audio/albums/1.jpg", ct.getAlbumArtUri());
    }
}