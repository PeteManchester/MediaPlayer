package org.rpi.airplay;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RaopSessionManager {
  private static Map<String, AudioSession> map = new HashMap<String, AudioSession>();
  private static Logger log = Logger.getLogger("RaopSessionManager");

  public static AudioSession getSession(String sessionId) {
    return map.get(sessionId);
  }

  public static void addSession(String sessionId, AudioSession session) {
    map.put(sessionId, session);
  }

  public static void shutdownSession(String sessionId) {
    log.debug("Shutdown Session");
    AudioSession session = getSession(sessionId);
    if(session == null) {
    	log.debug("Session not found " + sessionId);
      return;
    }
    map.remove(session);
  }
}
