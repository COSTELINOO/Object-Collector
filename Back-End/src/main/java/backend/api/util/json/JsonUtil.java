package backend.api.util.json;

import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    public static String toJson(Object obj) throws CustomException {
        if (obj == null) {
            Logger.warning("Încercare de serializare a unui obiect null");
            return "null";
        }

        try {
            return gson.toJson(obj);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new Exception500.InternalServerErrorException(
                    "JsonError",
                    "SerializationFailed",
                    "Eroare la serializarea obiectului în JSON",
                    e
            );
        }
    }

    public static <T> T fromJson(String json, Class<T> classOfT) throws CustomException {
        String cleaned = Jsoup.clean(json, Safelist.basic());
        if (cleaned.isEmpty()||cleaned.equals("null")) {
            Logger.malicious("[MALICIOUS] [XSS DETECTAT] Valoare body: "+ json);
            throw new CustomException(
                    "JsonError",
                    "XSS atack",
                    "Malicious attack"
            );

        }

        try {
            if(!json.equals(cleaned)){
                Logger.malicious("[MALICIOUS] [XSS DETECTAT] Valoare body: "+ json);
            }
            return gson.fromJson(cleaned, classOfT);
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new Exception500.InternalServerErrorException(
                    "JsonError",
                    "DeserializationFailed",
                    "Eroare la deserializarea JSON în obiect",
                    e
            );
        }
    }

}