package org.rpi.pins;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

class MyLogFilter implements ClientRequestFilter {
    private static final Logger LOG = Logger.getLogger(MyLogFilter.class.getName());

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
    	if(requestContext == null) {
    		LOG.log(Level.DEBUG, "requestContext was NULL");
    		return;
    	}
    	if(!requestContext.hasEntity() ) {
        		LOG.log(Level.DEBUG, "requestContext.Entity was NULL: " + requestContext.toString());
        		return;
    	}
        LOG.log(Level.DEBUG, requestContext.getEntity().toString()); // you can configure logging level here
    }
}
