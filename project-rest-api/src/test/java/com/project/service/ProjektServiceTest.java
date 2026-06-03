package com.project.service;

import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.repository.ProjektRepository;
import com.project.repository.ZadanieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjektServiceTest {

    @Mock
    private ProjektRepository mockProjektRepository;

    @Mock
    private ZadanieRepository mockZadanieRepository;

    @InjectMocks
    private ProjektServiceImpl projektService;

    @Test
    void getProjekt_whenValidId_returnsOptionalWithProjekt() {
        Integer projektId = 1;
        Projekt expectedProjekt = createProjektTestowy(projektId, "Testowy Projekt");
        given(mockProjektRepository.findById(projektId)).willReturn(Optional.of(expectedProjekt));

        Optional<Projekt> actual = projektService.getProjekt(projektId);

        assertThat(actual).isPresent().contains(expectedProjekt);
        verify(mockProjektRepository).findById(projektId);
    }

    @Test
    void setProjekt_whenCalled_savesAndReturnsProjekt() {
        Projekt projektToSave = createProjektTestowy(null, "Nowy Projekt");
        Projekt savedProjekt = createProjektTestowy(1, "Nowy Projekt");
        given(mockProjektRepository.save(projektToSave)).willReturn(savedProjekt);

        Projekt actual = projektService.setProjekt(projektToSave);

        assertThat(actual).isEqualTo(savedProjekt);
        verify(mockProjektRepository).save(projektToSave);
    }

    @Test
    void deleteProjekt_whenValidId_deletesTasksAndProjekt() {
        Integer projektId = 1;
        List<Zadanie> zadania = List.of(
                createZadanieTestowe(1, "Zadanie 1"),
                createZadanieTestowe(2, "Zadanie 2"));
        given(mockZadanieRepository.findZadaniaProjektu(projektId)).willReturn(zadania);

        projektService.deleteProjekt(projektId);

        verify(mockZadanieRepository, times(1)).delete(zadania.get(0));
        verify(mockZadanieRepository, times(1)).delete(zadania.get(1));
        verify(mockProjektRepository).deleteById(projektId);
    }

    @Test
    void getProjekty_whenCalled_returnsPageFromRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Projekt> expectedPage = new PageImpl<>(List.of(
                createProjektTestowy(1, "Projekt 1"),
                createProjektTestowy(2, "Projekt 2")));
        given(mockProjektRepository.findAll(pageable)).willReturn(expectedPage);

        Page<Projekt> actual = projektService.getProjekty(pageable);

        assertThat(actual).isEqualTo(expectedPage);
        verify(mockProjektRepository).findAll(pageable);
    }

    @Test
    void searchByNazwa_whenPhraseProvided_callsRepositoryWithCorrectParams() {
        String phrase = "java";
        Pageable pageable = PageRequest.of(0, 5);
        given(mockProjektRepository.findByNazwaContainingIgnoreCase(any(), any())).willReturn(Page.empty());

        projektService.searchByNazwa(phrase, pageable);

        verify(mockProjektRepository).findByNazwaContainingIgnoreCase(phrase, pageable);
    }

    private Projekt createProjektTestowy(Integer id, String nazwa) {
        return Projekt.builder()
                .projektId(id)
                .nazwa(nazwa)
                .opis("Opis testowy")
                .dataOddania(LocalDate.of(2026, 6, 1))
                .build();
    }

    private Zadanie createZadanieTestowe(Integer id, String nazwa) {
        return Zadanie.builder()
                .zadanieId(id)
                .nazwa(nazwa)
                .build();
    }
}