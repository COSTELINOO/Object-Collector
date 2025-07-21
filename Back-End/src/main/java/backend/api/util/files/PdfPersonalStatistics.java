package backend.api.util.files;

import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.Obiect;
import backend.api.model.PersonalStatistics.StatisticsCollection;
import backend.api.model.PersonalStatistics.StatisticsObject;
import backend.api.model.PersonalStatistics.StatisticsProfile;
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
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class PdfPersonalStatistics {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static PdfFont fontRegular;
    private static PdfFont fontBold;

    private static final String[] FONT_PATHS = {
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/arialbd.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"
    };

    private static final DeviceRgb COLOR_PROFILE_BASE = new DeviceRgb(204, 217, 255);   // Albastru deschis
    private static final DeviceRgb COLOR_COLLECTION_BASE = new DeviceRgb(255, 204, 217); // Rosu deschis
    private static final DeviceRgb COLOR_OBJECT_BASE = new DeviceRgb(204, 255, 217);     // Verde deschis

    private static final float TITLE_FONT_SIZE = 24;
    private static final float PROFILE_HEADER_FONT_SIZE = 18;
    private static final float COLLECTION_HEADER_FONT_SIZE = 16;
    private static final float OBJECT_HEADER_FONT_SIZE = 14;
    private static final float SECTION_TITLE_FONT_SIZE_LARGE = 16;
    private static final float SECTION_TITLE_FONT_SIZE_MEDIUM = 14;
    private static final float SECTION_TITLE_FONT_SIZE_SMALL = 12;
    private static final float REGULAR_TEXT_FONT_SIZE = 11;
    private static final float REMINDER_FONT_SIZE = 8;

    public static void generateStatisticsPdf(StatisticsProfile statistics, String outputPath) throws CustomException {
        if (statistics == null) {
            throw new Exception400.BadRequestException(
                    "PdfError",
                    "NullStatistics",
                    "Obiectul cu statistici este null"
            );
        }

        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "PdfError",
                    "InvalidOutputPath",
                    "Cale fișier PDF invalidă"
            );
        }

        Logger.info("Generare PDF cu statistici personale pentru utilizatorul ID: " + statistics.getId() + ", cale: " + outputPath);

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

            buildPdfDocument(document, statistics);
            Logger.debug("Document PDF construit");

            document.close();
            Logger.success("PDF cu statistici personale generat cu succes: " + outputPath);
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "PdfError",
                    "FileWriteError",
                    "Eroare la scrierea fișierului PDF: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "PdfError",
                    "GenerationFailed",
                    "Eroare la generarea PDF-ului personal: " + e.getMessage(),
                    e
            );
        }
    }

    private static void initializeFonts() throws IOException {
        boolean fontsLoaded = false;

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
                Logger.debug("Fonturi standard Helvetica încărcate");
            } catch (Exception e) {
                Logger.warning("Nu s-au putut încărca fonturile Helvetica: " + e.getMessage());

                fontRegular = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN, PdfEncodings.CP1250);
                fontBold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD, PdfEncodings.CP1250);
            }
        }
    }

    private static void buildPdfDocument(Document document, StatisticsProfile statistics) {
        addTitle(document);
        document.add(new Paragraph("\n"));

        addProfileSection(document, statistics);

        addCollectionsSection(document, statistics);
    }

    private static void addProfileSection(Document document, StatisticsProfile statistics)   {
        addProfileHeaderBox(document);

        addSectionTitle(document, "Informații generale", SECTION_TITLE_FONT_SIZE_LARGE);
        addProfileGeneralInfo(document, statistics);
        document.add(new Paragraph("\n"));

        addSectionTitle(document, "Statistici generale", SECTION_TITLE_FONT_SIZE_LARGE);
        addProfileStatistics(document, statistics);
        document.add(new Paragraph("\n"));

        addSectionTitle(document, "Top obiecte", SECTION_TITLE_FONT_SIZE_LARGE);
        addTopObjectsSection(document, statistics);
        document.add(new Paragraph("\n"));

        addSectionTitle(document, "Top colecții", SECTION_TITLE_FONT_SIZE_LARGE);
        addTopCollectionsSection(document, statistics);
        document.add(new Paragraph("\n"));
    }

    private static void addProfileGeneralInfo(Document document, StatisticsProfile statistics) {
        addLabeledValue(document, "ID-ul contului", statistics.getId() != null ? statistics.getId().toString() : "");
        addLabeledValue(document, "Nume utilizator", statistics.getUsername());
        addLabeledValue(document, "Email", statistics.getEmail());

        if (statistics.getDataCrearii() != null) {
            addLabeledValue(document, "Crearea contului", "");
            addLabeledValue(document, "     → Data", statistics.getDataCrearii().format(DATE_FORMATTER));
            addLabeledValue(document, "     → Ora", statistics.getOraCrearii().format(TIME_FORMATTER));
        }

        if (statistics.getDataActualizarii() != null) {
            addLabeledValue(document, "Ultima actualizare", "");
            addLabeledValue(document, "     → Data", statistics.getDataActualizarii().format(DATE_FORMATTER));
            addLabeledValue(document, "     → Ora", statistics.getOraActualizarii().format(TIME_FORMATTER));
        }
    }

    private static void addProfileStatistics(Document document, StatisticsProfile statistics) {
        addLabeledValue(document, "Aprecieri (like-uri)", "");
        addLabeledValue(document, "     → Număr total", statistics.getTotalLikes().toString());
        addLabeledValue(document, "     → Utilizatori unici", statistics.getDistinctLikes().toString());

        addLabeledValue(document, "Vizualizări", "");
        addLabeledValue(document, "     → Număr total", statistics.getTotalViews().toString());
        addLabeledValue(document, "     → Utilizatori unici", statistics.getDistinctViews().toString());

        addLabeledValue(document, "Valoare totală a colecțiilor", formatValue(statistics.getValue()));
    }

    private static void addTopObjectsSection(Document document, StatisticsProfile statistics) {
        if (!statistics.getMostLikedObject().isEmpty()) {
            Map.Entry<String, Long> mostLiked = statistics.getMostLikedObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai apreciat obiect",
                    mostLiked.getKey() + " (" + mostLiked.getValue() + " aprecieri)");
        }

        if (!statistics.getLessLikedObject().isEmpty()) {
            Map.Entry<String, Long> lessLiked = statistics.getLessLikedObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai puțin apreciat obiect",
                    lessLiked.getKey() + " (" + lessLiked.getValue() + " aprecieri)");
        }

        if (!statistics.getMostViewedObject().isEmpty()) {
            Map.Entry<String, Long> mostViewed = statistics.getMostViewedObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai vizualizat obiect",
                    mostViewed.getKey() + " (" + mostViewed.getValue() + " vizualizări)");
        }

        if (!statistics.getLessViewedObject().isEmpty()) {
            Map.Entry<String, Long> lessViewed = statistics.getLessViewedObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai puțin vizualizat obiect",
                    lessViewed.getKey() + " (" + lessViewed.getValue() + " vizualizări)");
        }

        if (!statistics.getMostValuableObject().isEmpty()) {
            Map.Entry<String, Double> mostValuable = statistics.getMostValuableObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai valoros obiect",
                    mostValuable.getKey() + " (" + formatValue(mostValuable.getValue()) + ")");
        }

        if (!statistics.getLessValuableObject().isEmpty()) {
            Map.Entry<String, Double> lessValuable = statistics.getLessValuableObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai puțin valoros obiect",
                    lessValuable.getKey() + " (" + formatValue(lessValuable.getValue()) + ")");
        }
    }

    private static void addTopCollectionsSection(Document document, StatisticsProfile statistics) {
        if (!statistics.getMostLikedCollection().isEmpty()) {
            Map.Entry<String, Long> mostLiked = statistics.getMostLikedCollection().entrySet().iterator().next();
            addLabeledValue(document, "Cea mai apreciată colecție",
                    mostLiked.getKey() + " (" + mostLiked.getValue() + " aprecieri)");
        }

        if (!statistics.getLessLikedCollection().isEmpty()) {
            Map.Entry<String, Long> lessLiked = statistics.getLessLikedCollection().entrySet().iterator().next();
            addLabeledValue(document, "Cea mai puțin apreciată colecție",
                    lessLiked.getKey() + " (" + lessLiked.getValue() + " aprecieri)");
        }

        if (!statistics.getMostViewedCollection().isEmpty()) {
            Map.Entry<String, Long> mostViewed = statistics.getMostViewedCollection().entrySet().iterator().next();
            addLabeledValue(document, "Cea mai vizualizată colecție",
                    mostViewed.getKey() + " (" + mostViewed.getValue() + " vizualizări)");
        }

        if (!statistics.getLessViewedCollection().isEmpty()) {
            Map.Entry<String, Long> lessViewed = statistics.getLessViewedCollection().entrySet().iterator().next();
            addLabeledValue(document, "Cea mai puțin vizualizată colecție",
                    lessViewed.getKey() + " (" + lessViewed.getValue() + " vizualizări)");
        }

        if (!statistics.getMostValuableCollection().isEmpty()) {
            Map.Entry<String, Double> mostValuable = statistics.getMostValuableCollection().entrySet().iterator().next();
            addLabeledValue(document, "Cea mai valoroasă colecție",
                    mostValuable.getKey() + " (" + formatValue(mostValuable.getValue()) + ")");
        }

        if (!statistics.getLessValuableCollection().isEmpty()) {
            Map.Entry<String, Double> lessValuable = statistics.getLessValuableCollection().entrySet().iterator().next();
            addLabeledValue(document, "Cea mai puțin valoroasă colecție",
                    lessValuable.getKey() + " (" + formatValue(lessValuable.getValue()) + ")");
        }
    }

    private static void addCollectionsSection(Document document, StatisticsProfile statistics)   {
        if (statistics.getColectii() == null || statistics.getColectii().isEmpty()) {
            Logger.debug("Nu există colecții pentru utilizatorul cu ID: " + statistics.getId());
            return;
        }


        for (int i = 0; i < statistics.getColectii().size(); i++) {
            StatisticsCollection colectie = statistics.getColectii().get(i);

            addCollectionHeaderBox(document, colectie.getNume());

            addSectionTitle(document, "Informații generale", SECTION_TITLE_FONT_SIZE_MEDIUM);
            addCollectionGeneralInfo(document, colectie);
            document.add(new Paragraph("\n"));

            addSectionTitle(document, "Statistici colecție", SECTION_TITLE_FONT_SIZE_MEDIUM);
            addCollectionStatistics(document, colectie);
            document.add(new Paragraph("\n"));

            addSectionTitle(document, "Top obiecte din colecție", SECTION_TITLE_FONT_SIZE_MEDIUM);
            addCollectionTopObjectsSection(document, colectie);
            document.add(new Paragraph("\n"));

            addObjectsFromCollection(document, colectie);

            if (i < statistics.getColectii().size() - 1) {
                document.add(new Paragraph("\n\n"));
            }
        }
    }

    private static void addCollectionGeneralInfo(Document document, StatisticsCollection colectie) {
        addLabeledValue(document, "Tip colecție", colectie.getTipColectie());
        addLabeledValue(document, "Vizibilitate", Boolean.TRUE.equals(colectie.getVizibilitate()) ? "Publică" : "Privată");

        if (colectie.getDataCrearii() != null) {
            addLabeledValue(document, "Crearea colecției", "");
            addLabeledValue(document, "     → Data", colectie.getDataCrearii().format(DATE_FORMATTER));
            addLabeledValue(document, "     → Ora", colectie.getOraCrearii().format(TIME_FORMATTER));
        }
    }

    private static void addCollectionStatistics(Document document, StatisticsCollection colectie) {
        addLabeledValue(document, "Aprecieri (like-uri)", "");
        addLabeledValue(document, "     → Număr total", colectie.getTotalLikes().toString());
        addLabeledValue(document, "     → Utilizatori unici", colectie.getDistinctLikes().toString());

        addLabeledValue(document, "Vizualizări", "");
        addLabeledValue(document, "     → Număr total", colectie.getTotalViews().toString());
        addLabeledValue(document, "     → Utilizatori unici", colectie.getDistinctViews().toString());

        addLabeledValue(document, "Valoare totală", formatValue(colectie.getTotalValuables()));
    }

    private static void addCollectionTopObjectsSection(Document document, StatisticsCollection colectie) {
        if (!colectie.getMostLikedObject().isEmpty()) {
            Map.Entry<String, Long> mostLiked = colectie.getMostLikedObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai apreciat obiect",
                    mostLiked.getKey() + " (" + mostLiked.getValue() + " aprecieri)");
        }

        if (!colectie.getLessLikedObject().isEmpty()) {
            Map.Entry<String, Long> lessLiked = colectie.getLessLikedObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai puțin apreciat obiect",
                    lessLiked.getKey() + " (" + lessLiked.getValue() + " aprecieri)");
        }

        if (!colectie.getMostViewedObject().isEmpty()) {
            Map.Entry<String, Long> mostViewed = colectie.getMostViewedObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai vizualizat obiect",
                    mostViewed.getKey() + " (" + mostViewed.getValue() + " vizualizări)");
        }

        if (!colectie.getLessViewedObject().isEmpty()) {
            Map.Entry<String, Long> lessViewed = colectie.getLessViewedObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai puțin vizualizat obiect",
                    lessViewed.getKey() + " (" + lessViewed.getValue() + " vizualizări)");
        }

        if (!colectie.getMostValuableObject().isEmpty()) {
            Map.Entry<String, Double> mostValuable = colectie.getMostValuableObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai valoros obiect",
                    mostValuable.getKey() + " (" + formatValue(mostValuable.getValue()) + ")");
        }

        if (!colectie.getLessValuableObject().isEmpty()) {
            Map.Entry<String, Double> lessValuable = colectie.getLessValuableObject().entrySet().iterator().next();
            addLabeledValue(document, "Cel mai puțin valoros obiect",
                    lessValuable.getKey() + " (" + formatValue(lessValuable.getValue()) + ")");
        }
    }

    private static void addObjectsFromCollection(Document document, StatisticsCollection colectie)   {
        if (colectie.getObiecte() == null || colectie.getObiecte().isEmpty()) {
            Logger.debug("Nu există obiecte în colecția: " + colectie.getNume());
            return;
        }


        for (StatisticsObject obiect : colectie.getObiecte()) {
            addObjectHeaderBox(document, obiect.getNume());

            addCollectionReminderBox(document, colectie.getNume());

            addSectionTitle(document, "Informații generale", SECTION_TITLE_FONT_SIZE_SMALL);
            addObjectGeneralInfo(document, obiect);
            document.add(new Paragraph("\n"));

            addSectionTitle(document, "Statistici obiect", SECTION_TITLE_FONT_SIZE_SMALL);
            addObjectStatistics(document, obiect);
            document.add(new Paragraph("\n"));

            addSectionTitle(document, "Atribute obiect", SECTION_TITLE_FONT_SIZE_SMALL);
            addObjectAttributes(document, obiect);
            document.add(new Paragraph("\n"));
        }
    }

    private static void addObjectGeneralInfo(Document document, StatisticsObject obiect) {
        addLabeledValue(document, "Descriere", obiect.getDescriere());
        addLabeledValue(document, "Vizibilitate", Boolean.TRUE.equals(obiect.getVizibilitate()) ? "Public" : "Privat");

        if (obiect.getDataCrearii() != null) {
            addLabeledValue(document, "Crearea obiectului", "");
            addLabeledValue(document, "     → Data", obiect.getDataCrearii().format(DATE_FORMATTER));
            addLabeledValue(document, "     → Ora", obiect.getOraCrearii().format(TIME_FORMATTER));
        }

        if (obiect.getDataActualizarii() != null) {
            addLabeledValue(document, "Ultima actualizare", "");
            addLabeledValue(document, "     → Data", obiect.getDataActualizarii().format(DATE_FORMATTER));
            addLabeledValue(document, "     → Ora", obiect.getOraActualizarii().format(TIME_FORMATTER));
        }
    }

    private static void addObjectStatistics(Document document, StatisticsObject obiect) {
        addLabeledValue(document, "Aprecieri (like-uri)", "");
        addLabeledValue(document, "     → Număr total", obiect.getTotalLikes().toString());
        addLabeledValue(document, "     → Utilizatori unici", obiect.getDistinctLikes().toString());

        addLabeledValue(document, "Vizualizări", "");
        addLabeledValue(document, "     → Număr total", obiect.getTotalViews().toString());
        addLabeledValue(document, "     → Utilizatori unici", obiect.getDistinctViews().toString());

        addLabeledValue(document, "Valoare", formatValue(obiect.getValue()));

        if (obiect.getMostLiker() != null && !obiect.getMostLiker().isEmpty()) {
            addLabeledValue(document, "Cel mai frecvent utilizator care a apreciat", obiect.getMostLiker());
        }

        if (obiect.getMostViewer() != null && !obiect.getMostViewer().isEmpty()) {
            addLabeledValue(document, "Cel mai frecvent vizitator", obiect.getMostViewer());
        }

        if (obiect.getLastLiker() != null && !obiect.getLastLiker().isEmpty() && obiect.getLastLikedDate() != null) {
            addLabeledValue(document, "Ultima apreciere", "");
            addLabeledValue(document, "     → Utilizator", obiect.getLastLiker());
            addLabeledValue(document, "     → Data", obiect.getLastLikedDate().format(DATE_FORMATTER));
            addLabeledValue(document, "     → Ora", obiect.getLastLikedTime().format(TIME_FORMATTER));
        }

        if (obiect.getLastViewer() != null && !obiect.getLastViewer().isEmpty() && obiect.getLastViewDate() != null) {
            addLabeledValue(document, "Ultima vizualizare", "");
            addLabeledValue(document, "     → Utilizator", obiect.getLastViewer());
            addLabeledValue(document, "     → Data", obiect.getLastViewDate().format(DATE_FORMATTER));
            addLabeledValue(document, "     → Ora", obiect.getLastViewTime().format(TIME_FORMATTER));
        }
    }

    private static void addObjectAttributes(Document document, StatisticsObject obiect) {
        Obiect atribute = obiect.getAtribute();
        Map<String, Boolean> visibleFields = obiect.getVisibleFields();

        if (atribute == null || visibleFields == null) {
            addLabeledValue(document, "Nu există atribute disponibile", "");
            return;
        }

        if (Boolean.TRUE.equals(visibleFields.getOrDefault("material", false)) && atribute.getMaterial() != null) {
            addLabeledValue(document, "Material", atribute.getMaterial());
        }

        // Valoare
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("valoare", false))) {
            atribute.getValoare();
        }

        // Greutate
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("greutate", false))) {
            atribute.getGreutate();
        }

        // Artist
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("nume_artist", false)) && atribute.getNumeArtist() != null) {
            addLabeledValue(document, "Artist", atribute.getNumeArtist());
        }

        // Tematică
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("tematica", false)) && atribute.getTematica() != null) {
            addLabeledValue(document, "Tematică", atribute.getTematica());
        }

        // Gen
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("gen", false)) && atribute.getGen() != null) {
            addLabeledValue(document, "Gen", atribute.getGen());
        }

        // Casa de discuri
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("casa_discuri", false)) && atribute.getCasaDiscuri() != null) {
            addLabeledValue(document, "Casa de discuri", atribute.getCasaDiscuri());
        }

        // Țara
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("tara", false)) && atribute.getTara() != null) {
            addLabeledValue(document, "Țara de origine", atribute.getTara());
        }

        // An
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("an", false)) && atribute.getAn() != null) {
            addLabeledValue(document, "An", String.valueOf(atribute.getAn().getYear()));
        }

        // Stare
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("stare", false)) && atribute.getStare() != null) {
            addLabeledValue(document, "Stare", atribute.getStare());
        }

        // Raritate
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("raritate", false)) && atribute.getRaritate() != null) {
            addLabeledValue(document, "Raritate", atribute.getRaritate());
        }

        // Pret achizitie
        if (Boolean.TRUE.equals(visibleFields.getOrDefault("pret_achizitie", false)))atribute.getPretAchizitie();
    }

    private static void addTitle(Document document) {
        Paragraph titleParagraph = new Paragraph("STATISTICILE MELE")
                .setFont(fontBold)
                .setFontSize(TITLE_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        document.add(titleParagraph);
    }

    private static void addProfileHeaderBox(Document document)   {
        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
        float width = pageWidth * 0.5f;

        Table table = new Table(1)
                .setWidth(UnitValue.createPointValue(width))
                .setHorizontalAlignment(HorizontalAlignment.LEFT);

        Paragraph titleParagraph = new Paragraph("Profil")
                .setFont(fontBold)
                .setFontSize(PdfPersonalStatistics.PROFILE_HEADER_FONT_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLACK);

        Cell cell = new Cell()
                .add(titleParagraph)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setBackgroundColor(COLOR_PROFILE_BASE)
                .setPadding(5);

        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private static void addCollectionHeaderBox(Document document, String name)   {
        Table table = new Table(1)
                .setHorizontalAlignment(HorizontalAlignment.LEFT);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));

        Text typeText = new Text("Colecția" + ": ")
                .setFont(fontBold)
                .setFontSize(PdfPersonalStatistics.COLLECTION_HEADER_FONT_SIZE)
                .setFontColor(ColorConstants.BLACK);

        Text nameText = new Text(name)
                .setFont(fontRegular)
                .setFontSize(PdfPersonalStatistics.COLLECTION_HEADER_FONT_SIZE)
                .setFontColor(ColorConstants.BLACK);

        Paragraph titleParagraph = new Paragraph()
                .add(typeText)
                .add(nameText);

        Cell cell = new Cell()
                .add(titleParagraph)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setBackgroundColor(COLOR_COLLECTION_BASE)
                .setPadding(5);

        float estimatedWidth = calculateEstimatedWidth("Colecția", name, PdfPersonalStatistics.COLLECTION_HEADER_FONT_SIZE);
        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth() - 2 * document.getLeftMargin();
        estimatedWidth = Math.min(estimatedWidth, pageWidth);

        table.setWidth(UnitValue.createPointValue(estimatedWidth));
        table.addCell(cell);

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private static void addObjectHeaderBox(Document document, String name)  {
        Table table = new Table(1)
                .setHorizontalAlignment(HorizontalAlignment.LEFT);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));

        Text typeText = new Text("Obiectul" + ": ")
                .setFont(fontBold)
                .setFontSize(PdfPersonalStatistics.OBJECT_HEADER_FONT_SIZE)
                .setFontColor(ColorConstants.BLACK);

        Text nameText = new Text(name)
                .setFont(fontRegular)
                .setFontSize(PdfPersonalStatistics.OBJECT_HEADER_FONT_SIZE)
                .setFontColor(ColorConstants.BLACK);

        Paragraph titleParagraph = new Paragraph()
                .add(typeText)
                .add(nameText);

        Cell cell = new Cell()
                .add(titleParagraph)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setBackgroundColor(COLOR_OBJECT_BASE)
                .setPadding(5);

        float estimatedWidth = calculateEstimatedWidth("Obiectul", name, PdfPersonalStatistics.OBJECT_HEADER_FONT_SIZE);
        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth() - 2 * document.getLeftMargin();
        estimatedWidth = Math.min(estimatedWidth, pageWidth);

        table.setWidth(UnitValue.createPointValue(estimatedWidth));
        table.addCell(cell);

        document.add(table);
    }

    private static void addCollectionReminderBox(Document document, String collectionName)   {
        Table table = new Table(1)
                .setHorizontalAlignment(HorizontalAlignment.LEFT);

        Text labelText = new Text("Colecția: ")
                .setFont(fontBold)
                .setFontSize(PdfPersonalStatistics.REMINDER_FONT_SIZE)
                .setFontColor(ColorConstants.BLACK);

        Text nameText = new Text(collectionName)
                .setFont(fontRegular)
                .setFontSize(PdfPersonalStatistics.REMINDER_FONT_SIZE)
                .setFontColor(ColorConstants.BLACK);

        Paragraph reminderParagraph = new Paragraph()
                .add(labelText)
                .add(nameText);

        Cell cell = new Cell()
                .add(reminderParagraph)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1))
                .setPadding(5);

        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth() - 2 * document.getLeftMargin();
        float width = pageWidth * 0.5f;

        table.setWidth(UnitValue.createPointValue(width));
        table.addCell(cell);

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private static void addSectionTitle(Document document, String title, float fontSize) {
        document.add(new Paragraph(title)
                .setFont(fontBold)
                .setFontSize(fontSize));
    }

    private static void addLabeledValue(Document document, String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Text(label + ": ")
                .setFont(fontBold)
                .setFontSize(REGULAR_TEXT_FONT_SIZE));

        if (value != null && !value.isEmpty()) {
            p.add(new Text(value)
                    .setFont(fontRegular)
                    .setFontSize(REGULAR_TEXT_FONT_SIZE));
        }

        document.add(p);
    }

    private static float calculateEstimatedWidth(String type, String name, float fontSize) {
        return (type.length() + name.length() + 2) * fontSize * 0.6f;
    }

    private static String formatValue(Double value) {
        if (value == null) {
            return "0 RON";
        }
        return value + " RON";
    }

}