package server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class GetGreetingResponseMessage {

    public long requestMessageId;

    public String message;
}
