package backend.api.util.files;

import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.PersonalStatistics.StatisticsCollection;
import backend.api.model.PersonalStatistics.StatisticsObject;
import backend.api.model.PersonalStatistics.StatisticsProfile;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CsvPersonalStatistics {

    // Constante pentru formatare și separatori
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String CSV_SEPARATOR = ",";


    private static final int COLLECTION_FIELD_COUNT = 16;
    private static final int OBJECT_FIELD_COUNT = 20;
    private static final int ATTRIBUTE_FIELD_COUNT = 11;


    public static void generateStatisticsCsv(StatisticsProfile statistics, String outputPath) throws CustomException {
        if (statistics == null) {
            throw new Exception400.BadRequestException(
                    "CsvError",
                    "NullStatistics",
                    "Obiectul cu statistici este null"
            );
        }

        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "CsvError",
                    "InvalidOutputPath",
                    "Cale fișier CSV invalidă"
            );
        }

        Logger.info("Generare CSV cu statistici personale pentru utilizatorul ID: " + statistics.getId() + ", cale: " + outputPath);

        try {
            Path directory = Paths.get(outputPath).getParent();
            if (directory != null && !Files.exists(directory)) {
                Files.createDirectories(directory);
                Logger.debug("Director creat pentru CSV: " + directory);
            }
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "DirectoryCreationFailed",
                    "Nu s-a putut crea directorul pentru CSV: " + e.getMessage(),
                    e
            );
        }

        try (FileWriter csvWriter = new FileWriter(outputPath)) {
            // Scrie header-ul CSV
            writeHeader(csvWriter);
            Logger.debug("Header CSV scris");

            if (statistics.getColectii() == null || statistics.getColectii().isEmpty()) {
                writeRowWithoutCollectionAndObject(csvWriter, statistics);
                Logger.debug("Rând fără colecție și obiect scris pentru utilizatorul ID: " + statistics.getId());
            } else {
                int collectionCount = 0;
                int objectCount = 0;

                for (StatisticsCollection colectie : statistics.getColectii()) {
                    collectionCount++;

                    if (colectie.getObiecte() == null || colectie.getObiecte().isEmpty()) {
                        writeRowWithoutObject(csvWriter, statistics, colectie);
                        Logger.debug("Rând fără obiect scris pentru colecția: " + colectie.getNume());
                    } else {
                        for (StatisticsObject obiect : colectie.getObiecte()) {
                            writeRow(csvWriter, statistics, colectie, obiect);
                            objectCount++;
                        }
                    }
                }

                Logger.debug("Procesate " + collectionCount + " colecții și " + objectCount + " obiecte");
            }

            csvWriter.flush();
            Logger.success("CSV cu statistici personale generat cu succes: " + outputPath);
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "CsvError",
                    "FileWriteError",
                    "Eroare la scrierea fișierului CSV: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "CsvError",
                    "GenerationFailed",
                    "Eroare la generarea CSV-ului personal: " + e.getMessage(),
                    e
            );
        }
    }

    private static void writeHeader(FileWriter csvWriter) throws IOException {

        String header = "p_id" + CSV_SEPARATOR +
                        "p_username" + CSV_SEPARATOR +
                        "p_email" + CSV_SEPARATOR +
                        "p_dataCrearii" + CSV_SEPARATOR +
                        "p_oraCrearii" + CSV_SEPARATOR +
                        "p_dataActualizarii" + CSV_SEPARATOR +
                        "p_oraActualizarii" + CSV_SEPARATOR +
                        "p_distinctLikes" + CSV_SEPARATOR +
                        "p_distinctViews" + CSV_SEPARATOR +
                        "p_totalLikes" + CSV_SEPARATOR +
                        "p_totalViews" + CSV_SEPARATOR +
                        "p_value" + CSV_SEPARATOR +
                        "p_mostLikedObject" + CSV_SEPARATOR +
                        "p_lessLikedObject" + CSV_SEPARATOR +
                        "p_mostViewedObject" + CSV_SEPARATOR +
                        "p_lessViewedObject" + CSV_SEPARATOR +
                        "p_mostLikedCollection" + CSV_SEPARATOR +
                        "p_lessLikedCollection" + CSV_SEPARATOR +
                        "p_mostViewedCollection" + CSV_SEPARATOR +
                        "p_lessViewedCollection" + CSV_SEPARATOR +
                        "p_mostValuableCollection" + CSV_SEPARATOR +
                        "p_lessValuableCollection" + CSV_SEPARATOR +
                        "p_mostValuableObject" + CSV_SEPARATOR +
                        "p_lessValuableObject" + CSV_SEPARATOR +

                        // Collection headers
                        "c_nume" + CSV_SEPARATOR +
                        "c_tipColectie" + CSV_SEPARATOR +
                        "c_vizibilitate" + CSV_SEPARATOR +
                        "c_dataCrearii" + CSV_SEPARATOR +
                        "c_oraCrearii" + CSV_SEPARATOR +
                        "c_distinctLikes" + CSV_SEPARATOR +
                        "c_distinctViews" + CSV_SEPARATOR +
                        "c_totalLikes" + CSV_SEPARATOR +
                        "c_totalViews" + CSV_SEPARATOR +
                        "c_totalValuables" + CSV_SEPARATOR +
                        "c_mostLikedObject" + CSV_SEPARATOR +
                        "c_lessLikedObject" + CSV_SEPARATOR +
                        "c_mostViewedObject" + CSV_SEPARATOR +
                        "c_lessViewedObject" + CSV_SEPARATOR +
                        "c_mostValuableObject" + CSV_SEPARATOR +
                        "c_lessValuableObject" + CSV_SEPARATOR +

                        // Object headers
                        "o_nume" + CSV_SEPARATOR +
                        "o_descriere" + CSV_SEPARATOR +
                        "o_vizibilitate" + CSV_SEPARATOR +
                        "o_dataCrearii" + CSV_SEPARATOR +
                        "o_oraCrearii" + CSV_SEPARATOR +
                        "o_dataActualizarii" + CSV_SEPARATOR +
                        "o_oraActualizarii" + CSV_SEPARATOR +
                        "o_distinctLikes" + CSV_SEPARATOR +
                        "o_distinctViews" + CSV_SEPARATOR +
                        "o_totalLikes" + CSV_SEPARATOR +
                        "o_totalViews" + CSV_SEPARATOR +
                        "o_value" + CSV_SEPARATOR +
                        "o_mostLiker" + CSV_SEPARATOR +
                        "o_mostViewer" + CSV_SEPARATOR +
                        "o_lastLiker" + CSV_SEPARATOR +
                        "o_lastViewDate" + CSV_SEPARATOR +
                        "o_lastViewTime" + CSV_SEPARATOR +
                        "o_lastViewer" + CSV_SEPARATOR +
                        "o_lastLikedDate" + CSV_SEPARATOR +
                        "o_lastLikedTime" + CSV_SEPARATOR +

                        // Atribute obiect
                        "o_material" + CSV_SEPARATOR +
                        "o_valoare" + CSV_SEPARATOR +
                        "o_greutate" + CSV_SEPARATOR +
                        "o_numeArtist" + CSV_SEPARATOR +
                        "o_tematica" + CSV_SEPARATOR +
                        "o_gen" + CSV_SEPARATOR +
                        "o_casaDiscuri" + CSV_SEPARATOR +
                        "o_tara" + CSV_SEPARATOR +
                        "o_an" + CSV_SEPARATOR +
                        "o_stare" + CSV_SEPARATOR +
                        "o_raritate" + CSV_SEPARATOR +
                        "o_pretAchizitie";

        csvWriter.append(header).append("\n");
    }

    private static void writeRowWithoutCollectionAndObject(FileWriter csvWriter, StatisticsProfile profile) throws IOException {
        StringBuilder row = new StringBuilder();

        appendProfileData(row, profile);

        appendEmptyFields(row, COLLECTION_FIELD_COUNT);

        appendEmptyFields(row, OBJECT_FIELD_COUNT + ATTRIBUTE_FIELD_COUNT - 1);

        csvWriter.append(row.toString()).append("\n");
    }

    private static void writeRowWithoutObject(FileWriter csvWriter, StatisticsProfile profile, StatisticsCollection colectie) throws IOException {
        StringBuilder row = new StringBuilder();

        appendProfileData(row, profile);

        appendCollectionData(row, colectie);

        appendEmptyFields(row, OBJECT_FIELD_COUNT + ATTRIBUTE_FIELD_COUNT - 1);

        csvWriter.append(row.toString()).append("\n");
    }

    private static void writeRow(FileWriter csvWriter, StatisticsProfile profile, StatisticsCollection colectie, StatisticsObject obiect) throws IOException {
        StringBuilder row = new StringBuilder();

        appendProfileData(row, profile);

        appendCollectionData(row, colectie);

        appendObjectData(row, obiect);

        appendObjectAttributes(row, obiect);

        csvWriter.append(row.toString()).append("\n");
    }

    private static void appendProfileData(StringBuilder row, StatisticsProfile profile) {
        row.append(escapeField(profile.getId() != null ? profile.getId().toString() : "")).append(CSV_SEPARATOR);
        row.append(escapeField(profile.getUsername())).append(CSV_SEPARATOR);
        row.append(escapeField(profile.getEmail())).append(CSV_SEPARATOR);

        row.append(formatDate(profile.getDataCrearii())).append(CSV_SEPARATOR);
        row.append(formatTime(profile.getOraCrearii())).append(CSV_SEPARATOR);
        row.append(formatDate(profile.getDataActualizarii())).append(CSV_SEPARATOR);
        row.append(formatTime(profile.getOraActualizarii())).append(CSV_SEPARATOR);

        row.append(formatLong(profile.getDistinctLikes())).append(CSV_SEPARATOR);
        row.append(formatLong(profile.getDistinctViews())).append(CSV_SEPARATOR);
        row.append(formatLong(profile.getTotalLikes())).append(CSV_SEPARATOR);
        row.append(formatLong(profile.getTotalViews())).append(CSV_SEPARATOR);
        row.append(formatDouble(profile.getValue())).append(CSV_SEPARATOR);

        row.append(mapToString(profile.getMostLikedObject())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getLessLikedObject())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getMostViewedObject())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getLessViewedObject())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getMostLikedCollection())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getLessLikedCollection())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getMostViewedCollection())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getLessViewedCollection())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getMostValuableCollection())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getLessValuableCollection())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getMostValuableObject())).append(CSV_SEPARATOR);
        row.append(mapToString(profile.getLessValuableObject())).append(CSV_SEPARATOR);
    }

    private static void appendCollectionData(StringBuilder row, StatisticsCollection colectie) {
        row.append(escapeField(colectie.getNume())).append(CSV_SEPARATOR);
        row.append(escapeField(colectie.getTipColectie())).append(CSV_SEPARATOR);
        row.append(formatBoolean(colectie.getVizibilitate())).append(CSV_SEPARATOR);

        row.append(formatDate(colectie.getDataCrearii())).append(CSV_SEPARATOR);
        row.append(formatTime(colectie.getOraCrearii())).append(CSV_SEPARATOR);

        row.append(formatLong(colectie.getDistinctLikes())).append(CSV_SEPARATOR);
        row.append(formatLong(colectie.getDistinctViews())).append(CSV_SEPARATOR);
        row.append(formatLong(colectie.getTotalLikes())).append(CSV_SEPARATOR);
        row.append(formatLong(colectie.getTotalViews())).append(CSV_SEPARATOR);
        row.append(formatDouble(colectie.getTotalValuables())).append(CSV_SEPARATOR);

        row.append(mapToString(colectie.getMostLikedObject())).append(CSV_SEPARATOR);
        row.append(mapToString(colectie.getLessLikedObject())).append(CSV_SEPARATOR);
        row.append(mapToString(colectie.getMostViewedObject())).append(CSV_SEPARATOR);
        row.append(mapToString(colectie.getLessViewedObject())).append(CSV_SEPARATOR);
        row.append(mapToString(colectie.getMostValuableObject())).append(CSV_SEPARATOR);
        row.append(mapToString(colectie.getLessValuableObject())).append(CSV_SEPARATOR);
    }

    private static void appendObjectData(StringBuilder row, StatisticsObject obiect) {
        row.append(escapeField(obiect.getNume())).append(CSV_SEPARATOR);
        row.append(escapeField(obiect.getDescriere())).append(CSV_SEPARATOR);
        row.append(formatBoolean(obiect.getVizibilitate())).append(CSV_SEPARATOR);

        row.append(formatDate(obiect.getDataCrearii())).append(CSV_SEPARATOR);
        row.append(formatTime(obiect.getOraCrearii())).append(CSV_SEPARATOR);
        row.append(formatDate(obiect.getDataActualizarii())).append(CSV_SEPARATOR);
        row.append(formatTime(obiect.getOraActualizarii())).append(CSV_SEPARATOR);

        row.append(formatLong(obiect.getDistinctLikes())).append(CSV_SEPARATOR);
        row.append(formatLong(obiect.getDistinctViews())).append(CSV_SEPARATOR);
        row.append(formatLong(obiect.getTotalLikes())).append(CSV_SEPARATOR);
        row.append(formatLong(obiect.getTotalViews())).append(CSV_SEPARATOR);
        row.append(formatDouble(obiect.getValue())).append(CSV_SEPARATOR);

        row.append(escapeField(obiect.getMostLiker())).append(CSV_SEPARATOR);
        row.append(escapeField(obiect.getMostViewer())).append(CSV_SEPARATOR);
        row.append(escapeField(obiect.getLastLiker())).append(CSV_SEPARATOR);

        row.append(formatDate(obiect.getLastViewDate())).append(CSV_SEPARATOR);
        row.append(formatTime(obiect.getLastViewTime())).append(CSV_SEPARATOR);
        row.append(escapeField(obiect.getLastViewer())).append(CSV_SEPARATOR);
        row.append(formatDate(obiect.getLastLikedDate())).append(CSV_SEPARATOR);
        row.append(formatTime(obiect.getLastLikedTime())).append(CSV_SEPARATOR);
    }

    private static void appendObjectAttributes(StringBuilder row, StatisticsObject obiect) {
        Map<String, Boolean> visibleFields = obiect.getVisibleFields();

        if (obiect.getAtribute() != null && visibleFields != null) {
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("material", false)) ?
                    escapeField(obiect.getAtribute().getMaterial()) : "").append(CSV_SEPARATOR);

            // Valoare
            if (Boolean.TRUE.equals(visibleFields.getOrDefault("valoare", false))) {
                obiect.getAtribute().getValoare();
            }
            row.append(CSV_SEPARATOR);

            // Greutate
            if (Boolean.TRUE.equals(visibleFields.getOrDefault("greutate", false))) {
                obiect.getAtribute().getGreutate();
            }
            row.append(CSV_SEPARATOR);

            // Nume artist
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("nume_artist", false)) ?
                    escapeField(obiect.getAtribute().getNumeArtist()) : "").append(CSV_SEPARATOR);

            // Tematica
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("tematica", false)) ?
                    escapeField(obiect.getAtribute().getTematica()) : "").append(CSV_SEPARATOR);

            // Gen
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("gen", false)) ?
                    escapeField(obiect.getAtribute().getGen()) : "").append(CSV_SEPARATOR);

            // Casa discuri
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("casa_discuri", false)) ?
                    escapeField(obiect.getAtribute().getCasaDiscuri()) : "").append(CSV_SEPARATOR);

            // Tara
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("tara", false)) ?
                    escapeField(obiect.getAtribute().getTara()) : "").append(CSV_SEPARATOR);

            // An
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("an", false)) && obiect.getAtribute().getAn() != null ?
                    obiect.getAtribute().getAn().getYear() : "").append(CSV_SEPARATOR);

            // Stare
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("stare", false)) ?
                    escapeField(obiect.getAtribute().getStare()) : "").append(CSV_SEPARATOR);

            // Raritate
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("raritate", false)) ?
                    escapeField(obiect.getAtribute().getRaritate()) : "").append(CSV_SEPARATOR);

            // Pret achizitte
            row.append(Boolean.TRUE.equals(visibleFields.getOrDefault("pret_achizitie", false)) && obiect.getAtribute().getPretAchizitie() != null ?
                    formatDouble(obiect.getAtribute().getPretAchizitie()) : "");
        } else {
            appendEmptyFields(row, ATTRIBUTE_FIELD_COUNT - 1);
        }
    }

    private static void appendEmptyFields(StringBuilder row, int count) {
        row.append(CSV_SEPARATOR.repeat(Math.max(0, count)));
    }

    private static String formatDate(LocalDate date) {
        return date != null ? escapeField(date.format(DATE_FORMATTER)) : "";
    }

    private static String formatTime(LocalTime time) {
        return time != null ? escapeField(time.format(TIME_FORMATTER)) : "";
    }

    private static String formatBoolean(Boolean value) {
        if (value == null) return "";
        return value ? "Public" : "Privat";
    }

    private static String formatLong(Long value) {
        return value != null ? value.toString() : "";
    }

    private static String formatDouble(Double value) {
        return value != null ? value.toString() : "";
    }

    private static String escapeField(String field) {
        if (field == null) {
            return "";
        }

        if (field.contains(CSV_SEPARATOR) || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private static String mapToString(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!result.isEmpty()) {
                result.append("; ");
            }

            String key = escapeField(entry.getKey());
            String value = entry.getValue() != null ? entry.getValue().toString() : "";

            result.append(key).append(": ").append(value);
        }

        return escapeField(result.toString());
    }
}