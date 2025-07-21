package backend.api.util.files;

import backend.api.config.applicationConfig.Properties;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CsvGeneralStatistics {

    private static final String CSV_SEPARATOR = ",";
    private static final int TOP_ITEMS_COUNT = 5;

    // Categorii de top-uri
    private static final String CAT_GENERAL = "General Statistics";
    private static final String CAT_OBJ_LIKES = "Top Objects by Likes";
    private static final String CAT_OBJ_VIEWS = "Top Objects by Views";
    private static final String CAT_OBJ_VALUE = "Top Objects by Value";
    private static final String CAT_COL_LIKES = "Top Collections by Likes";
    private static final String CAT_COL_VIEWS = "Top Collections by Views";
    private static final String CAT_COL_VALUE = "Top Collections by Value";


    public static void generateStatisticsCsv(String outputPath) throws CustomException {
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new Exception500.InternalServerErrorException(
                    "CsvError",
                    "InvalidOutputPath",
                    "Cale fișier CSV invalidă"
            );
        }

        Logger.info("Generare CSV cu statistici generale: " + outputPath);

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

            // Parseaza datele din XML
            Map<String, String> data = parseXmlStatistics();
            Logger.debug("Date statistici parsate din XML, " + data.size() + " elemente");

            List<String> headers = createHeadersList();
            csvWriter.append(String.join(CSV_SEPARATOR, headers)).append("\n");
            Logger.debug("Headerele CSV scrise: " + headers.size() + " coloane");

            // Scrie randurile cu statistici
            writeStatisticsRows(csvWriter, data, headers);
            Logger.debug("Toate rândurile CSV au fost scrise");

            csvWriter.flush();
            Logger.success("CSV cu statistici generale generat cu succes: " + outputPath);
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
                    "Eroare la generarea CSV-ului: " + e.getMessage(),
                    e
            );
        }
    }

    private static List<String> createHeadersList() {
        List<String> headers = new ArrayList<>();


        // Statistici generale
        headers.add("total_collections");
        headers.add("total_objects");
        headers.add("total_value");
        headers.add("last_month_percent");

        // Distributie tipuri
        headers.add("percent_monede");
        headers.add("percent_tablouri");
        headers.add("percent_timbre");
        headers.add("percent_vinil");
        headers.add("percent_custom");

        // Informatii despre elemente din topuri
        headers.add("category");
        headers.add("position");
        headers.add("id");
        headers.add("name");
        headers.add("value");
        headers.add("visibility");

        return headers;
    }

    private static void writeStatisticsRows(FileWriter csvWriter, Map<String, String> data, List<String> headers) throws IOException {

        // Scrie randul cu statistici generale
        writeGeneralStatisticsRow(csvWriter, data, headers);

        // Scrie randurile cu top-uri de obiecte si colecții
        for (int i = 1; i <= TOP_ITEMS_COUNT; i++) {
            writeTopItemRow(csvWriter, data, headers, CAT_OBJ_LIKES, "topLikeObj", i);
            writeTopItemRow(csvWriter, data, headers, CAT_OBJ_VIEWS, "topViewObj", i);
            writeTopItemRow(csvWriter, data, headers, CAT_OBJ_VALUE, "topPriceObj", i);
            writeTopItemRow(csvWriter, data, headers, CAT_COL_LIKES, "topLikeCol", i);
            writeTopItemRow(csvWriter, data, headers, CAT_COL_VIEWS, "topViewCol", i);
            writeTopItemRow(csvWriter, data, headers, CAT_COL_VALUE, "topPriceCol", i);
        }
    }

    private static void writeGeneralStatisticsRow(FileWriter csvWriter, Map<String, String> data, List<String> headers) throws IOException {
        Map<String, String> rowData = new HashMap<>();

        // Adauga statisticile generale
        rowData.put("total_collections", data.getOrDefault("totalCollections", ""));
        rowData.put("total_objects", data.getOrDefault("totalObjects", ""));
        rowData.put("total_value", data.getOrDefault("totalValue", ""));
        rowData.put("last_month_percent", data.getOrDefault("lastMonth", ""));

        // Adauga distributia tipurilor
        rowData.put("percent_monede", data.getOrDefault("procentMonede", ""));
        rowData.put("percent_tablouri", data.getOrDefault("procentTablouri", ""));
        rowData.put("percent_timbre", data.getOrDefault("procentTimbre", ""));
        rowData.put("percent_vinil", data.getOrDefault("procentVinil", ""));
        rowData.put("percent_custom", data.getOrDefault("procentCustom", ""));

        // Adauga categoria
        rowData.put("category", CAT_GENERAL);

        // Scrie randul
        writeRowFromMap(csvWriter, rowData, headers);
    }

    private static void writeTopItemRow(FileWriter csvWriter, Map<String, String> data, List<String> headers,
                                        String category, String prefix, int position) throws IOException {
        Map<String, String> rowData = new HashMap<>();

        // Adauga informatiile despre element
        rowData.put("category", category);
        rowData.put("position", String.valueOf(position));
        rowData.put("id", data.getOrDefault(prefix + "Id_" + position, ""));
        rowData.put("name", data.getOrDefault(prefix + "Name_" + position, ""));
        rowData.put("value", data.getOrDefault(prefix + "Value_" + position, ""));
        rowData.put("visibility", convertVisibility(data.getOrDefault(prefix + "Visible_" + position, "")));

        // Scrie randul
        writeRowFromMap(csvWriter, rowData, headers);
    }



    private static String convertVisibility(String visibility) {
        if (visibility.equals("true")) {
            return "Public";
        } else if (visibility.equals("false")) {
            return "Private";
        }
        return visibility;
    }

    private static void writeRowFromMap(FileWriter csvWriter, Map<String, String> rowData, List<String> headers) throws IOException {
        List<String> values = new ArrayList<>();

        for (String header : headers) {
            values.add(escapeField(rowData.getOrDefault(header, "")));
        }

        csvWriter.append(String.join(CSV_SEPARATOR, values)).append("\n");
    }

    private static Map<String, String> parseXmlStatistics() throws CustomException {
        Map<String, String> data = new HashMap<>();

        String xmlPath = Properties.getPath() + File.separator+"flux" +File.separator+"statistics_rss.xml";
        File xmlFile = new File(xmlPath);

        if (!xmlFile.exists()) {
            Logger.exception("FileNotFoundException");
            Logger.error("Fișierul XML nu a fost găsit la calea: " + xmlPath);
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "XmlNotFound",
                    "Fișierul XML nu a fost găsit la calea: " + xmlPath
            );
        }

        try {
            DocumentBuilderFactory factory;
            factory = DocumentBuilderFactory.newInstance();
            //se activeaza protectia de securitate
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // Previne XXE
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList items = document.getElementsByTagName("item");
            Logger.debug("Număr de elemente item găsite în XML: " + items.getLength());

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);

                // Verifică dacă elementele title și description există
                NodeList titleNodes = item.getElementsByTagName("title");
                NodeList descriptionNodes = item.getElementsByTagName("description");

                if (titleNodes.getLength() == 0 || descriptionNodes.getLength() == 0) {
                    Logger.warning("Element item invalid la index " + i + ": lipsește title sau description");
                    continue;
                }

                String title = titleNodes.item(0).getTextContent();
                String description = descriptionNodes.item(0).getTextContent();

                data.put(title, description);
            }

            return data;
        } catch (Exception e) {
            Logger.exception("XmlParsingException");
            Logger.error("Eroare la parsarea fișierului XML: " + e.getMessage());
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "ParsingFailed",
                    "Eroare la parsarea fișierului XML: " + e.getMessage(),
                    e
            );
        }
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

    public static byte[] generateCsv() throws CustomException {
        Logger.info("Generare CSV cu statistici generale ca array de bytes");

        try {
            String tempDir = Properties.getPath() +  File.separator+"temp";
            String outputPath = tempDir +  File.separator+"statistici_generale.csv";

            Path dirPath = Paths.get(tempDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                Logger.debug("Director temp creat: " + tempDir);
            }

            // Genereaza fisierul CSV
            generateStatisticsCsv(outputPath);


            File csvFile = new File(outputPath);
            if (!csvFile.exists()) {
                throw new Exception500.InternalServerErrorException(
                        "FileError",
                        "CsvNotGenerated",
                        "Fișierul CSV nu a putut fi generat"
                );
            }

            // Citește si returneaza continutul
            byte[] csvBytes = Files.readAllBytes(csvFile.toPath());
            Logger.success("CSV generat și citit cu succes: " + csvBytes.length + " bytes");
            return csvBytes;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "CsvReadError",
                    "Eroare la citirea fișierului CSV generat: " + e.getMessage(),
                    e
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "CsvError",
                    "GenerationFailed",
                    "Eroare neașteptată la generarea CSV-ului: " + e.getMessage(),
                    e
            );
        }
    }
}