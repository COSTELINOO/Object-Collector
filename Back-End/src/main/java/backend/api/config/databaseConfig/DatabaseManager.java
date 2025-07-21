package backend.api.config.databaseConfig;

import backend.api.exception.CustomException;
import backend.api.exception.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseManager {

    private static String databaseUrl;
    private static String databaseUser;
    private static String databasePassword;
    private static String databaseDriver;

    private static boolean initialized = false;
    private static DatabaseManager instance;

    //singleton, imi garanteaza ca baza de date este instantiata si configurata o singura data
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            Logger.info("Crearea unei noi instanțe DatabaseManager");
            instance = new DatabaseManager();
        }
        return instance;
    }

    public  synchronized void configDatabase(String url, String user, String password, String driver) throws CustomException {
        if (url == null || url.isEmpty()) {
            throw new CustomException("DatabaseConfigError", "URL invalid", "URL-ul bazei de date nu poate fi gol");
        }

        if (driver == null || driver.isEmpty()) {
            throw new CustomException("DatabaseConfigError", "Driver invalid", "Driver-ul bazei de date nu poate fi gol");
        }

        Logger.info("Configurare bază de date cu URL: " + url + " și driver: " + driver);
        databaseUrl = url;
        databaseUser = user;
        databasePassword = password;
        databaseDriver = driver;
        Logger.success("Baza de date a fost configurată");
    }

    public static synchronized void initialize() throws CustomException {
        if (databaseDriver == null || databaseUrl == null) {
            throw new CustomException("DatabaseInitError", "Configurare lipsă",
                    "Baza de date nu a fost configurată. Apelați configDatabase() mai întâi.");
        }

        if (!initialized) {
            try {
                try {
                    Logger.info("Încărcare driver bază de date: " + databaseDriver);

                    //imi incarca la runtime driverul si returneaza o exceptie de tip ClassNotFound, daca nu exista driver-ul respectiv
                    Class.forName(databaseDriver);
                    Logger.success("Driver bază de date încărcat cu succes");
                } catch (ClassNotFoundException e) {
                    throw new CustomException("DatabaseDriverError", "Driver negăsit", e);
                }

                Logger.info("Inițializare structură bază de date...");
                //Functie proprie: configurare tabele in baza de date
                createTablesIfNotExist();
                initialized = true;
                Logger.success("Baza de date a fost inițializată cu succes");
            } catch (SQLException e) {
                throw new CustomException("DatabaseConnectionError", "Eroare de conexiune",
                        e);
            }
        } else {
            Logger.debug("Baza de date este deja inițializată");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (databaseUrl == null) {
            throw new SQLException("Baza de date nu a fost configurată");
        }
            return DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);

    }

    private static void createTablesIfNotExist() throws SQLException {
        Logger.info("Verificare și creare tabele în baza de date...");
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                createUsersTable(conn);
                createCollectionsTable(conn);
                createCustomCollectionFieldsTable(conn);
                createObjectsTable(conn);
                createViewsTable(conn);
                createLikesTable(conn);

                conn.commit();
                Logger.success("Toate tabelele au fost verificate/create cu succes");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void createUsersTable(Connection conn) throws SQLException {
        Logger.info("Verificare/creare tabel users");
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY NOT NULL, " +
                    "username VARCHAR(20) NOT NULL UNIQUE, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "cod_token VARCHAR(6) NOT NULL, " +
                    "cod_resetare VARCHAR(6) NOT NULL, " +
                    "image VARCHAR(500) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            stmt.execute(sql);
            Logger.debug("Tabelul users a fost verificat/creat");
        }
    }

    private static void createCollectionsTable(Connection conn) throws SQLException {
        Logger.info("Verificare/creare tabel collections");
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS collections (" +
                    "id SERIAL PRIMARY KEY NOT NULL, " +
                    "id_user INTEGER NOT NULL, " +
                    "id_tip INTEGER NOT NULL, " +
                    "nume VARCHAR(200) NOT NULL, " +
                    "visibility BOOLEAN NOT NULL DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_user FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE " +
                    ")";

            stmt.execute(sql);
            Logger.debug("Tabelul collections a fost verificat/creat");
        }
    }

    private static void createCustomCollectionFieldsTable(Connection conn) throws SQLException {
        Logger.info("Verificare/creare tabel custom_collections");
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS custom_collections (" +
                    "id SERIAL PRIMARY KEY NOT NULL, " +
                    "id_colectie INTEGER NOT NULL, " +
                    "material BOOLEAN DEFAULT FALSE, " +
                    "valoare BOOLEAN DEFAULT FALSE, " +
                    "greutate BOOLEAN DEFAULT FALSE, " +
                    "nume_artist BOOLEAN DEFAULT FALSE, " +
                    "tematica BOOLEAN DEFAULT FALSE, " +
                    "gen BOOLEAN DEFAULT FALSE, " +
                    "casa_discuri BOOLEAN DEFAULT FALSE, " +
                    "tara BOOLEAN DEFAULT FALSE, " +
                    "an BOOLEAN DEFAULT FALSE, " +
                    "stare BOOLEAN DEFAULT FALSE, " +
                    "raritate BOOLEAN DEFAULT FALSE, " +
                    "pret_achizitie BOOLEAN DEFAULT FALSE, " +
                    "CONSTRAINT fk_custom_colectie FOREIGN KEY (id_colectie) REFERENCES collections(id) ON DELETE CASCADE" +
                    ")";

            stmt.execute(sql);
            Logger.debug("Tabelul custom_collections a fost verificat/creat");
        }
    }

    private static void createObjectsTable(Connection conn) throws SQLException {
        Logger.info("Verificare/creare tabel objects");
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS objects (" +
                    "id SERIAL PRIMARY KEY NOT NULL, " +
                    "id_colectie INTEGER NOT NULL, " +
                    "nume_colectie VARCHAR(100), " +
                    "descriere VARCHAR(500) DEFAULT 'NO DESCRIPTION', " +
                    "material VARCHAR(100) DEFAULT 'UNKNOWN', " +
                    "valoare REAL, " +
                    "greutate INTEGER, " +
                    "nume_artist VARCHAR(50), " +
                    "tematica VARCHAR(50) DEFAULT 'UNKNOWN', " +
                    "gen VARCHAR(50), " +
                    "casa_discuri VARCHAR(50), " +
                    "tara VARCHAR(100) DEFAULT 'UNKNOWN', " +
                    "an DATE, " +
                    "stare VARCHAR(50) DEFAULT 'UNKNOWN', " +
                    "raritate VARCHAR(50) DEFAULT 'UNKNOWN', " +
                    "pret_achizitie REAL, " +
                    "image VARCHAR(500) NOT NULL, " +
                    "visibility BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_colectie FOREIGN KEY (id_colectie) REFERENCES collections(id) ON DELETE CASCADE" +
                    ")";

            stmt.execute(sql);
            Logger.debug("Tabelul objects a fost verificat/creat");
        }
    }

    private static void createViewsTable(Connection conn) throws SQLException {
        Logger.info("Verificare/creare tabel views");
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS views (" +
                    "id SERIAL PRIMARY KEY NOT NULL, " +
                    "id_obiect INTEGER NOT NULL, " +
                    "data DATE NOT NULL DEFAULT CURRENT_DATE, " +
                    "ora TIME DEFAULT CURRENT_TIME, " +
                    "id_user INTEGER NOT NULL, " +
                    "CONSTRAINT fk_obiect_view FOREIGN KEY (id_obiect) REFERENCES objects(id) ON DELETE CASCADE, " +
                    "CONSTRAINT fk_user_view FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE " +
                    ")";

            stmt.execute(sql);
            Logger.debug("Tabelul views a fost verificat/creat");
        }
    }

    private static void createLikesTable(Connection conn) throws SQLException {
        Logger.info("Verificare/creare tabel likes");
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS likes (" +
                    "id SERIAL PRIMARY KEY NOT NULL, " +
                    "id_obiect INTEGER NOT NULL, " +
                    "data DATE NOT NULL DEFAULT CURRENT_DATE, " +
                    "ora TIME DEFAULT CURRENT_TIME, " +
                    "id_user INTEGER NOT NULL, " +
                    "CONSTRAINT fk_obiect_like FOREIGN KEY (id_obiect) REFERENCES objects(id) ON DELETE CASCADE, " +
                    "CONSTRAINT fk_user_like FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE " +
                    ")";

            stmt.execute(sql);
            Logger.debug("Tabelul likes a fost verificat/creat");
        }
    }

}