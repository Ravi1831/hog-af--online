package com.ravi.hogwartsartifact.wizard;

import com.ravi.hogwartsartifact.artifact.Artifact;
import com.ravi.hogwartsartifact.artifact.ArtifactRepository;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class WizardServiceTest {

    @Mock
    WizardRepository wizardRepository;

    @Mock
    ArtifactRepository artifactRepository;


    @InjectMocks
    WizardService wizardService;

    List<Wizard> wizards;

    @BeforeEach
    void setUp() {
        Wizard wizard1 = new Wizard();
        wizard1.setId(1);
        wizard1.setName("Albus Dumbledore");
        Wizard wizard2 = new Wizard();
        wizard2.setId(2);
        wizard2.setName("Harry Potter");
        Wizard wizard3 = new Wizard();
        wizard3.setId(3);
        wizard3.setName("Neville Longbottom");

        this.wizards = new ArrayList<>();
        this.wizards.add(wizard1);
        this.wizards.add(wizard2);
        this.wizards.add(wizard3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findAllSuccess() {
        //given
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<Wizard> wizardPage = new PageImpl<>(this.wizards, pageable, this.wizards.size());
        given(this.wizardRepository.findAll(pageable)).willReturn(wizardPage);
        //when
        Page<Wizard> actualWizard = this.wizardService.findAll(pageable);
        //then
        assertThat(actualWizard.getContent()).isEqualTo(wizards);
        assertThat(actualWizard.getTotalElements()).isEqualTo(wizards.size());
        verify(this.wizardRepository, times(1)).findAll(pageable);
    }

    @Test
    void findAllEmptyList() {
        //given
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<Wizard> emptyWizardPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
        given(this.wizardRepository.findAll(pageable)).willReturn(emptyWizardPage);
        //when
        Page<Wizard> actualWizard = this.wizardService.findAll(pageable);
        //then
        assertThat(actualWizard.getContent()).isEmpty();
        assertThat(actualWizard.getTotalElements()).isEqualTo(0);
        verify(this.wizardRepository, times(1)).findAll(pageable);
    }

    @Test
    void testSaveSuccess() {
        //given
        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Albus Dumbledore");
        given(wizardRepository.save(wizard)).willReturn(wizard);
        //when
        Wizard savedWizard = wizardService.save(wizard);
        //then
        assertThat(savedWizard.getId()).isEqualTo(1);
        assertThat(savedWizard.getName()).isEqualTo("Albus Dumbledore");
        verify(wizardRepository, times(1)).save(wizard);
    }

    @Test
    void testFindByIdSuccess() {
        //given
        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Albus Dumbledore");
        given(this.wizardRepository.findById(1)).willReturn(Optional.of(wizard));
        //when
        Wizard returnedWizard = wizardService.findById(1);
        //then
        assertThat(returnedWizard.getId()).isEqualTo(1);
        assertThat(returnedWizard.getName()).isEqualTo("Albus Dumbledore");
        verify(wizardRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {
        //given
        given(wizardRepository.findById(any(Integer.class))).willReturn(Optional.empty());
        //when
        Throwable thrown = catchThrowable(() -> {
            wizardService.findById(999);
        });
        //then
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find wizard with id 999 :(");
        verify(wizardRepository, times(1)).findById(999);
    }


    @Test
    void testUpdateWizardSuccess() {
        //given
        Wizard oldWizard = new Wizard();
        oldWizard.setId(1);
        oldWizard.setName("Albus Dumbledore");
        Wizard update = new Wizard();
        update.setId(1);
        update.setName("Albus Percival Wulfric Brian Dumbledore");
        given(this.wizardRepository.findById(1)).willReturn(Optional.of(oldWizard));
        given(this.wizardRepository.save(update)).willReturn(update);
        //when
        Wizard updatedWizard = this.wizardService.updateWizard(update, 1);
        //then
        assertThat(updatedWizard.getId()).isEqualTo(1);
        assertThat(updatedWizard.getName()).isEqualTo("Albus Percival Wulfric Brian Dumbledore");
        verify(this.wizardRepository, times(1)).findById(1);
        verify(this.wizardRepository, times(1)).save(update);
    }

    @Test
    void testUpdateWizardNotFound() {
        //given
        Wizard update = new Wizard();
        update.setName("Updated Name");
        given(this.wizardRepository.findById(999)).willReturn(Optional.empty());
        //when
        Throwable throwable = catchThrowable(() -> {
            this.wizardService.updateWizard(update, 999);
        });
        //assert
        assertThat(throwable).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find wizard with id 999 :(");
        verify(this.wizardRepository, times(1)).findById(999);
        verify(this.wizardRepository, never()).save(any(Wizard.class));
    }


    @Test
    void testDeleteWizardSuccess() {
        //given
        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Albus Dumbledore");
        given(this.wizardRepository.findById(1)).willReturn(Optional.of(wizard));
        doNothing().when(this.wizardRepository).deleteById(1);
        //when
        this.wizardService.deleteWizard(1);
        //then
        verify(this.wizardRepository, times(1)).findById(1);
        verify(this.wizardRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteWizardNotFound() {
        //given
        given(this.wizardRepository.findById(999)).willReturn(Optional.empty());
        //when
        Throwable thrown = catchThrowable(() -> {
            this.wizardService.deleteWizard(999);
        });
        //then
        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find wizard with id 999 :(");
        verify(this.wizardRepository, times(1)).findById(999);
        verify(this.wizardRepository, never()).deleteById(any(Integer.class));
    }

    @Test
    void testAssignArtifactSuccess() {
        //given
        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Albus Dumbledore");

        Artifact artifact = new Artifact();
        artifact.setId("1250808601744904192");
        artifact.setName("Invisibility Cloak");
        artifact.setDescription("An invisibility cloak is used to make the wearer invisible.");
        artifact.setImageUrl("imageUrl");

        given(this.wizardRepository.findById(1)).willReturn(Optional.of(wizard));
        given(this.artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(artifact));
        given(this.wizardRepository.save(wizard)).willReturn(wizard);

        //when
        this.wizardService.assignArtifact(1, "1250808601744904192");
        //then
        verify(this.wizardRepository, times(1)).findById(1);
        verify(this.artifactRepository, times(1)).findById("1250808601744904192");
        verify(this.wizardRepository, times(1)).save(wizard);
        assertThat(wizard.getArtifacts()).contains(artifact);
        assertThat(artifact.getOwner()).isEqualTo(wizard);
    }

    @Test
    void testAssignArtifactWizardNotFound() {
        //given
        Artifact artifact = new Artifact();
        artifact.setId("1250808601744904192");

        given(this.artifactRepository.findById("1250808601744904192"))
                .willReturn(Optional.of(artifact));
        //when
        Throwable throwable = catchThrowable(() -> {
            this.wizardService.assignArtifact(999, "1250808601744904192");
        });
        //then
        assertThat(throwable).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find wizard with id 999 :(");
        verify(this.wizardRepository, times(1)).findById(999);
        verify(this.artifactRepository, times(1)).findById("1250808601744904192");
        verify(this.wizardRepository, never()).save(any(Wizard.class));
    }


}