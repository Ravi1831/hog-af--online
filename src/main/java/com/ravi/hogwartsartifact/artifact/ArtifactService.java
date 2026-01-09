package com.ravi.hogwartsartifact.artifact;

import com.ravi.hogwartsartifact.artifact.dto.ArtifactDto;
import com.ravi.hogwartsartifact.artifact.utils.IdWorker;
import com.ravi.hogwartsartifact.client.ai.chat.ChatClient;
import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatRequest;
import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatResponses;
import com.ravi.hogwartsartifact.client.ai.chat.dto.Message;
import com.ravi.hogwartsartifact.system.ExceptionConstants;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@Transactional
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final IdWorker idWorker;
    private final ChatClient chatClient;
    private static final Logger logger = LoggerFactory.getLogger(ArtifactService.class);

    public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker, ChatClient chatClient) {
        this.artifactRepository = artifactRepository;
        this.idWorker = idWorker;
        this.chatClient = chatClient;
    }


    public Artifact findById(String artifactId) {
        return this.artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.ARTIFACT,artifactId));
    }

    @Timed("findAllArtifactService.time")
    public List<Artifact> findAll() {
        return this.artifactRepository.findAll();
    }

    public Artifact save(Artifact newArtifact) {
        newArtifact.setId(String.valueOf(idWorker.nextId()));
        return this.artifactRepository.save(newArtifact);
    }

    public Artifact update(String artifactId, Artifact update) {
        return this.artifactRepository.findById(artifactId)
                .map(oldArtifacts -> {
                    oldArtifacts.setName(update.getName());
                    oldArtifacts.setDescription(update.getDescription());
                    oldArtifacts.setImageUrl(update.getImageUrl());
                    return this.artifactRepository.save(oldArtifacts);
                })
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.ARTIFACT,artifactId));
    }

    public void delete(String artifactId){
        this.artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException(ExceptionConstants.ARTIFACT,artifactId));
        this.artifactRepository.deleteById(artifactId);
    }

    public String summarize(List<ArtifactDto> artifactDtos) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonArray = objectMapper.writeValueAsString(artifactDtos);
        List<Message> messages = List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", jsonArray)
        );
        ChatRequest chatRequest = new ChatRequest(
                "ibm/granite-4-h-tiny",
                messages
        );
        ChatResponses chatResponse = this.chatClient.generate(chatRequest);
        return chatResponse.choices()
                .get(0)
                .message()
                .content();
    }

    public Page<Artifact> findAll(Pageable pageable) {
        return this.artifactRepository.findAll(pageable);
    }
}
