package com.project.dao;

import java.time.LocalDate;
import java.util.List;
import com.project.model.Projekt;

/**
 * Interfejs DAO (Data Access Object) dla encji Projekt.
 *
 * Hermetyzuje konkretną implementację dostępu do danych –
 * reszta aplikacji nie wie, czy dane pochodzą z SQL, pliku, API itp.
 */
public interface ProjektDAO {

    /** Pobiera projekt po kluczu głównym. */
    Projekt getProjekt(Integer projektId);

    /** Wstawia nowy projekt (projektId == null) lub aktualizuje istniejący. */
    void setProjekt(Projekt projekt);

    /** Usuwa projekt o podanym id. */
    void deleteProjekt(Integer projektId);

    /** Zwraca stronę projektów (offset + limit). */
    List<Projekt> getProjekty(Integer offset, Integer limit);

    /** Wyszukuje projekty po fragmencie nazwy (LIKE). */
    List<Projekt> getProjektyWhereNazwaLike(String nazwa, Integer offset, Integer limit);

    /** Wyszukuje projekty po dacie oddania. */
    List<Projekt> getProjektyWhereDataOddaniaIs(LocalDate dataOddania, Integer offset, Integer limit);

    /** Łączna liczba wierszy (do paginacji). */
    int getRowsNumber();

    /** Liczba wierszy pasujących do nazwy. */
    int getRowsNumberWhereNazwaLike(String nazwa);

    /** Liczba wierszy pasujących do daty oddania. */
    int getRowsNumberWhereDataOddaniaIs(LocalDate dataOddania);
}
