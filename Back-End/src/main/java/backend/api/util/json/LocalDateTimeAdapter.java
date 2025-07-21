package backend.api.util.json;

import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) throws JsonParseException {
        if (src == null) {
            Logger.warning("Încercare de serializare a unei date și ore null");
            return JsonNull.INSTANCE;
        }

        try {
            String formattedDateTime = src.format(FORMATTER);
            return new JsonPrimitive(formattedDateTime);
        } catch (DateTimeException e) {
            throw new JsonParseException("Eroare la serializarea LocalDateTime: " + e.getMessage());
        }
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            Logger.warning("Încercare de deserializare a unei valori JSON null în LocalDateTime");
            return null;
        }

        try {
            String dateTimeString = json.getAsString();
            return LocalDateTime.parse(dateTimeString, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Eroare la deserializarea JSON în LocalDateTime: " + e.getMessage());
        } catch (IllegalStateException e) {
            throw new JsonParseException("Element JSON invalid pentru LocalDateTime: " + e.getMessage());
        }
    }
}