package server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GreetingService {

    public GetGreetingResponseMessage getGreeting(GetGreetingRequestMessage request) {
        log.info("Responding to request {}...", request.messageId);
        return new GetGreetingResponseMessage(request.messageId,
                String.format("Hello, %s", request.name));
    }
}
