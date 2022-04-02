package proxy;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetGreetingRequestMessage {

    public final long messageId;

    public final String name;
}
