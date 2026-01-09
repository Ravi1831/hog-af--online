package com.ravi.hogwartsartifact.artifact;

import com.ravi.hogwartsartifact.artifact.dto.ArtifactDto;
import com.ravi.hogwartsartifact.artifact.utils.IdWorker;
import com.ravi.hogwartsartifact.client.ai.chat.ChatClient;
import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatRequest;
import com.ravi.hogwartsartifact.client.ai.chat.dto.ChatResponses;
import com.ravi.hogwartsartifact.client.ai.chat.dto.Choice;
import com.ravi.hogwartsartifact.client.ai.chat.dto.Message;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import com.ravi.hogwartsartifact.wizard.Wizard;
import com.ravi.hogwartsartifact.wizard.dto.WizardDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ArtifactServiceTest {

    @Mock
    ArtifactRepository artifactRepository;

    @Mock
    IdWorker idWorker;

    @Mock
    ChatClient chatClient;

    @InjectMocks
    ArtifactService artifactService;

    List<Artifact> artifacts;

    @BeforeEach
    void setUp() {
        Artifact a1 = new Artifact();
        a1.setId("1250808601744904191");
        a1.setName("Deluminator");
        a1.setDescription("A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter. It is used to remove or absorb (as well as return) the light from any light source to provide cover to the user.");
        a1.setImageUrl("imageUrl");

        Artifact a2 = new Artifact();
        a2.setId("1250808601744904192");
        a2.setName("Invisibility Cloak");
        a2.setDescription("An invisibility cloak is used to make the wearer invisible.");
        a2.setImageUrl("imageUrl");

        this.artifacts = new ArrayList<>();
        this.artifacts.add(a1);
        this.artifacts.add(a2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindByIdSuccess() {
        //Given //Arrange input and target. Define the behaviour of the Mock Object artifactRepo
        /*
        "id": "1250808601744904192",
        "name": "Invisibility Cloak",
        "description": "An invisibility cloak is used to make the wearer invisible.",
        "imageUrl": "ImageUrl",
         */

        Artifact artifact = new Artifact();
        artifact.setId("1250808601744904192");
        artifact.setName("Invisibility Cloak");
        artifact.setDescription("An invisibility cloak is used to make the wearer invisible.");
        artifact.setImageUrl("ImageUrl");

        Wizard wizard = new Wizard();
        wizard.setId(2);
        wizard.setName("Harry Potter");

        artifact.setOwner(wizard);

        given(artifactRepository.findById("1250808601744904192"))
                .willReturn(Optional.of(artifact));
        //when //Act on the target behaviour. when steps should cover the method to be tested.
        Artifact returnedArtifact = artifactService.findById("1250808601744904192");
        //then //Assert expected outcome
        assertThat(returnedArtifact.getId()).isEqualTo("1250808601744904192");
        assertThat(returnedArtifact.getName()).isEqualTo("Invisibility Cloak");
        assertThat(returnedArtifact.getDescription()).isEqualTo("An invisibility cloak is used to make the wearer invisible.");
        assertThat(returnedArtifact.getImageUrl()).isEqualTo("ImageUrl");
        verify(artifactRepository, times(1)).findById("1250808601744904192");
    }

    @Test
    void testFindByIdNotFound() {
        //Given
        given(artifactRepository.findById(Mockito.any(String.class))).willReturn(Optional.empty());
        //When
        Throwable thrown = catchThrowable(() -> {
            artifactService.findById("1250808601744904192");
        });
        //then
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find artifact with id 1250808601744904192 :(");
        verify(artifactRepository, times(1)).findById("1250808601744904192");
    }

    @Test
    void testFindAllSuccess() {
        //given
        given(this.artifactRepository.findAll()).willReturn(this.artifacts);

        //when
        List<Artifact> actualArtifacts = this.artifactService.findAll();

        //then
        assertThat(actualArtifacts.size()).isEqualTo(this.artifacts.size());
        verify(this.artifactRepository, times(1)).findAll();

    }

    @Test
    void testSaveSuccess() {
        //given
        Artifact newArtifact = new Artifact();
        newArtifact.setName("Artifact 3");
        newArtifact.setDescription("Description");
        newArtifact.setImageUrl("ImageUrl...");

        given(idWorker.nextId()).willReturn(123456L);
        given(artifactRepository.save(newArtifact)).willReturn(newArtifact);
        //when

        Artifact savedArtifacts = artifactService.save(newArtifact);
        //then
        assertThat(savedArtifacts.getId()).isEqualTo("123456");
        assertThat(savedArtifacts.getName()).isEqualTo(newArtifact.getName());
        assertThat(savedArtifacts.getDescription()).isEqualTo(newArtifact.getDescription());
        assertThat(savedArtifacts.getImageUrl()).isEqualTo(newArtifact.getImageUrl());
        verify(artifactRepository, times(1)).save(newArtifact);
    }

    @Test
    void testUpdateSuccess() {
        //given
        Artifact oldArtifact = new Artifact();
        oldArtifact.setId("1250808601744904192");
        oldArtifact.setName("Invisibility Cloak");
        oldArtifact.setDescription("An invisibility cloak is used to make the wearer invisible.");
        oldArtifact.setImageUrl("imageUrl");

        Artifact update = new Artifact();
        update.setId("1250808601744904192");
        update.setName("Invisibility Cloak");
        update.setDescription("A new description.");
        update.setImageUrl("imageUrl");

        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(oldArtifact));
        given(artifactRepository.save(oldArtifact)).willReturn(oldArtifact);
        //when

        Artifact updatedArtifacts = artifactService.update("1250808601744904192", update);

        //then
        assertThat(updatedArtifacts.getId()).isEqualTo(update.getId());
        assertThat(updatedArtifacts.getDescription()).isEqualTo(update.getDescription());
        verify(artifactRepository, times(1)).findById("1250808601744904192");
        verify(artifactRepository, times(1)).save(oldArtifact);
    }

    @Test
    void testUpdatedNotFound() {
        //Given
        Artifact update = new Artifact();
        update.setName("Invisibility Cloak");
        update.setDescription("A new description.");
        update.setImageUrl("imageUrl");

        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.empty());
        //When
        assertThrows(ObjectNotFoundException.class, () -> {
            artifactService.update("1250808601744904192", update);
        });
        //Then
        verify(artifactRepository, times(1)).findById("1250808601744904192");
    }


    @Test
    void testDeleteSuccess() {
        //given
        Artifact artifact = new Artifact();
        artifact.setId("1250808601744904192");
        artifact.setName("Invisibility Cloak");
        artifact.setDescription("An invisibility cloak is used to make the wearer invisible.");
        artifact.setImageUrl("imageUrl");

        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(artifact));
        doNothing().when(artifactRepository).deleteById("1250808601744904192");
        //when
        artifactService.delete("1250808601744904192");
        //then
        verify(artifactRepository, times(1)).deleteById("1250808601744904192");
    }


    @Test
    void testDeleteNotFound() {
        //given
        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.empty());
        //when
        assertThrows(ObjectNotFoundException.class, () -> {
            artifactService.delete("1250808601744904192");
        });
        //then
        verify(artifactRepository, times(1)).findById("1250808601744904192");
    }
    @Test
    void testSummarizeSuccess()  {
        // Given:
        WizardDto wizardDto = new WizardDto(1, "Albus Dombledore", 2);
        List<ArtifactDto> artifactDtos = List.of(
                new ArtifactDto("1250808601744904191", "Deluminator", "A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter. It is used to remove or absorb (as well as return) the light from any light source to provide cover to the user.", "ImageUrl", wizardDto),
                new ArtifactDto("1250808601744904193", "Elder Wand", "The Elder Wand, known throughout history as the Deathstick or the Wand of Destiny, is an extremely powerful wand made of elder wood with a core of Thestral tail hair.", "ImageUrl", wizardDto)
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonArray = objectMapper.writeValueAsString(artifactDtos);

        List<Message> messages = List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", jsonArray)
        );

        ChatRequest chatRequest = new ChatRequest("ibm/granite-4-h-tiny", messages);

        ChatResponses chatResponse = new ChatResponses(List.of(
                new Choice(0, new Message("assistant", "A summary of two artifacts owned by Albus Dumbledore."))));

        given(this.chatClient.generate(chatRequest)).willReturn(chatResponse);

        // When:
        String summary = this.artifactService.summarize(artifactDtos);

        // Then:
        assertThat(summary).isEqualTo("A summary of two artifacts owned by Albus Dumbledore.");
        verify(this.chatClient, times(1)).generate(chatRequest);
    }

    @Test
    void testFindByCriteriaWithId(){
        //given
        Map<String,String> searchCriteria = Map.of("id", "1250808601744904191");
        Pageable pageable =  PageRequest.of(0,20);
        PageImpl<Artifact> expectedPage = new PageImpl<>(List.of(artifacts.get(0)),pageable,1);
        given(artifactRepository.findAll(Mockito.any(Specification.class),eq(pageable)))
                .willReturn(expectedPage);
        //when
        Page<Artifact> result = artifactService.findByCriteria(searchCriteria, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("1250808601744904191");
        verify(artifactRepository,times(1)).findAll(Mockito.any(Specification.class),eq(pageable));
    }

    @Test
    void testFindByCriteriaWithName(){
        //given
        Map<String,String> searchCriteria = Map.of("name","cloak");
        Pageable pageable = PageRequest.of(0,20);
        PageImpl<Artifact> expectedPage =new PageImpl<>(List.of(artifacts.get(1)),pageable,1);
        given(artifactRepository.findAll(Mockito.any(Specification.class),eq(pageable)))
                .willReturn(expectedPage);
        //when
        Page<Artifact> result = artifactService.findByCriteria(searchCriteria,pageable);

        //then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).containsIgnoringCase("cloak");
        verify(artifactRepository,times(1)).findAll(Mockito.any(Specification.class),eq(pageable));
    }

}