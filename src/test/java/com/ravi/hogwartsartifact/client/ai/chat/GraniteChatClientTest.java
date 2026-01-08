package com.ravi.hogwartsartifact.client.ai.chat;

import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatRequest;
import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatResponses;
import com.ravi.hogwartsartifact.client.ai.chat.dto.Choice;
import com.ravi.hogwartsartifact.client.ai.chat.dto.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


@SpringBootTest
class GraniteChatClientTest {

    @Autowired
    private GraniteChatClient graniteChatClient;
    
    @Autowired
    private MockRestServiceServer mockRestServiceServer;
    
    @Autowired
    private ObjectMapper objectMapper;

    private String url;

    @BeforeEach
    void setUp() {
        this.url = "http://localhost:1234/v1/chat/completions";
        // Reset the mock server before each test to ensure clean state
        if (mockRestServiceServer != null) {
            mockRestServiceServer.reset();
        }
    }

    @AfterEach
    void tearDown() {
        if (mockRestServiceServer != null) {
            mockRestServiceServer.verify();
            mockRestServiceServer.reset();
        }
    }

    @Test
    void testGenerateSuccess() throws Exception{
        //given
        ChatRequest chatRequest =  new ChatRequest("ibm/granite-4-h-tiny", List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", "A json array.")
        ));
        ChatResponses chatResponse = new ChatResponses(List.of(
                new Choice(0, new Message("assistant", "The summary includes six artifacts, owned by three different wizards."))));
        this.mockRestServiceServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(this.objectMapper.writeValueAsString(chatRequest)))
                .andRespond(withSuccess(this.objectMapper.writeValueAsString(chatResponse),MediaType.APPLICATION_JSON));
        //when
        ChatResponses generatedChatResponse = this.graniteChatClient.generate(chatRequest);
        //then
        this.mockRestServiceServer.verify();
        assertThat(generatedChatResponse.choices().get(0).message().content())
                .isEqualTo("The summary includes six artifacts, owned by three different wizards.");
    }


//    @Test
//    void testGenerateUnauthorizedRequest(){
//        // Given:
//        ChatRequest chatRequest = new ChatRequest("ibm/granite-4-h-tiny", List.of(
//                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
//                new Message("user", "A json array.")
//        ));
//        this.mockRestServiceServer.expect(requestTo(this.url))
//                .andExpect(method(HttpMethod.POST))
//                .andExpect(content().json(this.objectMapper.writeValueAsString(chatRequest)))
//                .andRespond(withUnauthorized());
//
//        // When:
//        Throwable thrown = catchThrowable(() -> {
//            ChatResponses generatedChatResponse = this.graniteChatClient.generate(chatRequest);
//        });
//
//        // Then:
//        this.mockRestServiceServer.verify();
//        assertThat(thrown)
//                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
//    }

    /**
     * This test simulates a scenario where the service receives a 429 Quota Exceeded response.
     */
    @Test
    void testGenerateQuotaExceeded() {
        // Given
        ChatRequest chatRequest = new ChatRequest("ibm/granite-4-h-tiny", List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", "A json array.")
        ));
        this.mockRestServiceServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(this.objectMapper.writeValueAsString(chatRequest)))
                .andRespond(withTooManyRequests());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponses chatResponse = this.graniteChatClient.generate(chatRequest);
        });

        // Then
        this.mockRestServiceServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown)
                .isInstanceOf(HttpClientErrorException.TooManyRequests.class);
    }

    /**
     * This test simulates receiving a 500 Internal Server Error response.
     */
    @Test
    void testGenerateServerError() {
        // Given
        ChatRequest chatRequest = new ChatRequest("ibm/granite-4-h-tiny", List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", "A json array.")
        ));
        this.mockRestServiceServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(this.objectMapper.writeValueAsString(chatRequest)))
                .andRespond(withServerError());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponses chatResponse = this.graniteChatClient.generate(chatRequest);
        });

        // Then
        this.mockRestServiceServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown)
                .isInstanceOf(HttpServerErrorException.InternalServerError.class);
    }

    /**
     * This test simulates receiving a 503 Service Unavailable response.
     */
    @Test
    void testGenerateServerOverloaded() {
        // Given
        ChatRequest chatRequest = new ChatRequest("ibm/granite-4-h-tiny", List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", "A json array.")
        ));
        this.mockRestServiceServer.expect(requestTo(this.url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(this.objectMapper.writeValueAsString(chatRequest)))
                .andRespond(withServiceUnavailable());

        // When
        Throwable thrown = catchThrowable(() -> {
            ChatResponses chatResponse = this.graniteChatClient.generate(chatRequest);
        });

        // Then
        this.mockRestServiceServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
        assertThat(thrown)
                .isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
    }
    
    @TestConfiguration
    static class TestConfig {
        
        private static RestClient.Builder sharedBuilder;
        private static MockRestServiceServer mockServer;
        
        @Bean
        @Primary
        public RestClient.Builder testRestClientBuilder() {
            if (sharedBuilder == null) {
                sharedBuilder = RestClient.builder()
                        .requestFactory(new JdkClientHttpRequestFactory());
                // Bind mock server to the builder immediately
                mockServer = MockRestServiceServer.bindTo(sharedBuilder).build();
            }
            return sharedBuilder;
        }
        
        @Bean
        public MockRestServiceServer mockRestServiceServer() {
            // Ensure builder is created first
            testRestClientBuilder();
            return mockServer;
        }
    }
}