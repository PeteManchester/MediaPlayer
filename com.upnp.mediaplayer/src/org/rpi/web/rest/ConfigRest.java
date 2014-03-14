package org.rpi.web.rest;

import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.rpi.config.Config;

/**
 * Root resource (exposed at "myresource" path)
 * http://localhost:8090/myapp/myresource
 */
@Path("config")
public class ConfigRest {

    private Logger log = Logger.getLogger(this.getClass());

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     * 
     * @return String that will be returned as a text/plain response.
     */
    @Path("getConfig")
    @GET
    @Produces("text/html; charset=utf-8")
    public String getIt() {
        StringBuilder sb = new StringBuilder();
        try {
            Properties pr = new Properties();
            pr.load(new FileInputStream("app.properties"));

            sb.append("{");
            String q = "\"";
            String colon = ":";
            String space = " ";
            String comma = "";
            Enumeration<?> e = pr.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = pr.getProperty(key);
                sb.append(comma + q + convertKey(key) + q + colon + space + q + converValues(value) + q);
                comma = ",";
            }

            sb.append("}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String convertKey(String key) {
        key = key.replaceAll("\\.", "_");
        return key;

    }

    private String converValues(String value) {
        try {
            //value = value.replace('\\', '/');
            value = URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            log.error("Error creating Encoding JSON",e);
        }
        return value;
    }

    @Path("getStatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("{");
            String q = "\"";
            String colon = ":";
            String space = " ";
            String comma = ",";
            sb.append(q + "version" + q + colon + space + q + Config.version + q);
            sb.append(comma + q + "java_version" + q + colon + space + q + System.getProperty("java.version") + q);
            MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = memBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();
            sb.append(comma + q + "memory_heap_used" + q + colon + space + q + heap.getUsed() + q);
            sb.append(comma + q + "memory_nonheap_used" + q + colon + space + q + nonHeap.getUsed() + q);
            com.sun.management.OperatingSystemMXBean mxbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            sb.append(comma + q + "cpu_time" + q + colon + space + q + mxbean.getProcessCpuTime() + q);
            sb.append("}");
        } catch (Exception e) {
            log.error("Error creating Status JSON",e);
        }
        return sb.toString();
    }
}