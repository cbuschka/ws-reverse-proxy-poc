package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class UpstreamConnector {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Set<WebSocketClient> webSocketClients = ConcurrentHashMap.newKeySet();

    @Value("${proxy.baseUrl:http://localhost:8080}")
    private String proxyBaseUrl;

    @Autowired
    private GreetingService greetingService;

    @Scheduled(fixedDelay = 1000)
    public void checkConnected() {

        if (webSocketClients.isEmpty()) {
            log.info("Opening upstream connection...");

            WebSocketClient webSocketClient = new StandardWebSocketClient();
            webSocketClient.doHandshake(new AbstractWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    UpstreamConnector.this.webSocketClients.add(webSocketClient);
                    log.info("Upstream connection established, {} open.", UpstreamConnector.this.webSocketClients.size());
                }

                @Override
                protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                    GetGreetingRequestMessage request = objectMapper.readerFor(GetGreetingRequestMessage.class).readValue(message.getPayload());
                    GetGreetingResponseMessage responseMessage = greetingService.getGreeting(request);
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMessage)));
                }

                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                    log.error("Transport error.", exception);
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                    UpstreamConnector.this.webSocketClients.remove(webSocketClient);
                    log.info("Upstream connection closed, {} open.", UpstreamConnector.this.webSocketClients.size());
                }
            }, String.format("%s/ws", this.proxyBaseUrl.replaceAll("^http://", "ws://")));
        }
    }
}
