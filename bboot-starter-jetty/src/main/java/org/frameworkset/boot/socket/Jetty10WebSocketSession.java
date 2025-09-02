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

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.core.ExtensionConfig;
import org.frameworkset.http.HttpHeaders;
import org.frameworkset.util.ObjectUtils;
import org.frameworkset.web.socket.endpoint.AbstractWebSocketSession;
import org.frameworkset.web.socket.handler.jetty.JettyWebSocketSession;
import org.frameworkset.web.socket.inf.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author biaoping.yin
 * @Date 2025/9/2
 */
public class Jetty10WebSocketSession extends AbstractWebSocketSession<Session> {

    private String id;

    private URI uri;

    private HttpHeaders headers;

    private String acceptedProtocol;

    private List<WebSocketExtension> extensions;

    private Principal user;


    /**
     * Create a new {@link JettyWebSocketSession} instance.
     *
     * @param attributes attributes from the HTTP handshake to associate with the WebSocket session
     */
    public Jetty10WebSocketSession(Map<String, Object> attributes) {
        this(attributes, null);
    }

    /**
     * Create a new {@link JettyWebSocketSession} instance associated with the given user.
     *
     * @param attributes attributes from the HTTP handshake to associate with the WebSocket
     * session; the provided attributes are copied, the original map is not used.
     * @param user the user associated with the session; if {@code null} we'll fallback on the user
     *  available via {@link org.eclipse.jetty.websocket.api.Session#getUpgradeRequest()}
     */
    public Jetty10WebSocketSession(Map<String, Object> attributes, Principal user) {
        super(attributes);
        this.user = user;
    }


    @Override
    public String getId() {
        checkNativeSessionInitialized();
        return this.id;
    }

    @Override
    public URI getUri() {
        checkNativeSessionInitialized();
        return this.uri;
    }

    @Override
    public HttpHeaders getHandshakeHeaders() {
        checkNativeSessionInitialized();
        return this.headers;
    }

    @Override
    public String getAcceptedProtocol() {
        checkNativeSessionInitialized();
        return this.acceptedProtocol;
    }

    @Override
    public List<WebSocketExtension> getExtensions() {
        checkNativeSessionInitialized();
        return this.extensions;
    }

    @Override
    public Principal getPrincipal() {
        return this.user;
    }

    @Override
    public SocketAddress getLocalAddress() {
        checkNativeSessionInitialized();
        return getNativeSession().getLocalAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        checkNativeSessionInitialized();
        return getNativeSession().getRemoteAddress();
    }

    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit) {
        checkNativeSessionInitialized();
        getNativeSession().getPolicy().setMaxTextMessageSize(messageSizeLimit);
    }

    @Override
    public int getTextMessageSizeLimit() {
        checkNativeSessionInitialized();
        return (int)getNativeSession().getPolicy().getMaxTextMessageSize();
    }

    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit) {
        checkNativeSessionInitialized();
        getNativeSession().getPolicy().setMaxBinaryMessageSize(messageSizeLimit);
    }

    @Override
    public int getBinaryMessageSizeLimit() {
        checkNativeSessionInitialized();
        return (int)getNativeSession().getPolicy().getMaxBinaryMessageSize();
    }

    @Override
    public boolean isOpen() {
        return ((getNativeSession() != null) && getNativeSession().isOpen());
    }

    @Override
    public void initializeNativeSession(Session session) {
        super.initializeNativeSession(session);

        this.id = ObjectUtils.getIdentityHexString(getNativeSession());
        this.uri = session.getUpgradeRequest().getRequestURI();

        this.headers = new HttpHeaders();
        this.headers.putAll(getNativeSession().getUpgradeRequest().getHeaders());
        this.headers = HttpHeaders.readOnlyHttpHeaders(headers);

        this.acceptedProtocol = session.getUpgradeResponse().getAcceptedSubProtocol();

        List<org.eclipse.jetty.websocket.api.ExtensionConfig> source = getNativeSession().getUpgradeResponse().getExtensions();
        this.extensions = new ArrayList<WebSocketExtension>(source.size());
        for (org.eclipse.jetty.websocket.api.ExtensionConfig ec : source) {
            this.extensions.add(new WebSocketExtension(ec.getName(), ec.getParameters()));
        }

        if (this.user == null) {
            this.user = session.getUpgradeRequest().getUserPrincipal();
        }
    }

    @Override
    protected void sendTextMessage(TextMessage message) throws IOException {
        getNativeSession().getRemote().sendString(message.getPayload());
    }

    @Override
    protected void sendBinaryMessage(BinaryMessage message) throws IOException {
        getNativeSession().getRemote().sendBytes(message.getPayload());
    }

    @Override
    protected void sendPingMessage(PingMessage message) throws IOException {
        getNativeSession().getRemote().sendPing(message.getPayload());
    }

    @Override
    protected void sendPongMessage(PongMessage message) throws IOException {
        getNativeSession().getRemote().sendPong(message.getPayload());
    }

    @Override
    protected void closeInternal(CloseStatus status) throws IOException {
        getNativeSession().close(status.getCode(), status.getReason());
    }

}
