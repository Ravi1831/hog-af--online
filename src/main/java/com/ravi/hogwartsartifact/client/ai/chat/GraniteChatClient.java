package com.ravi.hogwartsartifact.client.ai.chat;

import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatRequest;
import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GraniteChatClient implements ChatClient{

    private final RestClient restClient;

    public GraniteChatClient(
            @Value("${ai.openai.endpoints}") String endpoint,
            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(endpoint)
                .build();
    }

    @Override
    public ChatResponses generate(ChatRequest chatRequest) {
        return this.restClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(chatRequest)
                .retrieve()
                .body(ChatResponses.class);
    }
}
