package org.rpi.config;

import org.apache.log4j.Level;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigTest {

    @Test
    public void testGetLogLevel() {
        assertEquals(Level.DEBUG, Config.getLogLevel(null));

        assertEquals(Level.TRACE, Config.getLogLevel("TRACE"));
        assertEquals(Level.DEBUG, Config.getLogLevel("DEBUG"));
        assertEquals(Level.INFO, Config.getLogLevel("INFO"));
        assertEquals(Level.ALL, Config.getLogLevel("ALL"));
        assertEquals(Level.WARN, Config.getLogLevel("WARN"));
        assertEquals(Level.ERROR, Config.getLogLevel("ERROR"));
        assertEquals(Level.FATAL, Config.getLogLevel("FATAL"));
        assertEquals(Level.OFF, Config.getLogLevel("OFF"));
    }
}