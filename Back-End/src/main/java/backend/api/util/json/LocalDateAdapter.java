package backend.api.util.json;

import backend.api.exception.CustomException;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) throws JsonParseException {
        try {
            return new JsonPrimitive(src.format(formatter));
        }
        catch (JsonParseException e) {
            throw new CustomException("JsonParseException","Eroare la transformarea din LocalDate la Json",e.getMessage());

        }
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws CustomException {
        try {
            return LocalDate.parse(json.getAsString(), formatter);
        }
        catch (JsonParseException e) {
            throw new CustomException("JsonParseException","Eroare la transformarea din Json la LocalDate",e.getMessage());
        }
    }
}
