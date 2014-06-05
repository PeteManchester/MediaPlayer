package org.rpi.web.longpolling;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEngine;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;

public class LongPollingServlet extends HttpServlet implements Observer {

	private Logger log = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;

	private String contextPath = null;

	private Thread threadEvents = null;
	private WorkqeueEvents eventHandler = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ServletContext context = config.getServletContext();
		contextPath = context.getContextPath() + "/playerstatus";
		
		eventHandler = new WorkqeueEvents(contextPath);
		threadEvents = new Thread(eventHandler,"EventHandler" );
		threadEvents.start();

		CometEngine engine = CometEngine.getEngine();
		CometContext cometContext = engine.register(contextPath);
		cometContext.setExpirationDelay(5 * 30 * 1000);
		PlayManager.getInstance().observeInfoEvents(this);
		PlayManager.getInstance().observeTimeEvents(this);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		CometEngine engine = CometEngine.getEngine();
		CometContext<HttpServletResponse> context = engine.getCometContext(contextPath);
		LyricHandler handler = new LyricHandler(res);
		final int hash = context.addCometHandler(handler);
		// log.debug("ContextHandler Added: " + hash);
		// checkUpdate();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		CometContext<HttpServletResponse> context = CometEngine.getEngine().getCometContext(contextPath);
		context.notify(null);
		PrintWriter writer = res.getWriter();		
		writer.write(eventHandler.getJson());
		writer.flush();
	}
	
	@Override
	public void destroy() {
		if(eventHandler !=null)
		{
			eventHandler.stop();
			eventHandler = null;
		}
		if(threadEvents !=null)
		{
			threadEvents = null;
		}
	}

	@Override
	public void update(Observable o, Object e) {
		CometEngine engine = CometEngine.getEngine();
		CometContext<HttpServletResponse> contexts = engine.getCometContext(contextPath);
		int count = contexts.getCometHandlers().size();
		EventBase base = (EventBase) e;
		eventHandler.put(base);
		
	}


}