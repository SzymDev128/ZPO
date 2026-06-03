package com.project.controller;

import com.project.model.Projekt;
import com.project.service.ProjektService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProjektRestControllerTest {

    @Mock
    private ProjektService mockProjektService;

    @InjectMocks
    private ProjektRestController projectRestController;

    @Test
    void getProjekt_whenValidId_returnsProjekt() {
        // GIVEN
        Integer projektId = 1;
        Projekt expectedProjekt = createProjektTestowy(projektId, "Nazwa testowa");
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.of(expectedProjekt));
        // WHEN
        ResponseEntity<Projekt> responseEntity = projectRestController.getProjekt(projektId);
        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expectedProjekt);
    }

    @Test
    void getProjekt_whenInvalidId_returnsNotFound() {
        // GIVEN
        Integer projektId = 1;
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.empty());
        // WHEN
        ResponseEntity<Projekt> responseEntity = projectRestController.getProjekt(projektId);
        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProjekty_returnsPageWithProjekty() {
        // GIVEN
        List<Projekt> lista = List.of(
                createProjektTestowy(1, "Projekt 1"),
                createProjektTestowy(2, "Projekt 2"));
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Projekt> page = new PageImpl<>(lista, pageable, 5);
        given(mockProjektService.getProjekty(pageable)).willReturn(page);
        // WHEN
        Page<Projekt> result = projectRestController.getProjekty(pageable);
        // THEN
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void createProjekt_whenValidData_returnsCreated() {
        // GIVEN
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Projekt projektToSave = createProjektTestowy(null, "Nowy projekt");
        Projekt created = createProjektTestowy(1, "Nowy projekt");
        given(mockProjektService.setProjekt(projektToSave)).willReturn(created);
        // WHEN
        ResponseEntity<Void> responseEntity = projectRestController.createProjekt(projektToSave);
        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getHeaders().getLocation().getPath()).isEqualTo("/1");
    }

    @Test
    void deleteProjekt_whenValidId_returnsOk() {
        // GIVEN
        Integer projektId = 1;
        given(mockProjektService.getProjekt(projektId)).willReturn(Optional.of(createProjektTestowy(projektId, "Test")));
        // WHEN
        ResponseEntity<Void> responseEntity = projectRestController.deleteProjekt(projektId);
        // THEN
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mockProjektService).deleteProjekt(projektId);
    }

    private Projekt createProjektTestowy(Integer id, String nazwa) {
        return Projekt.builder()
                .projektId(id)
                .nazwa(nazwa)
                .opis("Opis testowy")
                .dataOddania(LocalDate.of(2026, 6, 1))
                .build();
    }

    @AfterEach
    void resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();
    }
}