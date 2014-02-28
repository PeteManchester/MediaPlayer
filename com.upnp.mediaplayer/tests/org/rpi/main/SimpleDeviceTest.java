package org.rpi.main;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by triplem on 27.02.14.
 */
public class SimpleDeviceTest {

    @Test
    public void testConstructIconList() {
        StringBuffer sb = new StringBuffer();
        sb.append("<icon>");
        sb.append("<mimetype>image/png</mimetype>");
        sb.append("<width>240</width>");
        sb.append("<height>240</height>");
        sb.append("<depth>24</depth>");
        sb.append("<url>/deviceName/Upnp/resource/org/rpi/image/mediaplayer240.png</url>");
        sb.append("</icon>");
        sb.append("<icon>");
        sb.append("<mimetype>image/jpeg</mimetype>");
        sb.append("<width>240</width>");
        sb.append("<height>240</height>");
        sb.append("<depth>24</depth>");
        sb.append("<url>/deviceName/Upnp/resource/org/rpi/image/mediaplayer240.jpg</url>");
        sb.append("</icon>");
        sb.append("<icon>");
        sb.append("<mimetype>image/png</mimetype>");
        sb.append("<width>120</width>");
        sb.append("<height>120</height>");
        sb.append("<depth>24</depth>");
        sb.append("<url>/deviceName/Upnp/resource/org/rpi/image/mediaplayer120.png</url>");
        sb.append("</icon>");
        sb.append("<icon>");
        sb.append("<mimetype>image/jpeg</mimetype>");
        sb.append("<width>120</width>");
        sb.append("<height>120</height>");
        sb.append("<depth>24</depth>");
        sb.append("<url>/deviceName/Upnp/resource/org/rpi/image/mediaplayer120.jpg</url>");
        sb.append("</icon>");
        sb.append("<icon>");
        sb.append("<mimetype>image/png</mimetype>");
        sb.append("<width>50</width>");
        sb.append("<height>50</height>");
        sb.append("<depth>24</depth>");
        sb.append("<url>/deviceName/Upnp/resource/org/rpi/image/mediaplayer50.png</url>");
        sb.append("</icon>");
        sb.append("<icon>");
        sb.append("<mimetype>image/jpeg</mimetype>");
        sb.append("<width>50</width>");
        sb.append("<height>50</height>");
        sb.append("<depth>24</depth>");
        sb.append("<url>/deviceName/Upnp/resource/org/rpi/image/mediaplayer50.jpg</url>");
        sb.append("</icon>");

        String compareValue = sb.toString();

        SimpleDevice sd = new SimpleDevice(true);

        assertEquals(compareValue, sd.constructIconList("deviceName"));
    }

    public void testConstructIconEntry() {
        StringBuffer sb = new StringBuffer();
        sb.append("<icon>");
        sb.append("<mimetype>image/png</mimetype>");
        sb.append("<width>240</width>");
        sb.append("<height>240</height>");
        sb.append("<depth>24</depth>");
        sb.append("<url>/deviceName/Upnp/resource/org/rpi/image/mediaplayer240.png</url>");
        sb.append("</icon>");

        String compareValue = sb.toString();
        SimpleDevice sd = new SimpleDevice(true);

        assertEquals(compareValue, sd.constructIconEntry("deviceName", "image/jpeg", ".jpg", "50"));
    }
}
