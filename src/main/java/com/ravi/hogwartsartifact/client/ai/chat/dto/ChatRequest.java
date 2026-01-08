package com.ravi.hogwartsartifact.client.ai.chat.dto;

import java.util.List;

public record ChatRequest(String model,
                          List<Message> messages) {
}
