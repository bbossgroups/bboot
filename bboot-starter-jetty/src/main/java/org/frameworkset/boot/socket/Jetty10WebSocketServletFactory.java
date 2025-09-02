package org.frameworkset.boot.socket;
/**
 * Copyright 2025 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.eclipse.jetty.http.pathmap.PathSpec;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.core.Configuration;
import org.eclipse.jetty.websocket.core.WebSocketComponents;
import org.eclipse.jetty.websocket.core.server.*;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.websocket.server.internal.DelegatedServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.internal.DelegatedServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.internal.JettyServerFrameHandlerFactory;
import org.frameworkset.util.Assert;
import org.frameworkset.web.socket.container.JettyRequestUpgradeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * @author biaoping.yin
 * @Date 2025/9/2
 */
public class Jetty10WebSocketServletFactory extends Configuration.ConfigurationCustomizer implements JettyWebSocketServletFactory
{

    private static Logger logger = LoggerFactory.getLogger(Jetty10WebSocketServletFactory.class);


    private ServletContext servletContext;



    private WebSocketMappings mapping;
    private WebSocketComponents components;
    /**
     * @return the instance of {@link FrameHandlerFactory} to be used to create the FrameHandler
     */
    private FrameHandlerFactory getFactory()
    {
        JettyServerFrameHandlerFactory frameHandlerFactory = JettyServerFrameHandlerFactory.getFactory(getServletContext());
        if (frameHandlerFactory == null)
            throw new IllegalStateException("JettyServerFrameHandlerFactory not found");

        return frameHandlerFactory;
    }

    public void setMapping(WebSocketMappings mapping) {
        this.mapping = mapping;
    }

    public void setComponents(WebSocketComponents components) {
        this.components = components;
    }
    @Override
    public Set<String> getAvailableExtensionNames()
    {
        return components.getExtensionRegistry().getAvailableExtensionNames();
    }
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void addMapping(String pathSpec)
    {
        mapping.addMapping(WebSocketMappings.parsePathSpec(pathSpec), new WrappedJettyCreator(null), getFactory(), this);
    }
    @Override
    public void addMapping(String pathSpec, JettyWebSocketCreator creator)
    {
        mapping.addMapping(WebSocketMappings.parsePathSpec(pathSpec), new WrappedJettyCreator(creator), getFactory(), this);
    }

    @Override
    public void register(Class<?> endpointClass)
    {
        Constructor<?> constructor;
        try
        {
            constructor = endpointClass.getDeclaredConstructor();
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }

        JettyWebSocketCreator creator = (req, resp) ->
        {
            try
            {
                return constructor.newInstance();
            }
            catch (Throwable t)
            {
                t.printStackTrace();
                return null;
            }
        };

        addMapping("/", creator);
    }

    @Override
    public void setCreator(JettyWebSocketCreator creator)
    {
        addMapping("/", creator);
    }

    @Override
    public JettyWebSocketCreator getMapping(String pathSpec)
    {
        WebSocketCreator creator = mapping.getWebSocketCreator(WebSocketMappings.parsePathSpec(pathSpec));
         
//        return (JettyWebSocketCreator)creator;
        if (creator instanceof WrappedJettyCreator)
            return ((WrappedJettyCreator)creator).getJettyWebSocketCreator();
        return null;
    }

    @Override
    public boolean removeMapping(String pathSpec)
    {
        return mapping.removeMapping(WebSocketMappings.parsePathSpec(pathSpec));
    }

    public PathSpec parsePathSpec(String s) {
        return mapping.parsePathSpec(s);
    }

    public void stop() {
    }

    public Iterator<String> splitAt(String str, String delims) {
        return new DeQuotingStringIterator(str.trim(), delims);
    }

  
    public boolean isUpgradeRequest(HttpServletRequest request, HttpServletResponse response)
    {
        // Tests sorted by least common to most common.

        String upgrade = request.getHeader("Upgrade");
        if (upgrade == null)
        {
            // no "Upgrade: websocket" header present.
            return false;
        }

        if (!"websocket".equalsIgnoreCase(upgrade))
        {
            // Not a websocket upgrade
            return false;
        }

        String connection = request.getHeader("Connection");
        if (connection == null)
        {
            // no "Connection: upgrade" header present.
            return false;
        }

        // Test for "Upgrade" token
        boolean foundUpgradeToken = false;
        Iterator<String> iter = splitAt(connection, ",");
        while (iter.hasNext())
        {
            String token = iter.next();
            if ("upgrade".equalsIgnoreCase(token))
            {
                foundUpgradeToken = true;
                break;
            }
        }

        if (!foundUpgradeToken)
        {
            return false;
        }

        if (!"GET".equalsIgnoreCase(request.getMethod()))
        {
            // not a "GET" request (not a websocket upgrade)
            return false;
        }

        if (!"HTTP/1.1".equals(request.getProtocol()))
        {
            if ("HTTP/2".equals(request.getProtocol()))
            {
                logger.warn("WebSocket Bootstrap from HTTP/2 (RFC8441) not supported in Jetty 9.x");
            }
            else
            {
                logger.warn("Not a 'HTTP/1.1' request (was [" + request.getProtocol() + "])");
            }
            return false;
        }

        return true;
    }
    public void acceptWebSocket(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
        this.mapping.upgrade(servletRequest, servletResponse, this);
    }

    private static class WrappedJettyCreator implements WebSocketCreator
    {
        private final JettyWebSocketCreator creator;

        private WrappedJettyCreator(JettyWebSocketCreator creator)
        {
            this.creator = creator;
        }

        private JettyWebSocketCreator getJettyWebSocketCreator()
        {
            return creator == null ? new JettyWebSocketCreator(){
                @Override
                public Object createWebSocket(JettyServerUpgradeRequest req, JettyServerUpgradeResponse resp)
                {
                    return null;
                }
            } : creator;
        }
 
        @Override
        public Object createWebSocket(ServerUpgradeRequest req, ServerUpgradeResponse response)
        {
            Jetty10RequestUpgradeStrategy.WebSocketHandlerContainer container = Jetty10RequestUpgradeStrategy.getWsContainerHolder().get();
            Assert.state(container != null, "Expected WebSocketHandlerContainer");
            response.setAcceptedSubProtocol(container.getSelectedProtocol());
            response.setExtensions(container.getExtensionConfigs());
            return container.getHandler();
//            return creator.createWebSocket(new DelegatedServerUpgradeRequest(req), new DelegatedServerUpgradeResponse(response));
        }
    }

}
