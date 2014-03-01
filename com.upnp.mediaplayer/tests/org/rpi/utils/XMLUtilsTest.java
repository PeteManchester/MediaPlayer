package org.rpi.utils;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 */
public class XMLUtilsTest {

    @Test
    public void testGetElementTest() throws Exception {
        String testValue = "<TrackList>" +
                "    <Entry>" +
                "        <Id>11</Id>" +
                "        <Metadata>Somevalue</Metadata>" +
                "        <EmptyData></EmptyData>" +
                "    </Entry>" +
                "</TrackList>";

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document doc = null;

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Reader car = new CharArrayReader(testValue.toCharArray());
        InputSource source = new InputSource(car);
        doc = documentBuilder.parse(source);

        NodeList listOfChannels = doc.getElementsByTagName("Entry");
        for (int s = 0; s < listOfChannels.getLength(); s++) {
            Node channel = listOfChannels.item(s);
            Element element = (Element) channel;

            assertEquals("11", XMLUtils.getElementTest(element, "Id"));
            assertEquals("Somevalue", XMLUtils.getElementTest(element, "Metadata", "default"));
            assertEquals("default", XMLUtils.getElementTest(element, "NotAvailableElement", "default"));
            assertEquals("default", XMLUtils.getElementTest(element, "EmptyData", "default"));
        }

    }
}
