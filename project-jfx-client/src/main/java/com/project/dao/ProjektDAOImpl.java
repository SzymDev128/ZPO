package com.project.dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.project.datasource.DataSource;
import com.project.model.Projekt;

/**
 * Implementacja ProjektDAO korzystająca z JDBC i bazy HSQLDB.
 *
 * Każda metoda:
 *  - pobiera połączenie z puli (try-with-resources → auto-close),
 *  - używa PreparedStatement (ochrona przed SQL Injection),
 *  - rzuca RuntimeException owijając SQLException.
 */
public class ProjektDAOImpl implements ProjektDAO {

    // -----------------------------------------------------------------------
    // Pomocnicza metoda mapująca bieżący wiersz ResultSet → obiekt Projekt
    // -----------------------------------------------------------------------
    private Projekt mapRow(ResultSet rs) throws SQLException {
        Projekt p = new Projekt();
        p.setProjektId(rs.getInt("projekt_id"));
        p.setNazwa(rs.getString("nazwa"));
        p.setOpis(rs.getString("opis"));
        p.setDataCzasUtworzenia(rs.getObject("dataczas_utworzenia", LocalDateTime.class));
        p.setDataOddania(rs.getObject("data_oddania", LocalDate.class));
        return p;
    }

    // -----------------------------------------------------------------------
    // INSERT lub UPDATE – decyzja na podstawie projektId == null
    // -----------------------------------------------------------------------
    @Override
    public void setProjekt(Projekt projekt) {
        boolean isInsert = projekt.getProjektId() == null;
        String query = isInsert
            ? "INSERT INTO projekt(nazwa, opis, dataczas_utworzenia, data_oddania) VALUES (?, ?, ?, ?)"
            : "UPDATE projekt SET nazwa=?, opis=?, dataczas_utworzenia=?, data_oddania=? WHERE projekt_id=?";

        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, projekt.getNazwa());
            ps.setString(2, projekt.getOpis());
            if (projekt.getDataCzasUtworzenia() == null)
                projekt.setDataCzasUtworzenia(LocalDateTime.now());
            ps.setObject(3, projekt.getDataCzasUtworzenia());
            ps.setObject(4, projekt.getDataOddania());
            if (!isInsert) ps.setInt(5, projekt.getProjektId());

            int rows = ps.executeUpdate();

            // Po INSERT pobieramy wygenerowany klucz główny i ustawiamy go w obiekcie
            if (isInsert && rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) projekt.setProjektId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // SELECT po kluczu głównym
    // -----------------------------------------------------------------------
    @Override
    public Projekt getProjekt(Integer projektId) {
        String query = "SELECT * FROM projekt WHERE projekt_id = ?";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, projektId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // DELETE
    // -----------------------------------------------------------------------
    @Override
    public void deleteProjekt(Integer projektId) {
        String query = "DELETE FROM projekt WHERE projekt_id = ?";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, projektId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // SELECT – strona (offset + limit)
    // -----------------------------------------------------------------------
    @Override
    public List<Projekt> getProjekty(Integer offset, Integer limit) {
        List<Projekt> lista = new ArrayList<>();
        String query = "SELECT * FROM projekt ORDER BY dataczas_utworzenia DESC"
                + (offset != null ? " OFFSET ?" : "")
                + (limit  != null ? " LIMIT ?"  : "");
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            int i = 1;
            if (offset != null) ps.setInt(i++, offset);
            if (limit  != null) ps.setInt(i,   limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    // -----------------------------------------------------------------------
    // SELECT – wyszukiwanie po nazwie (LIKE)
    // -----------------------------------------------------------------------
    @Override
    public List<Projekt> getProjektyWhereNazwaLike(String nazwa, Integer offset, Integer limit) {
        List<Projekt> lista = new ArrayList<>();
        String query = "SELECT * FROM projekt WHERE LOWER(nazwa) LIKE LOWER(?)"
                + " ORDER BY dataczas_utworzenia DESC"
                + (offset != null ? " OFFSET ?" : "")
                + (limit  != null ? " LIMIT ?"  : "");
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            int i = 1;
            ps.setString(i++, "%" + nazwa + "%");
            if (offset != null) ps.setInt(i++, offset);
            if (limit  != null) ps.setInt(i,   limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    // -----------------------------------------------------------------------
    // SELECT – wyszukiwanie po dacie oddania
    // -----------------------------------------------------------------------
    @Override
    public List<Projekt> getProjektyWhereDataOddaniaIs(LocalDate dataOddania, Integer offset, Integer limit) {
        List<Projekt> lista = new ArrayList<>();
        String query = "SELECT * FROM projekt WHERE data_oddania = ?"
                + " ORDER BY dataczas_utworzenia DESC"
                + (offset != null ? " OFFSET ?" : "")
                + (limit  != null ? " LIMIT ?"  : "");
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            int i = 1;
            ps.setObject(i++, dataOddania);
            if (offset != null) ps.setInt(i++, offset);
            if (limit  != null) ps.setInt(i,   limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    // -----------------------------------------------------------------------
    // COUNT
    // -----------------------------------------------------------------------
    @Override
    public int getRowsNumber() {
        String query = "SELECT COUNT(*) FROM projekt";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public int getRowsNumberWhereNazwaLike(String nazwa) {
        String query = "SELECT COUNT(*) FROM projekt WHERE LOWER(nazwa) LIKE LOWER(?)";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, "%" + nazwa + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public int getRowsNumberWhereDataOddaniaIs(LocalDate dataOddania) {
        String query = "SELECT COUNT(*) FROM projekt WHERE data_oddania = ?";
        try (Connection conn = DataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setObject(1, dataOddania);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
