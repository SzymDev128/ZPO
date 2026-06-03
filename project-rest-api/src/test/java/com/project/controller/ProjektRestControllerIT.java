package com.project.controller;

import com.project.model.Projekt;
import com.project.service.ProjektService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(ProjektRestController.class)
@AutoConfigureJsonTesters
@WithMockUser(username = "admin", password = "admin")
public class ProjektRestControllerIT {

    private final String apiPath = "/api/projekty";

    @MockitoBean
    private ProjektService mockProjektService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<Projekt> jacksonTester;

    @Test
    void getProjekt_whenValidId_returnsProjekt() throws Exception {
        Integer projektId = 1;
        Projekt expectedProjekt = createProjektTestowy(projektId, "Nazwa testowa");
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.of(expectedProjekt));

        mockMvc.perform(get(apiPath + "/{projektId}", projektId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projektId").value(projektId))
                .andExpect(jsonPath("$.nazwa").value(expectedProjekt.getNazwa()));

        verify(mockProjektService).getProjekt(projektId);
        verifyNoMoreInteractions(mockProjektService);
    }

    @Test
    void getProjekt_whenInvalidId_returnsNotFound() throws Exception {
        Integer projektId = 2;
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.empty());

        mockMvc.perform(get(apiPath + "/{projektId}", projektId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(mockProjektService).getProjekt(projektId);
        verifyNoMoreInteractions(mockProjektService);
    }

    @Test
    void getProjekty_whenAvailable_returnsPagedContent() throws Exception {
        Projekt projekt1 = createProjektTestowy(1, "Nazwa testowa 1");
        Projekt projekt2 = createProjektTestowy(2, "Nazwa testowa 2");
        Page<Projekt> expectedPage = new PageImpl<>(List.of(projekt1, projekt2));
        given(mockProjektService.getProjekty(any(Pageable.class))).willReturn(expectedPage);

        mockMvc.perform(get(apiPath).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]").exists())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(mockProjektService).getProjekty(any(Pageable.class));
        verifyNoMoreInteractions(mockProjektService);
    }

    @Test
    void createProjekt_whenValidData_returnsCreatedWithLocation() throws Exception {
        Projekt projektToSave = createProjektTestowy(null, "Nazwa testowa");
        Projekt createdProjekt = createProjektTestowy(1, "Nazwa testowa");
        String jsonProjekt = jacksonTester.write(projektToSave).getJson();
        given(mockProjektService.setProjekt(any(Projekt.class))).willReturn(createdProjekt);

        mockMvc.perform(post(apiPath)
                        .with(csrf())
                        .content(jsonProjekt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString(apiPath + "/1")));

        verify(mockProjektService).setProjekt(any(Projekt.class));
        verifyNoMoreInteractions(mockProjektService);
    }

    @Test
    void createProjekt_whenEmptyName_returnsBadRequest() throws Exception {
        Projekt invalidProjekt = createProjektTestowy(null, "");
        var result = mockMvc.perform(post(apiPath)
                        .with(csrf())
                        .content(jacksonTester.write(invalidProjekt).getJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(mockProjektService, never()).setProjekt(any(Projekt.class));
        assertThat(result.getResolvedException())
                .isNotNull()
                .isInstanceOf(MethodArgumentNotValidException.class);
    }

    @Test
    void getProjekty_withPagingParams_passesParamsToService() throws Exception {
        mockMvc.perform(get(apiPath)
                        .param("page", "5")
                        .param("size", "15")
                        .param("sort", "nazwa,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(mockProjektService).getProjekty(pageableCaptor.capture());
        PageRequest pageable = (PageRequest) pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(5);
        assertThat(pageable.getPageSize()).isEqualTo(15);
        assertThat(pageable.getSort().getOrderFor("nazwa").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    private Projekt createProjektTestowy(Integer id, String nazwa) {
        return Projekt.builder()
                .projektId(id)
                .nazwa(nazwa)
                .opis("Opis testowy")
                .dataOddania(LocalDate.of(2026, 6, 1))
                .build();
    }

    @BeforeEach
    void before(TestInfo testInfo) {
        log.info("-- METODA -> {}", testInfo.getTestMethod().orElseThrow().getName());
    }

    @AfterEach
    void after(TestInfo testInfo) {
        log.info("<- KONIEC -- {}", testInfo.getTestMethod().orElseThrow().getName());
    }
}