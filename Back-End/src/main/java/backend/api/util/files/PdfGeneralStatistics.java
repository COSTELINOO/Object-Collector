package backend.api.util.files;

import backend.api.config.applicationConfig.Properties;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


public class PdfGeneralStatistics {

    // Constante pentru fonturi si culori
    private static PdfFont fontRegular;
    private static PdfFont fontBold;

    private static final DeviceRgb COLOR_HEADER = new DeviceRgb(51, 102, 204);      // Albastru
    private static final DeviceRgb COLOR_GENERAL = new DeviceRgb(204, 217, 255);     // Albastru deschis
    private static final DeviceRgb COLOR_DISTRIBUTION = new DeviceRgb(255, 230, 204);// Portocaliu deschis
    private static final DeviceRgb COLOR_TOP_OBJECTS = new DeviceRgb(204, 255, 217); // Verde deschis
    private static final DeviceRgb COLOR_TOP_COLLECTIONS = new DeviceRgb(255, 204, 217); // Rosu deschis

    private static final String[] FONT_PATHS = {
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/arialbd.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"
    };

    private static final float TITLE_FONT_SIZE = 24;
    private static final float HEADER_FONT_SIZE = 18;
    private static final float SUBHEADER_FONT_SIZE = 14;
    private static final float REGULAR_FONT_SIZE = 10;
    private static final float SMALL_FONT_SIZE = 8;


    private static class StatisticsData {
        Map<String, String> generalStats = new HashMap<>();
        Map<String, String> typesDistribution = new HashMap<>();
        Map<String, Map<Integer, Map<String, String>>> topObjects = new HashMap<>();
        Map<String, Map<Integer, Map<String, String>>> topCollections = new HashMap<>();
        String lastBuildDate = "";
    }

    public static void generateStatisticsPdf(String outputPath) throws CustomException {
        if (outputPath == null || outputPath.trim().isEmpty()) {
            Logger.exception("InvalidParameterException");
            Logger.error("Cale fișier PDF invalidă la generarea statisticilor");
            throw new Exception500.InternalServerErrorException(
                    "PdfError",
                    "InvalidOutputPath",
                    "Cale fișier PDF invalidă"
            );
        }

        Logger.info("Generare PDF cu statistici generale: " + outputPath);

        try {
            Path directory = Paths.get(outputPath).getParent();
            if (directory != null && !Files.exists(directory)) {
                Files.createDirectories(directory);
                Logger.debug("Director creat pentru PDF: " + directory);
            }
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "DirectoryCreationFailed",
                    "Nu s-a putut crea directorul pentru PDF: " + e.getMessage(),
                    e
            );
        }

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);

            initializeFonts();
            Logger.debug("Fonturi inițializate pentru PDF");

            String xmlPath = Properties.getPath() +  File.separator+"flux"+ File.separator+"statistics_rss.xml";
            StatisticsData stats = parseXmlFile(xmlPath);
            Logger.debug("Date statistici parsate din XML: " + xmlPath);

            buildPdfDocument(document, stats);
            Logger.debug("Document PDF construit");

            document.close();
            Logger.success("PDF cu statistici generale generat cu succes: " + outputPath);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "PdfError",
                    "GenerationFailed",
                    "Eroare la generarea PDF-ului cu statistici: " + e.getMessage(),
                    e
            );
        }
    }

    private static void initializeFonts() throws IOException {
        boolean fontsLoaded;
        fontsLoaded = false;

        for (int i = 0; i < FONT_PATHS.length; i += 2) {
            try {
                File regularFile = new File(FONT_PATHS[i]);
                File boldFile = new File(FONT_PATHS[i + 1]);

                if (regularFile.exists() && boldFile.exists()) {
                    fontRegular = PdfFontFactory.createFont(FONT_PATHS[i], PdfEncodings.IDENTITY_H);
                    fontBold = PdfFontFactory.createFont(FONT_PATHS[i + 1], PdfEncodings.IDENTITY_H);
                    fontsLoaded = true;
                    Logger.debug("Fonturi încărcate din sistem: " + FONT_PATHS[i] + ", " + FONT_PATHS[i + 1]);
                    break;
                }
            } catch (Exception e) {
                Logger.warning("Nu s-au putut încărca fonturile din " + FONT_PATHS[i] + ": " + e.getMessage());
            }
        }

        if (!fontsLoaded) {
            try {
                fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.CP1250);
                fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD, PdfEncodings.CP1250);
            } catch (Exception e) {
                Logger.warning("Nu s-au putut încărca fonturile Helvetica: " + e.getMessage());

                fontRegular = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN, PdfEncodings.CP1250);
                fontBold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD, PdfEncodings.CP1250);
            }
        }
    }

    private static void buildPdfDocument(Document document, StatisticsData stats) {
        addTitle(document, "Statistici privind totalul colecțiilor/obiectelor");
        document.add(new Paragraph("\n"));

        addGenerationInfo(document, stats.lastBuildDate);
        document.add(new Paragraph("\n"));

        addHeaderBox(document, "Statistici generale", COLOR_GENERAL, HEADER_FONT_SIZE);
        addGeneralStatistics(document, stats.generalStats);
        document.add(new Paragraph("\n"));

        addHeaderBox(document, "Distribuția tipurilor de obiecte", COLOR_DISTRIBUTION, HEADER_FONT_SIZE);
        addDistributionStatistics(document, stats.typesDistribution);
        document.add(new Paragraph("\n"));

        addHeaderBox(document, "Top obiecte", COLOR_TOP_OBJECTS, HEADER_FONT_SIZE);

        addSubHeaderBox(document, "Cele mai apreciate obiecte", COLOR_TOP_OBJECTS, SUBHEADER_FONT_SIZE);
        addTopItemsSection(document, stats.topObjects.get("like"), "likes");
        document.add(new Paragraph("\n"));

        addSubHeaderBox(document, "Cele mai vizualizate obiecte", COLOR_TOP_OBJECTS, SUBHEADER_FONT_SIZE);
        addTopItemsSection(document, stats.topObjects.get("view"), "views");
        document.add(new Paragraph("\n"));

        addSubHeaderBox(document, "Cele mai valoroase obiecte", COLOR_TOP_OBJECTS, SUBHEADER_FONT_SIZE);
        addTopItemsSection(document, stats.topObjects.get("price"), "RON");
        document.add(new Paragraph("\n"));

        addHeaderBox(document, "Top colecții", COLOR_TOP_COLLECTIONS, HEADER_FONT_SIZE);

        addSubHeaderBox(document, "Cele mai apreciate colecții", COLOR_TOP_COLLECTIONS, SUBHEADER_FONT_SIZE);
        addTopItemsSection(document, stats.topCollections.get("like"), "likes");
        document.add(new Paragraph("\n"));

        addSubHeaderBox(document, "Cele mai vizualizate colecții", COLOR_TOP_COLLECTIONS, SUBHEADER_FONT_SIZE);
        addTopItemsSection(document, stats.topCollections.get("view"), "views");
        document.add(new Paragraph("\n"));

        addSubHeaderBox(document, "Cele mai valoroase colecții", COLOR_TOP_COLLECTIONS, SUBHEADER_FONT_SIZE);
        addTopItemsSection(document, stats.topCollections.get("price"), "RON");

        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("Acest raport a fost generat automat de platforma Object Collector.")
                .setFont(fontRegular)
                .setFontSize(SMALL_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(footer);
    }

    private static StatisticsData parseXmlFile(String filePath) throws CustomException {
        StatisticsData data = new StatisticsData();

        try {
            File xmlFile = new File(filePath);
            if (!xmlFile.exists()) {
                throw new Exception500.InternalServerErrorException(
                        "FileError",
                        "XmlNotFound",
                        "Fișierul XML cu statistici nu există: " + filePath
                );
            }

            data.topObjects.put("like", new HashMap<>());
            data.topObjects.put("view", new HashMap<>());
            data.topObjects.put("price", new HashMap<>());
            data.topCollections.put("like", new HashMap<>());
            data.topCollections.put("view", new HashMap<>());
            data.topCollections.put("price", new HashMap<>());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // Previne XXE
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document xmlDoc = builder.parse(xmlFile);
            xmlDoc.getDocumentElement().normalize();

            NodeList lastBuildDateNodes = xmlDoc.getElementsByTagName("lastBuildDate");
            if (lastBuildDateNodes.getLength() > 0) {
                data.lastBuildDate = lastBuildDateNodes.item(0).getTextContent();
            } else {
                data.lastBuildDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
                Logger.debug("Data ultimei actualizări lipsă, folosind data curentă");
            }

            NodeList items = xmlDoc.getElementsByTagName("item");
            Logger.debug("Număr de elemente item găsite în XML: " + items.getLength());

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);

                NodeList titleNodes = item.getElementsByTagName("title");
                NodeList descriptionNodes = item.getElementsByTagName("description");

                if (titleNodes.getLength() == 0 || descriptionNodes.getLength() == 0) {
                    Logger.warning("Element item invalid la index " + i + ": lipsește title sau description");
                    continue;
                }

                String title = titleNodes.item(0).getTextContent();
                String description = descriptionNodes.item(0).getTextContent();

                categorizeStatisticsData(data, title, description);
            }

            return data;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "XmlError",
                    "ParsingFailed",
                    "Eroare la parsarea fișierului XML cu statistici: " + e.getMessage(),
                    e
            );
        }
    }

    private static void categorizeStatisticsData(StatisticsData data, String title, String description) {
        if (title.equals("totalCollections") || title.equals("totalObjects") ||
                title.equals("totalValue") || title.equals("lastMonth")) {
            data.generalStats.put(title, description);
            Logger.debug("Statistică generală: " + title + " = " + description);
        }
        else if (title.startsWith("procent")) {
            data.typesDistribution.put(title, description);
            Logger.debug("Statistică distribuție: " + title + " = " + description);
        }
        else if (title.startsWith("topLikeObj")) {
            processTopItem(data.topObjects.get("like"), title, description);
        }
        else if (title.startsWith("topViewObj")) {
            processTopItem(data.topObjects.get("view"), title, description);
        }
        else if (title.startsWith("topPriceObj")) {
            processTopItem(data.topObjects.get("price"), title, description);
        }
        else if (title.startsWith("topLikeCol")) {
            processTopItem(data.topCollections.get("like"), title, description);
        }
        else if (title.startsWith("topViewCol")) {
            processTopItem(data.topCollections.get("view"), title, description);
        }
        else if (title.startsWith("topPriceCol")) {
            processTopItem(data.topCollections.get("price"), title, description);
        }
        else {
            Logger.debug("Element necunoscut ignorat: " + title);
        }
    }

    private static void processTopItem(Map<Integer, Map<String, String>> container, String title, String value) {
        try {
            int lastUnderscoreIndex = title.lastIndexOf("_");
            if (lastUnderscoreIndex == -1) {
                Logger.warning("Format invalid pentru top item: " + title);
                return;
            }

            int position = Integer.parseInt(title.substring(lastUnderscoreIndex + 1));

            String infoType = title.substring(0, lastUnderscoreIndex);
            int typeStart = Math.max(infoType.lastIndexOf("Obj"), infoType.lastIndexOf("Col"));
            if (typeStart == -1) {
                Logger.warning("Format invalid pentru top item: " + title);
                return;
            }

            infoType = infoType.substring(typeStart + 3); // +3 pentru a sări peste "Obj" sau "Col"

            if (!container.containsKey(position)) {
                container.put(position, new HashMap<>());
            }

            container.get(position).put(infoType, value);
            Logger.debug("Top item procesat: poziția " + position + ", tip " + infoType + ", valoare " + value);
        } catch (NumberFormatException e) {
            Logger.warning("Format invalid pentru poziția în top item: " + title);
        } catch (Exception e) {
            Logger.warning("Eroare la procesarea top item " + title + ": " + e.getMessage());
        }
    }

    private static void addTitle(Document document, String title) {
        Paragraph titleParagraph = new Paragraph(title)
                .setFont(fontBold)
                .setFontSize(TITLE_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        document.add(titleParagraph);
    }

    private static void addGenerationInfo(Document document, String lastBuildDate) {
        Paragraph dateParagraph = new Paragraph("Raport generat la: " + lastBuildDate)
                .setFont(fontRegular)
                .setFontSize(REGULAR_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(dateParagraph);
    }

    private static void addHeaderBox(Document document, String title, DeviceRgb color, float fontSize) {
        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
        float width = pageWidth * 0.8f;

        Table table = new Table(1)
                .setWidth(UnitValue.createPointValue(width))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        Paragraph titleParagraph = new Paragraph(title)
                .setFont(fontBold)
                .setFontSize(fontSize)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.WHITE);

        Cell cell = new Cell()
                .add(titleParagraph)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setBackgroundColor(COLOR_HEADER)
                .setPadding(5);

        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private static void addSubHeaderBox(Document document, String title, DeviceRgb color, float fontSize) {
        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
        float width = pageWidth * 0.7f;

        Table table = new Table(1)
                .setWidth(UnitValue.createPointValue(width))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        Paragraph titleParagraph = new Paragraph(title)
                .setFont(fontBold)
                .setFontSize(fontSize)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLACK);

        Cell cell = new Cell()
                .add(titleParagraph)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setBackgroundColor(color)
                .setPadding(5);

        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private static void addGeneralStatistics(Document document, Map<String, String> generalStats) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(80))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.addCell(createHeaderCell("Indicator"));
        table.addCell(createHeaderCell("Valoare"));

        table.addCell(createCell("Număr total de colecții"));
        table.addCell(createCell(generalStats.getOrDefault("totalCollections", "N/A")));

        table.addCell(createCell("Număr total de obiecte"));
        table.addCell(createCell(generalStats.getOrDefault("totalObjects", "N/A")));

        table.addCell(createCell("Valoare totală a obiectelor"));
        table.addCell(createCell(generalStats.getOrDefault("totalValue", "N/A") + " RON"));

        table.addCell(createCell("Procent obiecte adăugate în ultima lună"));
        table.addCell(createCell(generalStats.getOrDefault("lastMonth", "N/A") + "%"));

        document.add(table);
    }

    private static void addDistributionStatistics(Document document, Map<String, String> distribution) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                .setWidth(UnitValue.createPercentValue(80))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.addCell(createHeaderCell("Tip obiect"));
        table.addCell(createHeaderCell("Procent"));
        table.addCell(createHeaderCell("Reprezentare grafică"));

        addDistributionRow(table, "Monede", distribution.getOrDefault("procentMonede", "0"));
        addDistributionRow(table, "Tablouri", distribution.getOrDefault("procentTablouri", "0"));
        addDistributionRow(table, "Timbre", distribution.getOrDefault("procentTimbre", "0"));
        addDistributionRow(table, "Viniluri", distribution.getOrDefault("procentVinil", "0"));
        addDistributionRow(table, "Custom", distribution.getOrDefault("procentCustom", "0"));

        document.add(table);
    }

    private static void addDistributionRow(Table table, String type, String percent) {
        table.addCell(createCell(type));
        table.addCell(createCell(percent + "%"));

        Cell graphCell = new Cell();
        float percentValue;

        try {
            percentValue = Float.parseFloat(percent);
        } catch (NumberFormatException e) {
            percentValue = 0f;
            Logger.warning("Valoare procent invalidă pentru " + type + ": " + percent);
        }

        percentValue = Math.max(0, Math.min(100, percentValue));

        Table graphTable = new Table(UnitValue.createPercentArray(new float[]{percentValue, 100 - percentValue}))
                .setWidth(UnitValue.createPercentValue(100));

        if (percentValue > 0) {
            Cell filledCell = new Cell()
                    .setBackgroundColor(COLOR_HEADER)
                    .setBorder(new SolidBorder(ColorConstants.WHITE, 0.5f))
                    .setHeight(15);
            graphTable.addCell(filledCell);
        }

        if (percentValue < 100) {
            Cell emptyCell = new Cell()
                    .setBackgroundColor(ColorConstants.WHITE)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                    .setHeight(15);
            graphTable.addCell(emptyCell);
        }

        graphCell.add(graphTable).setPadding(2);
        table.addCell(graphCell);
    }

    private static void addTopItemsSection(Document document, Map<Integer, Map<String, String>> items, String unit) {
        if (items == null || items.isEmpty()) {
            document.add(new Paragraph("Nu există date disponibile pentru această categorie.")
                    .setFont(fontRegular)
                    .setFontSize(REGULAR_FONT_SIZE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());
            return;
        }

        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 45, 25, 20}))
                .setWidth(UnitValue.createPercentValue(90))
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        table.addCell(createHeaderCell("Poziție"));
        table.addCell(createHeaderCell("Nume"));
        table.addCell(createHeaderCell("Valoare"));
        table.addCell(createHeaderCell("Vizibilitate"));

        List<Integer> positions = new ArrayList<>(items.keySet());
        Collections.sort(positions);

        for (Integer position : positions) {
            Map<String, String> itemData = items.get(position);

            table.addCell(createCell("#" + position));
            table.addCell(createCell(itemData.getOrDefault("Name", "N/A")));

            String valueStr = itemData.getOrDefault("Value", "N/A");
            if (!valueStr.equals("N/A")) {
                valueStr += " " + unit;
            }
            table.addCell(createCell(valueStr));

            boolean isVisible;
            try {
                isVisible = Boolean.parseBoolean(itemData.getOrDefault("Visible", "false"));
            } catch (Exception e) {
                isVisible = false;
                Logger.warning("Valoare de vizibilitate invalidă pentru poziția " + position);
            }
            table.addCell(createCell(isVisible ? "Public" : "Privat"));
        }

        document.add(table);
    }

    private static Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(fontBold)
                        .setFontSize(11)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(COLOR_HEADER)
                .setFontColor(ColorConstants.WHITE)
                .setPadding(5);
    }

    private static Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(fontRegular)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER))
                .setPadding(5);
    }
}