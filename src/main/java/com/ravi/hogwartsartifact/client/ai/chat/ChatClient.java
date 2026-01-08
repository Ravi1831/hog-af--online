package com.ravi.hogwartsartifact.client.ai.chat;

import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatRequest;
import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatResponses;

public interface ChatClient {

    ChatResponses generate(ChatRequest chatRequest);
}
