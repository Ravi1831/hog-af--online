package com.ravi.hogwartsartifact.wizard;

import com.ravi.hogwartsartifact.artifact.ArtifactService;
import com.ravi.hogwartsartifact.system.constant.ExceptionConstants;
import com.ravi.hogwartsartifact.system.StatusCode;
import com.ravi.hogwartsartifact.system.exception.ObjectNotFoundException;
import com.ravi.hogwartsartifact.wizard.dto.WizardDto;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WizardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WizardService wizardService;

    @MockitoBean
    ArtifactService artifactService;

    @MockitoBean
    WizardRepository wizardRepository;

    @Autowired
    ObjectMapper objectMapper;

    List<Wizard> wizards;

    @Value("${api.endpoints.base-url}")
    String baseUrl;

    RequestPostProcessor jwtAuthentication;

    @BeforeEach
    void setUp() {
        // Set up JWT authentication once for all tests
        this.jwtAuthentication = jwt();

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

    @Test
    void testFindWizardByIdSuccess() throws Exception {
        //given
        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Albus Dumbledore");
        given(this.wizardService.findById(1)).willReturn(wizard);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl+"/wizards/1")
                        .with(this.jwtAuthentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Albus Dumbledore"))
                .andExpect(jsonPath("$.data.numberOfArtifacts").value(0));
    }

    @Test
    void testFindWizardByIdNotFound() throws Exception {
        //given
        given(this.wizardService.findById(999))
                .willThrow(new ObjectNotFoundException(ExceptionConstants.WIZARD, 999));
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl+"/wizards/999")
                        .with(this.jwtAuthentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard with id 999 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllWizardSuccess() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0,20);
        PageImpl<Wizard> wizardPage = new PageImpl<>(this.wizards,pageable,this.wizards.size());
        given(this.wizardService.findAll(Mockito.any(Pageable.class))).willReturn(wizardPage);
        MultiValueMap<String,String> paramRequest = new LinkedMultiValueMap<>();
        paramRequest.add("page","0");
        paramRequest.add("size","20");
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get(this.baseUrl+"/wizards")
                        .with(this.jwtAuthentication)
                        .accept(MediaType.APPLICATION_JSON)
                        .params(paramRequest))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(this.wizards.size())))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Albus Dumbledore"))
                .andExpect(jsonPath("$.data.content[0].numberOfArtifacts").value(0))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.content[1].name").value("Harry Potter"))
                .andExpect(jsonPath("$.data.content[2].id").value(3))
                .andExpect(jsonPath("$.data.content[2].name").value("Neville Longbottom"));
    }

    @Test
    void testWizardSuccess() throws Exception {
        //given
        WizardDto wizardDto = new WizardDto(null, "Hermione Granger", null);
        String json = this.objectMapper.writeValueAsString(wizardDto);
        Wizard savedWizard = new Wizard();
        savedWizard.setId(4);
        savedWizard.setName("Hermione Granger");
        given(this.wizardService.save(Mockito.any(Wizard.class))).willReturn(savedWizard);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.post(this.baseUrl+"/wizards")
                        .with(this.jwtAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").value(4))
                .andExpect(jsonPath("$.data.name").value("Hermione Granger"))
                .andExpect(jsonPath("$.data.numberOfArtifacts").value(0));
    }

    @Test
    void testAddWizardWithNameTooShort() throws Exception {
        //given
        WizardDto wizardDto = new WizardDto(null, "Ab", null);
        String json = this.objectMapper.writeValueAsString(wizardDto);
        //when then
        this.mockMvc.perform(MockMvcRequestBuilders.post(this.baseUrl+"/wizards")
                        .with(this.jwtAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Provided Argument are invalid, see data for details"))
                .andExpect(jsonPath("$.data.name").value("min required length is 3"));
    }

    @Test
    void testAddWizardWithEmptyName() throws Exception {
        //given
        WizardDto wizardDto = new WizardDto(null, "", null);
        String json = this.objectMapper.writeValueAsString(wizardDto);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.post(this.baseUrl+"/wizards")
                        .with(this.jwtAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Provided Argument are invalid, see data for details"))
                .andExpect(jsonPath("$.data.name").value("min required length is 3"));
    }

    @Test
    void testUpdateWizardSuccess() throws Exception {
        //given
        WizardDto wizardDto = new WizardDto(null, "Albus Percival Wulfric Brian Dumbledore", null);
        String json = this.objectMapper.writeValueAsString(wizardDto);
        Wizard updatedWizard = new Wizard();
        updatedWizard.setId(1);
        updatedWizard.setName("Albus Percival Wulfric Brian Dumbledore");
        given(this.wizardService.updateWizard(Mockito.any(Wizard.class), eq(1))).willReturn(updatedWizard);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl+"/wizards/1")
                        .with(this.jwtAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Albus Percival Wulfric Brian Dumbledore"))
                .andExpect(jsonPath("$.data.numberOfArtifacts").value(0));
    }

    @Test
    void testUpdateWizardNotFound() throws Exception {
        //given
        WizardDto wizardDto = new WizardDto(null, "Updated Name", null);
        String json = this.objectMapper.writeValueAsString(wizardDto);
        given(this.wizardService.updateWizard(Mockito.any(Wizard.class), eq(999)))
                .willThrow(new ObjectNotFoundException(ExceptionConstants.WIZARD,999));
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl+"/wizards/999")
                        .with(this.jwtAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard with id 999 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateWizardWithNameTooShort() throws Exception {
        //given
        WizardDto wizardDto = new WizardDto(null, "Ab", null);
        String json = this.objectMapper.writeValueAsString(wizardDto);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl+"/wizards/1")
                        .with(this.jwtAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Provided Argument are invalid, see data for details"))
                .andExpect(jsonPath("$.data.name").value("min required length is 3"));
    }

    @Test
    void testUpdateWizardWithEmptyName() throws Exception {
        //given
        WizardDto wizardDto = new WizardDto(null, "", null);
        String json = this.objectMapper.writeValueAsString(wizardDto);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl+"/wizards/1")
                        .with(this.jwtAuthentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Provided Argument are invalid, see data for details"))
                .andExpect(jsonPath("$.data.name").value("min required length is 3"));
    }

    @Test
    void testDeleteWizardSuccess() throws Exception {
        //given
        doNothing().when(this.wizardService).deleteWizard(1);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.delete(this.baseUrl+"/wizards/1")
                        .with(this.jwtAuthentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteWizardNotFound() throws Exception {
        //given
        doThrow(new ObjectNotFoundException(ExceptionConstants.WIZARD,999))
                .when(this.wizardService).deleteWizard(999);
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.delete(this.baseUrl+"/wizards/999")
                        .with(this.jwtAuthentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard with id 999 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAssignArtifactToWizardSuccess() throws Exception {
        //given
        doNothing().when(this.wizardService).assignArtifact(1, "1250808601744904192");
        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl+"/wizards/1/artifacts/1250808601744904192")
                        .with(this.jwtAuthentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Artifact Assignment Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    void testAssignArtifactToWizardWizardNotFound() throws Exception {
        //given
        doThrow(new ObjectNotFoundException(ExceptionConstants.WIZARD,999))
                .when(this.wizardService).assignArtifact(999, "1250808601744904192");

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl+"/wizards/999/artifacts/1250808601744904192")
                        .with(this.jwtAuthentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard with id 999 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAssignArtifactToWizardArtifactNotFound() throws Exception {
        //given
        doThrow(new ObjectNotFoundException(ExceptionConstants.ARTIFACT,"1250808601744904192"))
                .when(this.wizardService).assignArtifact(1, "1250808601744904192");

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.put(this.baseUrl+"/wizards/1/artifacts/1250808601744904192")
                        .with(this.jwtAuthentication)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find artifact with id 1250808601744904192 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}