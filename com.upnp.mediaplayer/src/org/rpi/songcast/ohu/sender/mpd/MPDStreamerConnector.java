package org.rpi.songcast.ohu.sender.mpd;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import org.apache.log4j.Logger;
import org.rpi.config.Config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 * {@link System#out} to test {@link HttpSnoopServer}.
 */
public final class MPDStreamerConnector implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());
	private EventLoopGroup group = new NioEventLoopGroup();
	private Channel ch = null;

	public void start() throws InterruptedException, URISyntaxException, SSLException {
		String URL = "http://127.0.0.1:" + Config.getInstance().getMpdListenPort();
		log.debug("Starting MPD StreamerConnector: " + URL);
		URI uri = new URI(URL);
		String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
		String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
		int port = uri.getPort();
		if (port == -1) {
			if ("http".equalsIgnoreCase(scheme)) {
				port = 80;
			} else if ("https".equalsIgnoreCase(scheme)) {
				port = 443;
			}
		}

		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			log.error("Only HTTP(S) is supported.");
			return;
		}

		// Configure SSL context if necessary.
		final boolean ssl = "https".equalsIgnoreCase(scheme);
		final SslContext sslCtx;
		if (ssl) {
			sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} else {
			sslCtx = null;
		}

		// Configure the client.

		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(new MPDStreamerConnectorInitializer(sslCtx));
			boolean bConnect = true;
			while (bConnect) {
				try {
					// Make the connection attempt.
					ch = b.connect(host, port).sync().channel();
					bConnect = false;
				} catch (Exception e) {
					log.error("Error Conneting to MPD: ", e);
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException ex) {
						log.error("Error Sleeping", ex);
					}
				}
			}

			// Prepare the HTTP request.
			HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath(), Unpooled.EMPTY_BUFFER);
			request.headers().set(HttpHeaderNames.HOST, host);
			request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
			request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

			// Set some example cookies.
			/*
			 * request.headers().set( HttpHeaderNames.COOKIE,
			 * ClientCookieEncoder.STRICT.encode( new DefaultCookie("my-cookie",
			 * "foo"), new DefaultCookie("another-cookie", "bar")));
			 */

			// Send the HTTP request.
			ch.writeAndFlush(request);

			// Wait for the server to close the connection.
			ch.closeFuture().sync();
		} catch (Exception e) {
			log.error("Error Connecting: ", e);
			stop();
			throw e;
		} finally {
			// Shut down executor threads to exit.
			group.shutdownGracefully();
		}
	}

	@Override
	public void run() {

		try {
			log.debug("MPDConnector Run");
			start();
		} catch (Exception e) {
			log.error("Error Starting MPDStreamerConnector", e);
		} finally {

		}

	}

	public void stop() {
		log.debug("Stopping MPDStreamer Connection");

		try {
			if (ch != null) {
				ch.close();
				log.debug("Channel Closed");
			}
		} catch (Exception e) {
			log.error("Error Close Connection", e);
		}
		try {
			group.shutdownGracefully(1,2,TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("Error Group Shutdown", e);
		}

		try {
			group.terminationFuture();
		} catch (Exception e) {
			log.error("Error Group Terminate", e);
		}
		log.debug("Stopped MPDStreamer Connection");
	}
}