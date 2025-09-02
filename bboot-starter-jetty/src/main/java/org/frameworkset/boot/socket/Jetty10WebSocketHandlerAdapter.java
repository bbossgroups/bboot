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

import org.eclipse.jetty.websocket.api.Frame;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.core.OpCode;
import org.frameworkset.util.Assert;
import org.frameworkset.web.socket.handler.ExceptionWebSocketHandlerDecorator;
import org.frameworkset.web.socket.handler.jetty.JettyWebSocketHandlerAdapter;
import org.frameworkset.web.socket.handler.jetty.JettyWebSocketSession;
import org.frameworkset.web.socket.inf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author biaoping.yin
 * @Date 2025/9/2
 */
@WebSocket
public class Jetty10WebSocketHandlerAdapter {

    private static final ByteBuffer EMPTY_PAYLOAD = ByteBuffer.wrap(new byte[0]);

    private static final Logger logger = LoggerFactory.getLogger(JettyWebSocketHandlerAdapter.class);


    private final WebSocketHandler webSocketHandler;

    private final Jetty10WebSocketSession wsSession;


    public Jetty10WebSocketHandlerAdapter(WebSocketHandler webSocketHandler, Jetty10WebSocketSession wsSession) {
        Assert.notNull(webSocketHandler, "WebSocketHandler must not be null");
        Assert.notNull(wsSession, "WebSocketSession must not be null");
        this.webSocketHandler = webSocketHandler;
        this.wsSession = wsSession;
    }


    @OnWebSocketConnect
    public void onWebSocketConnect(Session session) {
        try {
            this.wsSession.initializeNativeSession(session);
            this.webSocketHandler.afterConnectionEstablished(this.wsSession);
        }
        catch (Throwable ex) {
            ExceptionWebSocketHandlerDecorator.tryCloseWithError(this.wsSession, ex, logger);
        }
    }

    @OnWebSocketMessage
    public void onWebSocketText(String payload) {
        TextMessage message = new TextMessage(payload);
        try {
            this.webSocketHandler.handleMessage(this.wsSession, message);
        }
        catch (Throwable ex) {
            ExceptionWebSocketHandlerDecorator.tryCloseWithError(this.wsSession, ex, logger);
        }
    }

    @OnWebSocketMessage
    public void onWebSocketBinary(byte[] payload, int offset, int length) {
        BinaryMessage message = new BinaryMessage(payload, offset, length, true);
        try {
            this.webSocketHandler.handleMessage(this.wsSession, message);
        }
        catch (Throwable ex) {
            ExceptionWebSocketHandlerDecorator.tryCloseWithError(this.wsSession, ex, logger);
        }
    }

    @OnWebSocketFrame
    public void onWebSocketFrame(Frame frame) {
        if (OpCode.PONG == frame.getOpCode()) {
            ByteBuffer payload = frame.getPayload() != null ? frame.getPayload() : EMPTY_PAYLOAD;
            PongMessage message = new PongMessage(payload);
            try {
                this.webSocketHandler.handleMessage(this.wsSession, message);
            }
            catch (Throwable ex) {
                ExceptionWebSocketHandlerDecorator.tryCloseWithError(this.wsSession, ex, logger);
            }
        }
    }

    @OnWebSocketClose
    public void onWebSocketClose(int statusCode, String reason) {
        CloseStatus closeStatus = new CloseStatus(statusCode, reason);
        try {
            this.webSocketHandler.afterConnectionClosed(this.wsSession, closeStatus);
        }
        catch (Throwable ex) {
            if (logger.isErrorEnabled()) {
                logger.error("Unhandled error for " + this.wsSession, ex);
            }
        }
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause) {
        try {
            this.webSocketHandler.handleTransportError(this.wsSession, cause);
        }
        catch (Throwable ex) {
            ExceptionWebSocketHandlerDecorator.tryCloseWithError(this.wsSession, ex, logger);
        }
    }
}
