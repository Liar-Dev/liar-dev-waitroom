package liar.waitservice.wait.controller.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CustomWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    public CustomWebSocketHandlerDecorator(@Qualifier("customWebSocketHandlerDecorator") WebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionMap.put(session.getId(), session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessionMap.remove(session.getId());
        super.afterConnectionClosed(session, closeStatus);
    }

    public void closeSession(String sessionId) throws IOException {
        WebSocketSession session = sessionMap.get(sessionId);
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
