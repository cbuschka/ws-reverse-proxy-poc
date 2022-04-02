package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class GetGreetingClient {

    @Value("${proxy.baseUrl:http://localhost:8080}")
    private String proxyBaseUrl;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public GetGreetingResponse getGreeting(String name) throws IOException, InterruptedException {
        URI uri = URI.create(String.format("%s/greetings?name=%s", proxyBaseUrl, name));
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        HttpResponse<GetGreetingResponse> response = httpClient.send(request, new HttpResponse.BodyHandler<GetGreetingResponse>() {
            @Override
            public HttpResponse.BodySubscriber<GetGreetingResponse> apply(HttpResponse.ResponseInfo responseInfo) {
                return HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), GetGreetingClient.this::apply);
            }
        });
        return response.body();
    }

    @SneakyThrows
    private GetGreetingResponse apply(String json) {
        return objectMapper.readerFor(GetGreetingResponse.class).readValue(json);
    }
}