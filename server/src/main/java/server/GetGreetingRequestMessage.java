package server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GetGreetingRequestMessage {

    public long messageId;

    public String name;
}
