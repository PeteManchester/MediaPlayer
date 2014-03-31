package org.rpi.plugin.lastfm;

import org.junit.Test;
import org.rpi.plugin.lastfm.configmodel.LastFMConfigModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LastFMPluginImplTest {

    @Test
    public void testLastFMConfigModel() throws JAXBException {
        org.rpi.plugin.lastfm.configmodel.LastFMConfigModel model = new org.rpi.plugin.lastfm.configmodel.LastFMConfigModel();
        org.rpi.plugin.lastfm.configmodel.LastFMConfig config = new org.rpi.plugin.lastfm.configmodel.LastFMConfig();
        config.setUserName("testUser");
        config.setPassword("testPassword");
        config.setProxyIP("anIPNumber");
        config.setProxyPort("80");
        config.setProxyType(Proxy.Type.HTTP);

        org.rpi.plugin.lastfm.configmodel.BlackList blackList = new org.rpi.plugin.lastfm.configmodel.BlackList();
        blackList.setArtist("UnknownArtist");
        blackList.setTitle("ATitle");

        List<org.rpi.plugin.lastfm.configmodel.BlackList> blackListList = new ArrayList<org.rpi.plugin.lastfm.configmodel.BlackList>();
        blackListList.add(blackList);

        model.setConfig(config);
        model.setBlackList(blackListList);

        JAXBContext context = JAXBContext.newInstance(org.rpi.plugin.lastfm.configmodel.LastFMConfigModel.class);
        Marshaller marshaller = context.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        File testFile = null;
        try {
            testFile = File.createTempFile("marshaltest", "xml");
        } catch (IOException e) {
            assertTrue("No exception should be thrown here", false);
        }

        assertNotNull(testFile);

        marshaller.marshal(model, testFile);

        model = null;

        assertNull(model);

        Unmarshaller unmarshaller = context.createUnmarshaller();
        model = (LastFMConfigModel) unmarshaller.unmarshal(testFile);

        assertNotNull(model);

        assertEquals("testUser", model.getConfig().getUserName());
        assertEquals(Proxy.Type.HTTP, model.getConfig().getProxyType());
        assertEquals(1, model.getBlackList().size());

    }
}
