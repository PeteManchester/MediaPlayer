

package org.rpi.web.longpolling;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.glassfish.grizzly.comet.CometEvent;
import org.glassfish.grizzly.comet.DefaultCometHandler;

public class LyricHandler extends DefaultCometHandler<HttpServletResponse> {

    private HttpServletResponse httpResponse;
    private String result = "Empty";

    LyricHandler(HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public void onEvent(CometEvent event) throws IOException {
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
        writer.flush();
    }
}