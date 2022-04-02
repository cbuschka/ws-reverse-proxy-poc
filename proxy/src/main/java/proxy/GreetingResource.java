package proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingResource {

    @Autowired
    private GreetingClient greetingClient;

    @GetMapping(path = "/greetings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetGreetingResponse> greet(@RequestParam("name") String name) throws Exception {
        String message = greetingClient.getGreeting(name).get();
        return ResponseEntity.ok(new GetGreetingResponse(message));
    }
}
