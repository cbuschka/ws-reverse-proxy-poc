package proxy;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GetGreetingResponseMessage {

    public long requestMessageId;

    public String message;
}
