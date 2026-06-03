package com.project.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Singleton zapewniający pulę połączeń z bazą danych HSQLDB (HikariCP).
 *
 * Wzorzec Singleton – tylko jedna instancja HikariDataSource w całej aplikacji.
 * Inicjalizacja następuje przy pierwszym załadowaniu klasy (statyczny blok inicjalizujący).
 */
public class DataSource {

    private static final String DB_DIR  = "db";
    private static final String DB_NAME = "projekty";
    private static final String DB_USERNAME      = "admin";
    private static final String DB_USER_PASSWORD = "admin";

    // sql.syntax_pgs – włącza obsługę typów TEXT i SERIAL oraz funkcji NEXTVAL/CURRVAL/LASTVAL
    // hsqldb.write_delay=false – natychmiastowy zapis na dysk (brak opóźnienia)
    private static final String HSQL_ADDITIONAL_PARAMS = ";hsqldb.write_delay=false;sql.syntax_pgs=true";

    private static final String DB_URL =
            String.format("jdbc:hsqldb:file:%s/%s%s", DB_DIR, DB_NAME, HSQL_ADDITIONAL_PARAMS);

    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USERNAME);
        config.setPassword(DB_USER_PASSWORD);
        config.setMaximumPoolSize(1); // jedna współdzielona baza plikowa – wystarczy 1 połączenie
        ds = new HikariDataSource(config);
    }

    // Prywatny konstruktor – uniemożliwia tworzenie instancji
    private DataSource() {}

    /** Zwraca połączenie z puli. Należy je zamknąć po użyciu (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
