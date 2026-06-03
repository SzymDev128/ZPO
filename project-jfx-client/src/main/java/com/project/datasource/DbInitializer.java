package com.project.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inicjalizuje schemat bazy danych przy starcie aplikacji.
 *
 * Polecenia SQL są wykonywane w ramach transakcji – albo wszystkie,
 * albo żadne (rollback przy błędzie).
 * Użycie IF NOT EXISTS gwarantuje idempotentność – można uruchamiać wielokrotnie.
 */
public class DbInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DbInitializer.class);

    private static final String[] queries = {
        """
        CREATE TABLE IF NOT EXISTS projekt(
            projekt_id SERIAL,
            nazwa VARCHAR(50) NOT NULL,
            opis VARCHAR(1000),
            dataczas_utworzenia TIMESTAMP DEFAULT now(),
            data_oddania DATE,
            CONSTRAINT projekt_pk PRIMARY KEY (projekt_id)
        );
        CREATE TABLE IF NOT EXISTS zadanie(
            zadanie_id SERIAL,
            nazwa VARCHAR(50) NOT NULL,
            opis VARCHAR(1000),
            kolejnosc INTEGER,
            dataczas_utworzenia TIMESTAMP DEFAULT now(),
            projekt_id INTEGER NOT NULL,
            CONSTRAINT zadanie_pk PRIMARY KEY (zadanie_id)
        );
        """,
        """
        CREATE INDEX IF NOT EXISTS projekt_nazwa_idx ON projekt(nazwa);
        CREATE INDEX IF NOT EXISTS zadanie_nazwa_idx ON zadanie(nazwa);
        ALTER TABLE zadanie ADD CONSTRAINT IF NOT EXISTS zadanie_projekt_fk
            FOREIGN KEY (projekt_id) REFERENCES projekt (projekt_id) ON DELETE CASCADE;
        ALTER TABLE zadanie ADD CONSTRAINT IF NOT EXISTS unique_kolejnosc
            UNIQUE (kolejnosc, projekt_id);
        """
    };

    private DbInitializer() {}

    /** Tworzy tabele i indeksy (jeśli jeszcze nie istnieją). */
    public static void init() {
        try (Connection connection = DataSource.getConnection()) {
            boolean initialAutocommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement stmt = connection.createStatement()) {
                for (int i = 0; i < queries.length; i++) {
                    stmt.executeUpdate(queries[i]);
                    logger.info("QUERY {} wykonane pomyślnie", i + 1);
                }
                connection.commit();
                logger.info("Schemat bazy danych zainicjalizowany.");
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            } finally {
                if (initialAutocommit) connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
