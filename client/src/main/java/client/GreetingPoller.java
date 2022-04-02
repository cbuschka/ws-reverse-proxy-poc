package client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class GreetingPoller {

    @Autowired
    private GetGreetingClient greetingClient;

    private Random random = new Random();

    @Scheduled(fixedDelay = 1000, initialDelay = 3000)
    public void poll() {

        try {
            GetGreetingResponse response = greetingClient.getGreeting("Bot" + random.nextInt());
            log.info("Got response: {}.", response.message);
        } catch (Exception ex) {
            log.error("Getting greeting failed.", ex);
        }
    }
}
