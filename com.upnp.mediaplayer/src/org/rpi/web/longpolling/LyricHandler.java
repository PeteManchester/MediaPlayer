

package org.rpi.web.longpolling;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.comet.CometEvent;
import org.glassfish.grizzly.comet.DefaultCometHandler;

public class LyricHandler extends DefaultCometHandler<HttpServletResponse> {
	private Logger log = Logger.getLogger(this.getClass());
    private HttpServletResponse httpResponse;
    private String result = "Empty";

    LyricHandler(HttpServletResponse httpResponse) {
    	//log.debug("Create LyricHandler");
        this.httpResponse = httpResponse;
    }

    public void onEvent(CometEvent event) throws IOException {
    	//log.debug("OnEvent: " + event.getType());
        if (CometEvent.Type.NOTIFY == event.getType()) {
        	
        	result = (String)event.getCometContext().getAttribute("Test");
            PrintWriter writer = httpResponse.getWriter();
            writer.write(result);            
            writer.flush();
            event.getCometContext().resumeCometHandler(this);
        }
    }

    public void onInterrupt(CometEvent event) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        //writer.write(result);
        writer.flush();
    }
}