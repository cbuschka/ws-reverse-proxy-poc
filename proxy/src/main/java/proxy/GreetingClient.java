package proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class GreetingClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final AtomicLong seq = new AtomicLong(1);

    private final Map<Long, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    @Autowired
    private WebSocketSessionPool webSocketSessionPool;

    @SneakyThrows(IOException.class)
    public Future<String> getGreeting(String name) {
        Optional<WebSocketSession> optionalWebSocketSession = webSocketSessionPool.pick();
        if (optionalWebSocketSession.isEmpty()) {
            throw new IllegalStateException("No downstream connection available.");
        }
        WebSocketSession webSocketSession = optionalWebSocketSession.get();

        long requestMessageId = seq.getAndIncrement();
        PendingRequest pendingRequest = new PendingRequest();
        pendingRequests.put(requestMessageId, pendingRequest);

        log.info("Forwarded {}...", requestMessageId);

        String requestJson = objectMapper.writeValueAsString(new GetGreetingRequestMessage(requestMessageId, name));
        webSocketSession.sendMessage(new TextMessage(requestJson));

        return pendingRequest;
    }

    void handleIncomingMessage(String payload) throws Exception {
        GetGreetingResponseMessage response = objectMapper.readerFor(GetGreetingResponseMessage.class).readValue(payload);
        PendingRequest pendingRequest = pendingRequests.remove(response.requestMessageId);
        if (pendingRequest == null) {
            return;
        }

        log.info("Received response for {}.", response.requestMessageId);

        pendingRequest.complete(response.message);
    }

    private static class PendingRequest implements Future<String> {
        private final Object lock = new Object();
        private boolean done = false;
        private String result;
        private Throwable exception;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            synchronized (lock) {
                return this.done;
            }
        }

        @Override
        public String get() throws InterruptedException, ExecutionException {
            synchronized (lock) {
                while (!done) {
                    lock.wait(100);
                }

                if (exception != null) {
                    throw new ExecutionException("Failed.", exception);
                }

                return result;
            }
        }

        @Override
        public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            synchronized (lock) {
                long startNanos = System.nanoTime();
                while (!done) {
                    lock.wait(100);
                    long nowNanos = System.nanoTime();
                    if (nowNanos > startNanos + unit.toNanos(timeout)) {
                        throw new TimeoutException("Timeout.");
                    }
                }

                if (exception != null) {
                    throw new ExecutionException("Failed.", exception);
                }

                return result;
            }
        }

        void complete(String result) {
            synchronized (lock) {
                this.result = result;
                this.done = true;
                lock.notifyAll();
            }
        }


        void fail(Exception exception) {
            synchronized (lock) {
                this.exception = exception;
                this.done = true;
                lock.notifyAll();
            }
        }
    }
}
