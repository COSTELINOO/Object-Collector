package backend.api.config.applicationConfig;

import backend.api.exception.CustomException;
import backend.api.exception.Logger;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Properties {
    //este un map de tip: proprietate(cheie) -> propertyValue(valoare)
    private static final java.util.Properties props = new java.util.Properties();

    //fisier de proprietati
    //se executa o singura data la prima apelare a unei functii din aceasta clasa
    private static final String PROPERTIES_FILE = "application.properties";
    //obtine path-ul curent
    private static final String PROPERTIES_PATH = getPath() + "/config/" + PROPERTIES_FILE;


    static {
        try {
            //getClassLoader= cauta prin toate fisierele proiectului
            //getResourceStream= returneaza resursa ca stream daca exista ca Input stream(fisier de citite octet cu octet)
            try (InputStream input = Properties.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
                if (input != null)
                {
                    props.load(input);
                    Logger.success("Fișierul de proprietăți s-a încărcat cu succes din classpath");

                } else {
                    //verifica daca fisierul exista la PROPERTIES_PATH= o cale personalizata setata de mine unde vreau sa fie fisierul
                    File file = new File(PROPERTIES_PATH);

                    if (file.exists()) {
                        try (InputStream fileInput = new java.io.FileInputStream(file)) {
                            //incarca proprietatile din fisier dupa nume si valoare( nume = valoare)
                            props.load(fileInput);
                            Logger.success("Fișierul de proprietăți s-a încărcat cu succes din director");
                        }
                    } else {
                        Logger.warning("Fișierul de proprietăți nu a fost găsit. Se vor folosi valorile implicite.");
                    }
                }
            }
        } catch (IOException e) {
            throw new CustomException("PropertiesLoadError", "Eroare la încărcarea fișierului de proprietăți", e);
        }
    }

    public static int getPort() {
        try {
            return Integer.parseInt(props.getProperty("server.port", "2222"));
        }
        catch (NumberFormatException e) {
            Logger.exception("InvalidPortException");
            Logger.error("Formatul portului specificat în fișierul de proprietăți este invalid. Se folosește portul implicit 2222.");
            return 2222;
        }
    }

    public static String getDatabaseUrl() throws CustomException {
        String url = props.getProperty("db.url");
        if (url == null || url.trim().isEmpty()) {
            throw new CustomException("ConfigurationError", "URL-ul bazei de date lipsește",
                    "Proprietatea db.url nu este definită în fișierul de configurare");
        }
        return url;
    }

    public static String getAddress() {
        String address = props.getProperty("server.address");
        if (address == null || address.trim().isEmpty())
        {
            Logger.warning("Adresa serverului nu este specificată. Se folosește localhost.");
            return "localhost";
        }
        return address;
    }

    public static String getDatabaseUser() throws CustomException {
        String user = props.getProperty("db.user");

        if (user == null || user.trim().isEmpty())
        {
            throw new CustomException("ConfigurationError", "Utilizatorul bazei de date lipsește",
                    "Proprietatea db.user nu este definită în fișierul de configurare");
        }
        return user;
    }

    public static String getDatabasePassword() throws CustomException {

        String password = props.getProperty("db.password");

        if (password == null || password.trim().isEmpty()) {
            throw new CustomException("ConfigurationError", "Parola bazei de date lipsește",
                    "Proprietatea db.password nu este definită în fișierul de configurare");
        }

        return password;
    }

    public static String getDatabaseDriver() throws CustomException {
        String driver = props.getProperty("db.driver");

        if (driver == null || driver.trim().isEmpty()) {

            throw new CustomException("ConfigurationError", "Driver-ul bazei de date lipsește",
                    "Proprietatea db.driver nu este definită în fișierul de configurare");

        }
        return driver;
    }

    public static String getCorsAllowOrigin() {
        return props.getProperty("cors.allow.origin", "*");
    }

    public static String getCorsAllowHeaders() {
        return props.getProperty("cors.allow.headers", "Content-Type, Authorization");
    }

    public static String getCorsAllowMethods() {
        return props.getProperty("cors.allow.methods", "GET, POST, PUT, DELETE, OPTIONS");
    }

    public static String getStaffEmail() throws CustomException {
        String email = props.getProperty("staff.email");
        if (email == null || email.trim().isEmpty()) {

            throw new CustomException("ConfigurationError", "Adresa de email a staff-ului lipsește",
                    "Proprietatea staff.email nu este definită în fișierul de configurare");
        }
        return email;
    }

    public static String getStaffPassword() throws CustomException {
        String password = props.getProperty("staff.email.password");
        if (password == null || password.trim().isEmpty()) {

            throw new CustomException("ConfigurationError", "Parola pentru email-ul staff-ului lipsește",
                    "Proprietatea staff.email.password nu este definită în fișierul de configurare");

        }
        return password;
    }

    public static String getPath() {
        return new File("").getAbsolutePath() + "/OCo";
    }

    public static String getFrontPath() {
        return new File("../").getAbsolutePath() + "/front-end";
    }

}