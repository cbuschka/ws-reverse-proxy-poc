package proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class WebSocketSessionPool {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public void put(WebSocketSession session) {
        sessions.add(session);

        log.info("Session added, {} open.", sessions.size());
    }

    public void remove(WebSocketSession session) {
        sessions.remove(session);

        log.info("Session remove, {} open.", sessions.size());
    }

    Optional<WebSocketSession> pick() {
        return sessions.stream().findFirst();
    }
}
