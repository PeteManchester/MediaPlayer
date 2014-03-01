package org.rpi.utils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utility class with some helpers for XML-Handling.
 *
 */
public class XMLUtils {

    /***
     * Retrieves the elements with name from the given element. The method checks, if the
     * element is emtpy, if it is, an empty string is returned, the textcontent is returned
     * otherwise.
     *
     * @param element
     * @param name
     * @return
     */
    public static String getStringFromElement(Element element, String name) {
        String res = "";
        NodeList nid = element.getElementsByTagName(name);
        if (nid != null) {
            Element fid = (Element) nid.item(0);
            if (fid != null) {
                res = fid.getTextContent();
                // log.debug("ElementName: " + name + " Value: " + res);
                return res;

            }
        }
        return res;
    }

    /***
     * Retrieves the elements with name from the given element. The method checks, if the
     * element is emtpy, if it is, the default value is returned, the textcontent is returned
     * otherwise.
     *
     * @param element
     * @param name
     * @param defaultValue
     * @return
     */
    public static String getStringFromElement(Element element, String name, String defaultValue) {
        String res = XMLUtils.getStringFromElement(element, name);
        if (Utils.isEmpty(res)) res = defaultValue;
        return res;
    }

}
