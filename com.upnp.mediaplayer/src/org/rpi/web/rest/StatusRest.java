package org.rpi.web.rest;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.rpi.config.Config;

@Path("status")
public class StatusRest {
	
	private Logger log = Logger.getLogger(this.getClass());
	
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
			sb.append(q + "version" + q + colon + space + q + Config.getInstance().getVersion() + q);
			sb.append(comma + q + "java_version" + q + colon + space + q + System.getProperty("java.version") + q);
			sb.append(comma + q + "mp_starttime" + q + colon + space + q + Config.getInstance().getStartTime() + q);
			sb.append(comma + q + "mp_currenttime" + q + colon + space + q + new Date() + q);
			MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
			MemoryUsage heap = memBean.getHeapMemoryUsage();
			MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();
			sb.append(comma + q + "memory_heap_used" + q + colon + space + q + heap.getUsed() + q);
			sb.append(comma + q + "memory_nonheap_used" + q + colon + space + q + nonHeap.getUsed() + q);
			com.sun.management.OperatingSystemMXBean mxbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			sb.append(comma + q + "cpu_time" + q + colon + space + q + mxbean.getProcessCpuTime() + q);
			sb.append(comma + q + "log_events" + q + colon + space + q + Config.getInstance().getLoggingEvents() + q);
			sb.append("}");
		} catch (Exception e) {
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}

}
