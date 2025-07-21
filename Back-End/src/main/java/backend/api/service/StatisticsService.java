package backend.api.service;

import backend.api.config.applicationConfig.Properties;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.*;
import backend.api.model.Collection;
import backend.api.model.GlobalStatistics.Clasament;
import backend.api.model.GlobalStatistics.GeneralStatistics;
import backend.api.model.PersonalStatistics.StatisticsCollection;
import backend.api.model.PersonalStatistics.StatisticsObject;
import backend.api.model.PersonalStatistics.StatisticsProfile;
import backend.api.repository.*;
import backend.api.util.files.CsvPersonalStatistics;
import backend.api.util.files.XmlGeneralStatistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static backend.api.util.files.PdfPersonalStatistics.generateStatisticsPdf;

public class StatisticsService {
    private static final Locale US_LOCALE = Locale.US;
    private static final DecimalFormat DECIMAL_FORMAT = (DecimalFormat) NumberFormat.getInstance(US_LOCALE);

    static {
        DECIMAL_FORMAT.applyPattern("0.00");
    }

    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final ObjectRepository objectRepository;
    private final CustomCollectionRepository customCollectionRepository;
    private final ObjectLikeRepository objectLikeRepository;
    private final ObjectViewRepository objectViewRepository;
    private final GeneralStatisticsRepository generalStatisticsRepository;

    public StatisticsService() {
        this.userRepository = new UserRepository();
        this.collectionRepository = new CollectionRepository();
        this.objectRepository = new ObjectRepository();
        this.customCollectionRepository = new CustomCollectionRepository();
        this.objectLikeRepository = new ObjectLikeRepository();
        this.objectViewRepository = new ObjectViewRepository();
        this.generalStatisticsRepository = new GeneralStatisticsRepository();
    }

    public StatisticsProfile personalStatistics(Long id) throws CustomException {
        if (id == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidUserId",
                    "ID utilizator invalid"
            );
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new Exception400.NotFoundException(
                    "ResourceNotFound",
                    "UserNotFound",
                    "Utilizatorul nu a fost găsit"
            );
        }

        StatisticsProfile statisticsProfile = new StatisticsProfile();
        User user = userOpt.get();

        statisticsProfile.setId(id);
        statisticsProfile.setUsername(user.getUsername());
        statisticsProfile.setEmail(user.getEmail());
        statisticsProfile.setDataCrearii(user.getCreatedAt().toLocalDate());
        statisticsProfile.setOraCrearii(user.getCreatedAt().toLocalTime());
        statisticsProfile.setDataActualizarii(user.getUpdatedAt().toLocalDate());
        statisticsProfile.setOraActualizarii(user.getUpdatedAt().toLocalTime());

        List<Collection> colectii = collectionRepository.findAllByUserId(id);

        for (Collection collection : colectii) {
            processCollectionStatistics(collection, statisticsProfile);
        }

        calculateProfileAggregateStatistics(statisticsProfile);

        Logger.success("Statistici personale generate cu succes pentru utilizatorul cu ID: " + id);
        return statisticsProfile;
    }

    private void processCollectionStatistics(Collection collection, StatisticsProfile statisticsProfile) throws CustomException {
        StatisticsCollection statisticsCollection = new StatisticsCollection();

        statisticsCollection.setNume(collection.getNume());
        Map<String, Boolean> atributes = getCollectionAttributes(collection);
        String tip = getCollectionTypeName(collection);

        statisticsCollection.setTipColectie(tip);
        statisticsCollection.setVizibilitate(collection.getVisibility());
        statisticsCollection.setDataCrearii(collection.getCreatedAt().toLocalDateTime().toLocalDate());
        statisticsCollection.setOraCrearii(collection.getCreatedAt().toLocalDateTime().toLocalTime());

        List<Obiect> obiecte = objectRepository.findAllByCollectionId(collection.getId());
        processObjectsStatistics(obiecte, statisticsCollection, atributes);

        statisticsCollection.setMostLikedObject();
        statisticsCollection.setMostViewedObject();
        statisticsCollection.setLessLikedObject();
        statisticsCollection.setLessViewedObject();
        statisticsCollection.setDistinctLikes();
        statisticsCollection.setDistinctViews();
        statisticsCollection.setTotalLikes();
        statisticsCollection.setTotalViews();
        statisticsCollection.setTotalValuables();
        statisticsCollection.setMostValuableObject();
        statisticsCollection.setLessValuableObject();

        statisticsProfile.addColection(statisticsCollection);
    }

    private String getCollectionTypeName(Collection collection) {
        switch (collection.getIdTip()) {
            case 1: return "MONEDE";
            case 2: return "TABLOURI";
            case 3: return "TIMBRE";
            case 4: return "VINILURI";
            case 5: return "CUSTOM";
            default: return "NECUNOSCUT";
        }
    }

    private Map<String, Boolean> getCollectionAttributes(Collection collection) throws CustomException {
        Map<String, Boolean> atributes = new HashMap<>();
        switch (collection.getIdTip()) {
            case 1:
                atributes = AtributeObiecte.getMonede();
                break;
            case 2:
                atributes = AtributeObiecte.getTablouri();
                break;
            case 3:
                atributes = AtributeObiecte.getTimbre();
                break;
            case 4:
                atributes = AtributeObiecte.getViniluri();
                break;
            case 5:
                Optional<CustomCollection> customFields = customCollectionRepository.findByCollectionId(collection.getId());
                if (customFields.isPresent()) {
                    CustomCollection custom = customFields.get();
                    atributes.put("material", custom.getMaterial());
                    atributes.put("valoare", custom.getValoare());
                    atributes.put("greutate", custom.getGreutate());
                    atributes.put("nume_artist", custom.getNumeArtist());
                    atributes.put("tematica", custom.getTematica());
                    atributes.put("gen", custom.getGen());
                    atributes.put("casa_discuri", custom.getCasaDiscuri());
                    atributes.put("tara", custom.getTara());
                    atributes.put("an", custom.getAn());
                    atributes.put("stare", custom.getStare());
                    atributes.put("raritate", custom.getRaritate());
                    atributes.put("pret_achizitie", custom.getPretAchizitie());
                }
                break;
            default:
                Logger.warning("Tip de colecție necunoscut: " + collection.getIdTip());
                break;
        }
        return atributes;
    }

    private void processObjectsStatistics(List<Obiect> obiecte, StatisticsCollection statisticsCollection, Map<String, Boolean> atributes) throws CustomException {
        for (Obiect obiect : obiecte) {
            StatisticsObject statisticsObject = new StatisticsObject();

            statisticsObject.setNume(obiect.getName());
            statisticsObject.setDescriere(obiect.getDescriere());
            statisticsObject.setVizibilitate(obiect.getVisibility());
            statisticsObject.setDataCrearii(obiect.getCreatedAt().toLocalDateTime().toLocalDate());
            statisticsObject.setOraCrearii(obiect.getCreatedAt().toLocalDateTime().toLocalTime());

            if (obiect.getUpdatedAt() != null) {
                statisticsObject.setDataActualizarii(obiect.getUpdatedAt().toLocalDateTime().toLocalDate());
                statisticsObject.setOraActualizarii(obiect.getUpdatedAt().toLocalDateTime().toLocalTime());
            }

            statisticsObject.setDistinctLikes(objectLikeRepository.getDistinctLikeCount(obiect.getId()));
            statisticsObject.setDistinctViews(objectViewRepository.getDistinctViewCount(obiect.getId()));
            statisticsObject.setTotalLikes(objectLikeRepository.getLikeCount(obiect.getId()));
            statisticsObject.setTotalViews(objectViewRepository.getViewCount(obiect.getId()));

            Double pret = obiect.getPretAchizitie();
            statisticsObject.setValue(pret);
            statisticsObject.setVisibleFields(atributes);
            statisticsObject.setAtribute(obiect);

            processObjectInteractionUsers(obiect.getId(), statisticsObject);

            statisticsCollection.addObject(statisticsObject);
        }
    }

    private void processObjectInteractionUsers(Long objectId, StatisticsObject statisticsObject) throws CustomException {
        Long topLikerId = objectLikeRepository.getTopLikerId(objectId);
        if (topLikerId != null && topLikerId > 0) {
            Optional<User> topLikerOpt = userRepository.findById(topLikerId);
            topLikerOpt.ifPresent(user -> statisticsObject.setMostLiker(user.getUsername()));
        }

        Long topViewerId = objectViewRepository.getTopViewerId(objectId);
        if (topViewerId != null && topViewerId > 0) {
            Optional<User> topViewerOpt = userRepository.findById(topViewerId);
            topViewerOpt.ifPresent(user -> statisticsObject.setMostViewer(user.getUsername()));
        }

        Long lastLikerId = objectLikeRepository.getLastLikerId(objectId);
        if (lastLikerId != null) {
            Optional<User> lastLikerOpt = userRepository.findById(lastLikerId);
            if (lastLikerOpt.isPresent()) {
                statisticsObject.setLastLiker(lastLikerOpt.get().getUsername());
                statisticsObject.setLastLikedDate(objectLikeRepository.getLastLikerData(objectId));
                statisticsObject.setLastLikedTime(objectLikeRepository.getLastLikerOra(objectId));
            }
        }

        Long lastViewerId = objectViewRepository.getLastViewerId(objectId);
        if (lastViewerId != null && lastViewerId > 0) {
            Optional<User> lastViewerOpt = userRepository.findById(lastViewerId);
            if (lastViewerOpt.isPresent()) {
                statisticsObject.setLastViewer(lastViewerOpt.get().getUsername());
                statisticsObject.setLastViewDate(objectViewRepository.getLastViewerData(objectId));
                statisticsObject.setLastViewTime(objectViewRepository.getLastViewerOra(objectId));
            }
        }
    }

    private void calculateProfileAggregateStatistics(StatisticsProfile statisticsProfile) {
        statisticsProfile.setMostLikedObject();
        statisticsProfile.setLessLikedObject();
        statisticsProfile.setMostViewedObject();
        statisticsProfile.setLessViewedObject();
        statisticsProfile.setMostLikedCollection();
        statisticsProfile.setLessLikedCollection();
        statisticsProfile.setMostViewedCollection();
        statisticsProfile.setLessViewedCollection();
        statisticsProfile.setMostValuableCollection();
        statisticsProfile.setLessValuableCollection();
        statisticsProfile.setMostValuableObject();
        statisticsProfile.setLessValuableObject();
        statisticsProfile.setDistinctLikes();
        statisticsProfile.setDistinctViews();
        statisticsProfile.setTotalLikes();
        statisticsProfile.setTotalViews();
        statisticsProfile.setValue();
    }

    public void getGeneralStatistics() throws CustomException {
        try {
            GeneralStatistics statistics = new GeneralStatistics();

            updateTopLikedCollections(statistics);
            updateTopLikedObjects(statistics);
            updateTopViewedCollections(statistics);
            updateTopViewedObjects(statistics);
            updateTotalCollections(statistics);
            updateGeneralObjectStatistics(statistics);

            XmlGeneralStatistics.generateSimpleRssXml(statistics);

            Logger.success("Statistici generale generate cu succes");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception500.InternalServerErrorException(
                    "StatisticsError",
                    "GenerationFailed",
                    "Eroare la generarea statisticilor: " + e.getMessage(),
                    e
            );
        }
    }

    public void updateTopLikedCollections(GeneralStatistics statistics) throws CustomException {
        try {
            List<Clasament> collections = generalStatisticsRepository.getAllTopCollectionsByLikes();
            statistics.setMostLikedCollections(collections);
            Logger.debug("Top colecții apreciate actualizate: " + collections.size());
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UpdateError",
                    "TopLikedCollectionsUpdateFailed",
                    "Eroare la actualizarea top colecțiilor apreciate: " + e.getMessage(),
                    e
            );
        }
    }

    public void updateTopLikedObjects(GeneralStatistics statistics) throws CustomException {
        try {
            List<Clasament> objects = generalStatisticsRepository.getAllTopObjectsByLikes();
            statistics.setMostLikedObjects(objects);
            Logger.debug("Top obiecte apreciate actualizate: " + objects.size());
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UpdateError",
                    "TopLikedObjectsUpdateFailed",
                    "Eroare la actualizarea top obiectelor apreciate: " + e.getMessage(),
                    e
            );
        }
    }

    public void updateTopViewedCollections(GeneralStatistics statistics) throws CustomException {
        try {
            List<Clasament> collections = generalStatisticsRepository.getAllTopCollectionsByViews();
            statistics.setMostViewedCollections(collections);
            Logger.debug("Top colecții vizionate actualizate: " + collections.size());
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UpdateError",
                    "TopViewedCollectionsUpdateFailed",
                    "Eroare la actualizarea top colecțiilor vizionate: " + e.getMessage(),
                    e
            );
        }
    }

    public void updateTopViewedObjects(GeneralStatistics statistics) throws CustomException {
        try {
            List<Clasament> objects = generalStatisticsRepository.getAllTopObjectsByViews();
            statistics.setMostViewedObjects(objects);
            Logger.debug("Top obiecte vizionate actualizate: " + objects.size());
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UpdateError",
                    "TopViewedObjectsUpdateFailed",
                    "Eroare la actualizarea top obiectelor vizionate: " + e.getMessage(),
                    e
            );
        }
    }

    public void updateTotalCollections(GeneralStatistics statistics) throws CustomException {
        try {
            Long count = collectionRepository.countAllCollections();
            statistics.setTotalCollections(count);
            Logger.debug("Număr total colecții actualizat: " + count);
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UpdateError",
                    "TotalCollectionsUpdateFailed",
                    "Eroare la actualizarea numărului total de colecții: " + e.getMessage(),
                    e
            );
        }
    }

    public void updateGeneralObjectStatistics(GeneralStatistics statistics) throws CustomException {
        try {
            Long totalObjects = objectRepository.countAllObjects();
            statistics.setTotalObjects(totalObjects);
            Logger.debug("Număr total obiecte actualizat: " + totalObjects);

            Double totalValue = objectRepository.getObjectsValue();
            statistics.setTotalValue(totalValue);
            Logger.debug("Valoare totală obiecte actualizată: " + totalValue);

            Double lastMonthPercent = generalStatisticsRepository.getAllPercentObjectsCreatedLastMonth();
            String formattedPercent = DECIMAL_FORMAT.format(lastMonthPercent);
            statistics.setLastMonth(Double.parseDouble(formattedPercent));
            Logger.debug("Procent obiecte în ultima lună actualizat: " + lastMonthPercent + "%");

            Map<String, Double> types = collectionRepository.countAllTypes();
            Double typesTotal = types.values().stream().mapToDouble(Double::doubleValue).sum() + 1.0;

            statistics.setProcentMonede(Double.parseDouble(String.format(US_LOCALE, "%.2f",
                    (types.getOrDefault("MONEDE", 0.0) / typesTotal * 100.0))));
            statistics.setProcentTablouri(Double.parseDouble(String.format(US_LOCALE, "%.2f",
                    (types.getOrDefault("TABLOURI", 0.0) / typesTotal * 100.0))));
            statistics.setProcentTimbre(Double.parseDouble(String.format(US_LOCALE, "%.2f",
                    (types.getOrDefault("TIMBRE", 0.0) / typesTotal * 100.0))));
            statistics.setProcentVinil(Double.parseDouble(String.format(US_LOCALE, "%.2f",
                    (types.getOrDefault("VINILE", 0.0) / typesTotal * 100.0))));
            statistics.setProcentCustom(Double.parseDouble(String.format(US_LOCALE, "%.2f",
                    (types.getOrDefault("CUSTOM", 0.0) / typesTotal * 100.0))));
            Logger.debug("Procente tipuri colecții actualizate");

            List<Clasament> valuableObjects = generalStatisticsRepository.getAllTopObjectsByValue();
            statistics.setMostValuableObjects(valuableObjects);
            Logger.debug("Top obiecte valoroase actualizate: " + valuableObjects.size());

            List<Clasament> valuableCollections = generalStatisticsRepository.getAllTopCollectionsByValue();
            statistics.setMostValuableCollections(valuableCollections);
            Logger.debug("Top colecții valoroase actualizate: " + valuableCollections.size());
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UpdateError",
                    "GeneralObjectStatsUpdateFailed",
                    "Eroare la actualizarea statisticilor generale despre obiecte: " + e.getMessage(),
                    e
            );
        }
    }

    public void updateAllStatisticsAndGenerateRss() throws CustomException {
        try {
            GeneralStatistics statistics = new GeneralStatistics();

            updateTopLikedCollections(statistics);
            updateTopLikedObjects(statistics);
            updateTopViewedCollections(statistics);
            updateTopViewedObjects(statistics);
            updateTotalCollections(statistics);
            updateGeneralObjectStatistics(statistics);

            ensureDirectoryExists(Paths.get(Properties.getPath(), "flux"));

            XmlGeneralStatistics.generateSimpleRssXml(statistics);

            Logger.success("Toate statisticile actualizate și RSS generat cu succes");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UpdateError",
                    "CompleteUpdateFailed",
                    "Eroare la actualizarea completă a statisticilor: " + e.getMessage(),
                    e
            );
        }
    }

    public byte[] personalPdf(Long id) throws CustomException {
        if (id == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidUserId",
                    "ID utilizator invalid"
            );
        }

        try {
            StatisticsProfile statisticsProfile = personalStatistics(id);
            String pdfPath = Properties.getPath() + File.separator + "temp" + File.separator + "statistici_complete_user_" + id + ".pdf";

            ensureDirectoryExists(Paths.get(Properties.getPath(), "temp"));

            generateStatisticsPdf(statisticsProfile, pdfPath);

            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                throw new Exception500.InternalServerErrorException(
                        "FileError",
                        "PdfGenerationFailed",
                        "Fișierul PDF nu a putut fi generat"
                );
            }

            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
            Logger.success("PDF cu statistici personale generat cu succes pentru utilizatorul cu ID: " + id);
            return pdfBytes;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "PdfIOError",
                    "Eroare la citirea fișierului PDF: " + e.getMessage(),
                    e
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UnexpectedError",
                    "PdfGenerationError",
                    "Eroare neașteptată la generarea PDF-ului: " + e.getMessage(),
                    e
            );
        }
    }

    public byte[] personalCsv(Long id) throws CustomException {
        if (id == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidUserId",
                    "ID utilizator invalid"
            );
        }

        try {
            StatisticsProfile statisticsProfile = personalStatistics(id);
            String csvPath = Properties.getPath() + File.separator + "temp" + File.separator + "statistici_complete_user_" + id + ".csv";

            ensureDirectoryExists(Paths.get(Properties.getPath(), "temp"));

            CsvPersonalStatistics.generateStatisticsCsv(statisticsProfile, csvPath);

            File csvFile = new File(csvPath);
            if (!csvFile.exists()) {
                throw new Exception500.InternalServerErrorException(
                        "FileError",
                        "CsvGenerationFailed",
                        "Fișierul CSV nu a putut fi generat"
                );
            }

            byte[] csvBytes = Files.readAllBytes(csvFile.toPath());
            Logger.success("CSV cu statistici personale generat cu succes pentru utilizatorul cu ID: " + id);
            return csvBytes;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "CsvIOError",
                    "Eroare la citirea fișierului CSV: " + e.getMessage(),
                    e
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UnexpectedError",
                    "CsvGenerationError",
                    "Eroare neașteptată la generarea CSV-ului: " + e.getMessage(),
                    e
            );
        }
    }

    public byte[] getStatisticsXml(boolean forceRefresh) throws CustomException {
        try {
            String xmlPath = Properties.getPath() + File.separator + "flux" + File.separator + "statistics_rss.xml";
            File xmlFile = new File(xmlPath);

            if (forceRefresh || !xmlFile.exists() || (System.currentTimeMillis() - xmlFile.lastModified() > 3600000)) {
                Logger.debug("Regenerare statistici generale pentru XML");

                ensureDirectoryExists(Paths.get(Properties.getPath(), "flux"));

                updateAllStatisticsAndGenerateRss();
            } else {
                Logger.debug("Utilizare statistici XML existente (generate acum mai puțin de o oră)");
            }

            if (!xmlFile.exists()) {
                throw new Exception500.InternalServerErrorException(
                        "FileError",
                        "XmlNotFound",
                        "Fișierul XML nu a putut fi găsit"
                );
            }

            byte[] xmlBytes = Files.readAllBytes(xmlFile.toPath());
            Logger.success("XML cu statistici obținut cu succes");
            return xmlBytes;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "XmlIOError",
                    "Eroare la citirea fișierului XML: " + e.getMessage(),
                    e
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UnexpectedError",
                    "XmlGenerationError",
                    "Eroare neașteptată la obținerea XML-ului: " + e.getMessage(),
                    e
            );
        }
    }

    public byte[] getStatisticsPdf() throws CustomException {
        Logger.info("Generare PDF cu statistici generale");

        try {
            getGeneralStatistics();

            String pdfPath = Properties.getPath() + File.separator + "temp" + File.separator + "statistici_generale.pdf";

            ensureDirectoryExists(Paths.get(Properties.getPath(), "temp"));

            backend.api.util.files.PdfGeneralStatistics.generateStatisticsPdf(pdfPath);

            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                throw new Exception500.InternalServerErrorException(
                        "FileError",
                        "PdfGenerationFailed",
                        "Fișierul PDF cu statistici generale nu a putut fi generat"
                );
            }

            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
            Logger.success("PDF cu statistici generale generat cu succes");
            return pdfBytes;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "PdfIOError",
                    "Eroare la citirea fișierului PDF: " + e.getMessage(),
                    e
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UnexpectedError",
                    "PdfGenerationError",
                    "Eroare neașteptată la generarea PDF-ului: " + e.getMessage(),
                    e
            );
        }
    }

    public byte[] getStatisticsCsv() throws CustomException {
        Logger.info("Generare CSV cu statistici generale");

        try {
            byte[] csvBytes = backend.api.util.files.CsvGeneralStatistics.generateCsv();
            Logger.success("CSV cu statistici generale generat cu succes");
            return csvBytes;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UnexpectedError",
                    "CsvGenerationError",
                    "Eroare neașteptată la generarea CSV-ului: " + e.getMessage(),
                    e
            );
        }
    }

    private void ensureDirectoryExists(Path dirPath) throws IOException {
        if (!Files.exists(dirPath)) {
            Logger.debug("Creare director: " + dirPath);
            Files.createDirectories(dirPath);
        }
    }
}