package org.frameworkset.boot.socket;

import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.core.ExtensionConfig;
import org.eclipse.jetty.websocket.core.WebSocketComponents;
import org.eclipse.jetty.websocket.core.server.WebSocketMappings;
import org.eclipse.jetty.websocket.core.server.WebSocketServerComponents;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.frameworkset.http.ServerHttpRequest;
import org.frameworkset.http.ServerHttpResponse;
import org.frameworkset.http.ServletServerHttpRequest;
import org.frameworkset.http.ServletServerHttpResponse;
import org.frameworkset.spi.Lifecycle;
import org.frameworkset.spi.support.NamedThreadLocal;
import org.frameworkset.util.Assert;
import org.frameworkset.util.ClassUtils;
import org.frameworkset.util.CollectionUtils;
import org.frameworkset.web.servlet.context.ServletContextAware;
import org.frameworkset.web.socket.container.RequestUpgradeStrategy;
import org.frameworkset.web.socket.handler.HandshakeFailureException;
import org.frameworkset.web.socket.inf.WebSocketExtension;
import org.frameworkset.web.socket.inf.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link RequestUpgradeStrategy} for use with Jetty 9.0-9.3. Based on Jetty's
 * internal {@code org.eclipse.jetty.websocket.server.WebSocketHandler} class.
 *
 * @author Phillip Webb
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class Jetty10RequestUpgradeStrategy implements RequestUpgradeStrategy, Lifecycle, ServletContextAware {

    private static Logger log = LoggerFactory.getLogger(Jetty10RequestUpgradeStrategy.class);
	// Pre-Jetty 9.3 init method without ServletContext
	private static final Method webSocketFactoryInitMethod =
			ClassUtils.getMethodIfAvailable(JettyWebSocketServletFactory.class, "init");

	private static final ThreadLocal<WebSocketHandlerContainer> wsContainerHolder =
			new NamedThreadLocal<WebSocketHandlerContainer>("WebSocket Handler Container");

    private WebSocketMappings mapping;
    private WebSocketComponents components;
	private Jetty10WebSocketServletFactory factory;

	private volatile List<WebSocketExtension> supportedExtensions;

	private ServletContext servletContext;

	private volatile boolean running = false;

    public static ThreadLocal<WebSocketHandlerContainer> getWsContainerHolder() {
        return wsContainerHolder;
    }

    public void configure(JettyWebSocketServletFactory factory)
      {
//        factory.setDefaultMaxFrameSize(4096);
          Jetty10WebSocketServletFactory jetty10WebSocketServletFactory =(Jetty10WebSocketServletFactory)factory;
//          jetty10WebSocketServletFactory.addMapping("/", (req,res)-> null);
      }
	/**
	 * Default constructor that creates {@link JettyWebSocketServletFactory} through
	 * its default constructor thus using a default {@link WebSocketPolicy}.
	 */
	public Jetty10RequestUpgradeStrategy() {
//		this(new JettyWebSocketServletFactory());
        this.factory = new Jetty10WebSocketServletFactory();
	}

   

	/**
	 * A constructor accepting a {@link JettyWebSocketServletFactory}.
	 * This may be useful for modifying the factory's {@link WebSocketPolicy}
	 */
	 void initJettyRequestUpgradeStrategy(String path) throws ServletException {
		Assert.notNull(factory, "WebSocketServerFactory must not be null");
         try
         {
//            ServletContext servletContext = getServletContext();

             components = WebSocketServerComponents.getWebSocketComponents(servletContext);
             mapping = new WebSocketMappings(components);
             factory.setComponents(components);
             factory.setMapping(mapping);
             factory.setServletContext(servletContext);
             factory.addMapping(path);
//            
//            String max = getInitParameter("idleTimeout");
//            if (max == null)
//            {
//                max = getInitParameter("maxIdleTime");
//                if (max != null)
//                    LOG.warn("'maxIdleTime' init param is deprecated, use 'idleTimeout' instead");
//            }
//            if (max != null)
//                factory.setIdleTimeout(Duration.ofMillis(Long.parseLong(max)));
//
//            max = getInitParameter("maxTextMessageSize");
//            if (max != null)
//                factory.setMaxTextMessageSize(Long.parseLong(max));
//
//            max = getInitParameter("maxBinaryMessageSize");
//            if (max != null)
//                factory.setMaxBinaryMessageSize(Long.parseLong(max));
//
//            max = getInitParameter("inputBufferSize");
//            if (max != null)
//                factory.setInputBufferSize(Integer.parseInt(max));
//
//            max = getInitParameter("outputBufferSize");
//            if (max != null)
//                factory.setOutputBufferSize(Integer.parseInt(max));
//
//            max = getInitParameter("maxFrameSize");
//            if (max == null)
//                max = getInitParameter("maxAllowedFrameSize");
//            if (max != null)
//                factory.setMaxFrameSize(Long.parseLong(max));
//
//            String autoFragment = getInitParameter("autoFragment");
//            if (autoFragment != null)
//                factory.setAutoFragment(Boolean.parseBoolean(autoFragment));

             configure(factory); // Let user modify customizer prior after init params
         }
         catch (Throwable x)
         {
             throw new ServletException(x);
         }
//         JettyWebSocketServletFactory factory = new JettyWebSocketServletFactory(this.servletContext);
//         
//         factory.setCreator(new WebSocketCreator() {
//			@Override
//			public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
//				// Cast to avoid infinite recursion
//				return createWebSocket((UpgradeRequest) request, (UpgradeResponse) response);
//			}
//			// For Jetty 9.0.x
//			public Object createWebSocket(UpgradeRequest request, UpgradeResponse response) {
//				WebSocketHandlerContainer container = wsContainerHolder.get();
//				Assert.state(container != null, "Expected WebSocketHandlerContainer");
//				response.setAcceptedSubProtocol(container.getSelectedProtocol());
//				response.setExtensions(container.getExtensionConfigs());
//				return container.getHandler();
//			}
//		});
	}


	@Override
	public String[] getSupportedVersions() {
		return new String[] { String.valueOf(13) };
	}

	@Override
	public List<WebSocketExtension> getSupportedExtensions(ServerHttpRequest request) {
		if (this.supportedExtensions == null) {
			this.supportedExtensions = getWebSocketExtensions();
		}
		return this.supportedExtensions;
	}

	private List<WebSocketExtension> getWebSocketExtensions() {
		List<WebSocketExtension> result = new ArrayList<WebSocketExtension>();
		for (String name : this.factory.getAvailableExtensionNames()) {
			result.add(new WebSocketExtension(name));
		}
		return result;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}


	@Override
	public void start(String path) {
		if (!isRunning()) {
			this.running = true;
			try {
                initJettyRequestUpgradeStrategy(path);
//				if (webSocketFactoryInitMethod != null) {
//					webSocketFactoryInitMethod.invoke(this.factory);
//				}
//				else {
//					this.factory.init(this.servletContext);
//				}
//                this.factory.();
			}
			catch (Exception ex) {
				throw new IllegalStateException("Unable to initialize Jetty WebSocketServerFactory", ex);
			}
		}
	}

	@Override
	public void stop() {
		if (isRunning()) {
			this.running = false;
            try {
                this.factory.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
	}

	@Override
	public void upgrade(ServerHttpRequest request, ServerHttpResponse response,
			String selectedProtocol, List<WebSocketExtension> selectedExtensions, Principal user,
			WebSocketHandler wsHandler, Map<String, Object> attributes) throws HandshakeFailureException {

		Assert.isInstanceOf(ServletServerHttpRequest.class, request);
		HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

		Assert.isInstanceOf(ServletServerHttpResponse.class, response);
		HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();

		Assert.isTrue(this.factory.isUpgradeRequest(servletRequest, servletResponse), "Not a WebSocket handshake");

        Jetty10WebSocketSession session = new Jetty10WebSocketSession(attributes, user);
        Jetty10WebSocketHandlerAdapter handlerAdapter = new Jetty10WebSocketHandlerAdapter(wsHandler, session);

		WebSocketHandlerContainer container =
				new WebSocketHandlerContainer(handlerAdapter, selectedProtocol, selectedExtensions);

		try {
			wsContainerHolder.set(container);
			this.factory.acceptWebSocket(servletRequest, servletResponse);
		}
		catch (IOException ex) {
			throw new HandshakeFailureException(
					"Response update failed during upgrade to WebSocket: " + request.getURI(), ex);
		}
		finally {
			wsContainerHolder.remove();
		}
	}


	static class WebSocketHandlerContainer {

		private final Jetty10WebSocketHandlerAdapter handler;

		private final String selectedProtocol;

		private final List<ExtensionConfig> extensionConfigs;

		public WebSocketHandlerContainer(Jetty10WebSocketHandlerAdapter handler, String protocol, List<WebSocketExtension> extensions) {
			this.handler = handler;
			this.selectedProtocol = protocol;
			if (CollectionUtils.isEmpty(extensions)) {
				this.extensionConfigs = null;
			}
			else {
				this.extensionConfigs = new ArrayList<ExtensionConfig>();
				for (WebSocketExtension e : extensions) {
					this.extensionConfigs.add(new WebSocketToJetty10ExtensionConfigAdapter(e));
				}
			}
		}

		public Jetty10WebSocketHandlerAdapter getHandler() {
			return this.handler;
		}

		public String getSelectedProtocol() {
			return this.selectedProtocol;
		}

		public List<ExtensionConfig> getExtensionConfigs() {
			return this.extensionConfigs;
		}
	}

}
