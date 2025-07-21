package backend.api.service;

import backend.api.dataTransferObject.ExplorerObjectDTO;
import backend.api.dataTransferObject.ObjectDTO;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.mapper.ExplorerObjectMapper;
import backend.api.model.Collection;
import backend.api.model.CustomCollection;
import backend.api.model.Obiect;
import backend.api.model.ObjectView;
import backend.api.repository.CollectionRepository;
import backend.api.repository.CustomCollectionRepository;
import backend.api.repository.ObjectRepository;
import backend.api.repository.ObjectLikeRepository;
import backend.api.repository.ObjectViewRepository;
import backend.api.util.json.JsonUtil;
import backend.api.util.multipart.MultipartParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static backend.api.config.applicationConfig.Properties.getPath;


public class ObjectService {
    private final ObjectRepository obiectRepository;
    private final CollectionRepository collectionRepository;
    private final CustomCollectionRepository customCollectionRepository;
    private final ObjectLikeRepository objectLikeRepository;
    private final ObjectViewRepository objectViewRepository;

    public ObjectService() {
        this.obiectRepository = new ObjectRepository();
        this.collectionRepository = new CollectionRepository();
        this.customCollectionRepository = new CustomCollectionRepository();
        this.objectLikeRepository = new ObjectLikeRepository();
        this.objectViewRepository = new ObjectViewRepository();
    }

    public List<ExplorerObjectDTO> getAllPublicObjects() throws CustomException {
        List<Obiect> obiecte = obiectRepository.findAllPublic();

        // Folosim stream-uri paralele pentru a transforma obiectele în DTO-uri în paralel
        List<ExplorerObjectDTO> explorerObjectDTOS = obiecte.parallelStream()
                .map(obiect -> {
                    try {
                        return ExplorerObjectMapper.toDTO(obiect);
                    } catch (Exception e) {
                        Logger.warning("Eroare la convertirea obiectului cu ID " + obiect.getId() + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Logger.success("S-au obținut " + explorerObjectDTOS.size() + " obiecte publice");
        return explorerObjectDTOS;
    }

    public ObjectDTO getPublicObjectById(Long obiectId, Long userId) throws CustomException {
        if (obiectId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidObjectId",
                    "ID obiect invalid"
            );
        }



            Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

            if (obiectOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "ObjectNotFound",
                        "Obiectul nu a fost găsit"
                );
            }

            Obiect obiect = obiectOpt.get();

            if (Boolean.FALSE.equals(obiect.getVisibility())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "PrivateObject",
                        "Acest obiect nu este public"
                );
            }

            recordView(obiectId, userId);

            ObjectDTO result = convertToDTO(obiect);
            Logger.success("Obiect public cu ID " + obiectId + " obținut cu succes");
            return result;

    }

    public Map<String, Object> getCollectionObjects(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidCollectionId",
                    "ID colecție invalid"
            );
        }


            Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);

            if (collectionOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "CollectionNotFound",
                        "Colecția nu a fost găsită"
                );
            }

            Collection collection = collectionOpt.get();

            if (Boolean.FALSE.equals(collection.getVisibility())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "PrivateCollection",
                        "Această colecție nu este publică"
                );
            }

            List<Obiect> obiecte = obiectRepository.findAllPublicByCollectionId(collectionId);
            List<Map<String, Object>> obiecteMaps = new ArrayList<>();

            for (Obiect obiect : obiecte) {
                Map<String, Object> obiectMap = obiectToMap(obiect);
                obiecteMaps.add(obiectMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("collection", collection);
            response.put("obiecte", obiecteMaps);

            Logger.success("S-au obținut " + obiecte.size() + " obiecte pentru colecția cu ID: " + collectionId);
            return response;

    }

    public ObjectDTO getCollectionObjectById(Long collectionId, Long obiectId, Long userId) throws CustomException {
        if (collectionId == null || obiectId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID colecție sau ID obiect invalid"
            );
        }

        Logger.info("Obținere obiect cu ID: " + obiectId + " din colecția cu ID: " + collectionId);


            Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);

            if (collectionOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "CollectionNotFound",
                        "Colecția nu a fost găsită"
                );
            }

            Collection collection = collectionOpt.get();

            if (Boolean.FALSE.equals(collection.getVisibility())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "PrivateCollection",
                        "Această colecție nu este publică"
                );
            }

            Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

            if (obiectOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "ObjectNotFound",
                        "Obiectul nu a fost găsit"
                );
            }

            Obiect obiect = obiectOpt.get();

            if (!obiect.getIdColectie().equals(collectionId.intValue())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "ObjectNotInCollection",
                        "Obiectul nu aparține acestei colecții"
                );
            }

            if (Boolean.FALSE.equals(obiect.getVisibility())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "PrivateObject",
                        "Acest obiect nu este public"
                );
            }

            recordView(obiectId, userId);

            ObjectDTO dto = convertToDTO(obiect);

            Logger.success("Obiect cu ID " + obiectId + " din colecția cu ID " + collectionId + " obținut cu succes");
            return dto;

    }

    public Map<String, Object> likeObject(Long obiectId, Long userId) throws CustomException {
        if (obiectId == null || userId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID obiect sau ID utilizator invalid"
            );
        }

            Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

            if (obiectOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "ObjectNotFound",
                        "Obiectul nu a fost găsit"
                );
            }

            Obiect obiect = obiectOpt.get();

            if (Boolean.FALSE.equals(obiect.getVisibility())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "PrivateObject",
                        "Acest obiect nu este public"
                );
            }

            objectLikeRepository.addLike(obiectId, userId);

            Long likeCount = objectLikeRepository.getLikeCount(obiectId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Like adăugat cu succes");
            response.put("likes", likeCount);

            Logger.success("Apreciere adăugată cu succes la obiectul cu ID: " + obiectId);
            return response;

    }

    public Map<String, Object> likeCollectionObject(Long collectionId, Long obiectId, Long userId) throws CustomException {
        if (collectionId == null || obiectId == null || userId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID colecție, ID obiect sau ID utilizator invalid"
            );
        }

        Logger.info("Adăugare apreciere la obiectul cu ID: " + obiectId + " din colecția cu ID: " + collectionId + " de către utilizatorul cu ID: " + userId);


            Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);

            if (collectionOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "CollectionNotFound",
                        "Colecția nu a fost găsită"
                );
            }

            Collection collection = collectionOpt.get();

            if (Boolean.FALSE.equals(collection.getVisibility())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "PrivateCollection",
                        "Această colecție nu este publică"
                );
            }

            Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

            if (obiectOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "ObjectNotFound",
                        "Obiectul nu a fost găsit"
                );
            }

            Obiect obiect = obiectOpt.get();

            if (!obiect.getIdColectie().equals(collectionId.intValue())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "ObjectNotInCollection",
                        "Obiectul nu aparține acestei colecții"
                );
            }

            if (Boolean.FALSE.equals(obiect.getVisibility())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "PrivateObject",
                        "Acest obiect nu este public"
                );
            }

            Map<String, Object> result = likeObject(obiectId, userId);
            Logger.success("Apreciere adăugată cu succes la obiectul cu ID: " + obiectId + " din colecția cu ID: " + collectionId);
            return result;

    }

    public Map<String, Object> getMyCollectionObjects(Long userId, Long collectionId) throws CustomException {
        if (userId == null || collectionId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID utilizator sau ID colecție invalid"
            );
        }

        Logger.info("Obținere obiecte pentru colecția proprie cu ID: " + collectionId + " a utilizatorului cu ID: " + userId);

        try {
            Collection collection = verifyCollectionOwnership(userId, collectionId);
            Logger.debug("Verificare proprietate colecție: OK");

            List<Obiect> obiecte = obiectRepository.findAllByCollectionId(collectionId);

            // Optimizare: Folosim stream paralel pentru transformarea obiectelor în Maps
            List<Map<String, Object>> obiecteMaps = obiecte.parallelStream()
                    .map(obiect -> {
                        try {
                            return obiectToMap(obiect);
                        } catch (CustomException e) {
                            Logger.warning("Eroare la maparea obiectului cu ID " + obiect.getId() + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("collection", collection);
            response.put("obiecte", obiecteMaps);

            Logger.success("S-au obținut " + obiecte.size() + " obiecte pentru colecția proprie cu ID: " + collectionId);
            return response;
        } catch (CustomException e) {
            Logger.exception(e.getNume());
            Logger.error("Eroare la obținerea obiectelor colecției proprii: " + e.getDescriere());
            throw e;
        }
    }

    public ObjectDTO getMyObjectById(Long userId, Long obiectId) throws CustomException {
        if (userId == null || obiectId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID utilizator sau ID obiect invalid"
            );
        }

            Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

            if (obiectOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "ObjectNotFound",
                        "Obiectul nu a fost găsit"
                );
            }

            Obiect obiect = obiectOpt.get();

            Optional<Collection> collectionOpt = collectionRepository.findById(Long.valueOf(obiect.getIdColectie()));

            if (collectionOpt.isEmpty() || !collectionOpt.get().getIdUser().equals(userId)) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "NotOwner",
                        "Nu aveți acces la acest obiect"
                );
            }

            recordView(obiectId, userId);

            ObjectDTO dto = convertToDTO(obiect);

            Logger.success("Obiect propriu cu ID " + obiectId + " obținut cu succes");
            return dto;

    }

    public ObjectDTO getMyCollectionObjectById(Long userId, Long collectionId, Long obiectId) throws CustomException {
        if (userId == null || collectionId == null || obiectId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID utilizator, ID colecție sau ID obiect invalid"
            );
        }


        Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

        if (obiectOpt.isEmpty()) {
            throw new Exception400.NotFoundException(
                    "ResourceNotFound",
                    "ObjectNotFound",
                    "Obiectul nu a fost găsit"
            );
        }

        Obiect obiect = obiectOpt.get();

        if (!obiect.getIdColectie().equals(collectionId.intValue())) {
            throw new Exception400.ForbiddenException(
                    "AccessDenied",
                    "ObjectNotInCollection",
                    "Obiectul nu aparține acestei colecții"
            );
        }

        recordView(obiectId, userId);

        ObjectDTO dto = convertToDTO(obiect);

        Logger.success("Obiect cu ID " + obiectId + " din colecția proprie cu ID " + collectionId + " obținut cu succes");
        return dto;
    }

    public Map<String, Object> getAllMyObjects(Long userId) throws CustomException {
        if (userId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidUserId",
                    "ID utilizator invalid"
            );
        }

            List<Obiect> obiecte = obiectRepository.findAllByUserId(userId);
            List<Map<String, Object>> obiecteMaps = new ArrayList<>();

            for (Obiect obiect : obiecte) {
                Map<String, Object> obiectMap = obiectToMap(obiect);
                obiecteMaps.add(obiectMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("obiecte", obiecteMaps);

            Logger.success("S-au obținut " + obiecte.size() + " obiecte pentru utilizatorul cu ID: " + userId);
            return response;

    }

    public Map<String, Object> createObject(Long userId, Long collectionId, InputStream is, String contentType) throws CustomException {
        if (userId == null || collectionId == null || is == null || contentType == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "Parametri invalizi pentru crearea obiectului"
            );
        }


        try {
            Collection collection = verifyCollectionOwnership(userId, collectionId);

            if (!contentType.startsWith("multipart/form-data")) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidContentType",
                        "Trebuie să încărcați datele prin multipart/form-data"
                );
            }

            MultipartParser parser = new MultipartParser(is, contentType);
            Logger.debug("Parser multipart inițializat");

            MultipartParser.MultipartFile imageFile = parser.getFile("image");
            Logger.debug("Fișier imagine obținut: " + (imageFile != null ? "Da" : "Nu"));

            String obiectJson = parser.getFormField("obiect");

            if (obiectJson == null || obiectJson.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "MissingObjectData",
                        "Datele obiectului sunt obligatorii"
                );
            }

            Obiect obiect = JsonUtil.fromJson(obiectJson, Obiect.class);
            Logger.debug("Obiect deserializat din JSON");

            obiect.setIdColectie(collectionId.intValue());
            obiect.setName(obiect.getName());

            validateObjectData(obiect, collection);
            Logger.debug("Date obiect validate");

            Timestamp currentTime = Timestamp.from(Instant.now());
            obiect.setCreatedAt(currentTime);
            obiect.setUpdatedAt(currentTime);

            String imagePath = processObjectImage(null, imageFile);
            obiect.setImage(imagePath);

            Long obiectId = obiectRepository.save(obiect);
            obiect.setId(obiectId);

            Path destinationDir = Paths.get(getPath() + "/obiecte");
            ensureDirectoryExists(destinationDir);

            processAndMoveObjectImage(obiect, imageFile, destinationDir);

            obiectRepository.update(obiect);

            Map<String, Object> response = new HashMap<>();
            response.put("obiect", obiectToMap(obiect));

            Logger.success("Obiect creat cu succes în colecția cu ID: " + collectionId + ", ID obiect: " + obiectId);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "FileProcessingError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    private void processAndMoveObjectImage(Obiect obiect, MultipartParser.MultipartFile imageFile, Path destinationDir) throws IOException {
        if (imageFile != null && !obiect.getImage().equals(getPath() + "/default_obict_image/picture.png")) {
            Path sourceImage = Paths.get(obiect.getImage());
            String fileName = sourceImage.getFileName().toString();
            Path destinationImage = destinationDir.resolve(fileName);

            Files.move(sourceImage, destinationImage, StandardCopyOption.REPLACE_EXISTING);

            obiect.setImage(destinationImage.toString().replace('\\', '/'));
        }
        else {
            Path sourceImage = Paths.get(getPath() + "/default_obict_image/picture.png");
            String fileName = UUID.randomUUID() + ".png";
            Path destinationImage = destinationDir.resolve(fileName);

            Files.copy(sourceImage, destinationImage, StandardCopyOption.REPLACE_EXISTING);

            obiect.setImage(destinationImage.toString().replace('\\', '/'));
        }
    }

    public Map<String, Object> updateObject(Long userId, Long collectionId, Long obiectId, InputStream is, String contentType) throws CustomException {
        if (userId == null || collectionId == null || obiectId == null || is == null || contentType == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "Parametri invalizi pentru actualizarea obiectului"
            );
        }


        try {
            Collection collection = verifyCollectionOwnership(userId, collectionId);

            Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

            if (obiectOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "ObjectNotFound",
                        "Obiectul nu a fost găsit"
                );
            }

            Obiect obiect = obiectOpt.get();

            if (!obiect.getIdColectie().equals(collectionId.intValue())) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "ObjectNotInCollection",
                        "Obiectul nu aparține acestei colecții"
                );
            }

            if (!contentType.startsWith("multipart/form-data")) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidContentType",
                        "Trebuie să încărcați datele prin multipart/form-data"
                );
            }

            MultipartParser parser = new MultipartParser(is, contentType);
            Logger.debug("Parser multipart inițializat");

            MultipartParser.MultipartFile imageFile = parser.getFile("image");

            String obiectJson = parser.getFormField("obiect");

            if (obiectJson != null && !obiectJson.isEmpty()) {
                Obiect updatedObiect = JsonUtil.fromJson(obiectJson, Obiect.class);

                updateObjectFields(obiect, updatedObiect);

                validateObjectData(obiect, collection);
                Logger.debug("Date obiect validate");
            }

            String oldImagePath = obiect.getImage();

            if (imageFile != null) {
                String newImagePath = processObjectImage(obiect.getId(), imageFile);

                Path destinationDir = Paths.get(getPath() +  File.separator+"obiecte");
                ensureDirectoryExists(destinationDir);

                if (!newImagePath.equals(getPath() + "/default_obict_image/picture.png")) {
                    Path sourceImage = Paths.get(newImagePath);
                    String fileName = sourceImage.getFileName().toString();
                    Path destinationImage = destinationDir.resolve(fileName);

                    Files.move(sourceImage, destinationImage, StandardCopyOption.REPLACE_EXISTING);

                    obiect.setImage(destinationImage.toString().replace('\\', '/'));
                } else {
                    Path sourceImage = Paths.get(getPath() + "/default_obict_image/picture.png");
                    String fileName = "picture.png";
                    Path destinationImage = destinationDir.resolve(fileName);

                    Files.copy(sourceImage, destinationImage, StandardCopyOption.REPLACE_EXISTING);

                    obiect.setImage(destinationImage.toString().replace('\\', '/'));
                }
                deleteOldImage(oldImagePath);
            }

            obiect.setUpdatedAt(Timestamp.from(Instant.now()));

            obiectRepository.update(obiect);

            Map<String, Object> response = new HashMap<>();
            response.put("obiect", obiectToMap(obiect));

            Logger.success("Obiect cu ID " + obiectId + " actualizat cu succes în colecția cu ID: " + collectionId);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "FileProcessingError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    private void deleteOldImage(String oldImagePath) {
        if (oldImagePath != null && !oldImagePath.equals(getPath() + "/default_obict_image/picture.png")) {
            try {
                Path oldImageFile = Paths.get(oldImagePath);
                if (Files.exists(oldImageFile)) {
                    Files.deleteIfExists(oldImageFile);
                    Logger.debug("Imagine veche ștearsă: " + oldImagePath);
                }
            } catch (IOException e) {
                Logger.warning("Nu s-a putut șterge imaginea veche: " + e.getMessage());
            }
        }
    }

    public Map<String, Object> updateMyObject(Long userId, Long obiectId, InputStream is, String contentType) throws CustomException {
        if (userId == null || obiectId == null || is == null || contentType == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "Parametri invalizi pentru actualizarea obiectului"
            );
        }


            Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

            if (obiectOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "ObjectNotFound",
                        "Obiectul nu a fost găsit"
                );
            }

            Obiect obiect = obiectOpt.get();

            Optional<Collection> collectionOpt = collectionRepository.findById(Long.valueOf(obiect.getIdColectie()));

            if (collectionOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "CollectionNotFound",
                        "Colecția obiectului nu a fost găsită"
                );
            }

            Collection collection = collectionOpt.get();

            if (!collection.getIdUser().equals(userId)) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "NotOwner",
                        "Nu aveți acces să modificați acest obiect"
                );
            }

            Map<String, Object> result = updateObject(userId, Long.valueOf(obiect.getIdColectie()), obiectId, is, contentType);
            Logger.success("Obiect propriu cu ID " + obiectId + " actualizat cu succes");
            return result;

    }

    public Map<String, Object> deleteObject(Long userId, Long collectionId, Long obiectId) throws CustomException {
        if (userId == null || collectionId == null || obiectId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "Parametri invalizi pentru ștergerea obiectului"
            );
        }


        verifyCollectionOwnership(userId, collectionId);

        Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

        if (obiectOpt.isEmpty()) {
            throw new Exception400.NotFoundException(
                    "ResourceNotFound",
                    "ObjectNotFound",
                    "Obiectul nu a fost găsit"
            );
        }

        Obiect obiect = obiectOpt.get();

        if (!obiect.getIdColectie().equals(collectionId.intValue())) {
            throw new Exception400.ForbiddenException(
                    "AccessDenied",
                    "ObjectNotInCollection",
                    "Obiectul nu aparține acestei colecții"
            );
        }

        objectViewRepository.deleteAllByObjectId(obiectId);
        Logger.debug("Vizualizări șterse pentru obiectul cu ID: " + obiectId);

        objectLikeRepository.deleteAllByObjectId(obiectId);
        Logger.debug("Aprecieri șterse pentru obiectul cu ID: " + obiectId);

        Path obiectDir = Paths.get(obiect.getImage());
        if (Files.exists(obiectDir)) {
            deleteOldImage(obiectDir.toString());
            Logger.debug("Director de imagini șters pentru obiectul cu ID: " + obiectId);
        }

        obiectRepository.delete(obiectId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Obiectul a fost șters cu succes");

        Logger.success("Obiect cu ID " + obiectId + " șters cu succes din colecția cu ID: " + collectionId);
        return response;
    }

    public Map<String, Object> deleteMyObject(Long userId, Long obiectId) throws CustomException {
        if (userId == null || obiectId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "Parametri invalizi pentru ștergerea obiectului"
            );
        }

            Optional<Obiect> obiectOpt = obiectRepository.findById(obiectId);

            if (obiectOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "ObjectNotFound",
                        "Obiectul nu a fost găsit"
                );
            }

            Obiect obiect = obiectOpt.get();

            Optional<Collection> collectionOpt = collectionRepository.findById(Long.valueOf(obiect.getIdColectie()));

            if (collectionOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "CollectionNotFound",
                        "Colecția obiectului nu a fost găsită"
                );
            }

            Collection collection = collectionOpt.get();

            if (!collection.getIdUser().equals(userId)) {
                throw new Exception400.ForbiddenException(
                        "AccessDenied",
                        "NotOwner",
                        "Nu aveți acces să ștergeți acest obiect"
                );
            }

            Map<String, Object> result = deleteObject(userId, Long.valueOf(obiect.getIdColectie()), obiectId);
            Logger.success("Obiect propriu cu ID " + obiectId + " șters cu succes");
            return result;

    }

    private void recordView(Long obiectId, Long userId) throws CustomException {
        try {
            ObjectView objectView = new ObjectView();
            objectView.setIdObject(obiectId);
            objectView.setIdUser(userId);
            objectView.setData(Date.valueOf(LocalDate.now()));
            objectView.setOra(Time.valueOf(LocalTime.now()));

            objectViewRepository.save(objectView);
            Logger.debug("Vizualizare înregistrată pentru obiectul cu ID: " + obiectId + " de către utilizatorul cu ID: " + userId);
        } catch (CustomException e) {
            Logger.warning("Nu s-a putut înregistra vizualizarea: " + e.getMessage());
        }
    }

    private ObjectDTO convertToDTO(Obiect obiect) throws CustomException {
        ObjectDTO obiectDTO = new ObjectDTO();

        obiectDTO.setId(obiect.getId());
        obiectDTO.setIdColectie(obiect.getIdColectie());
        obiectDTO.setNumeObiect(obiect.getName());
        obiectDTO.setDescriere(obiect.getDescriere());
        obiectDTO.setMaterial(obiect.getMaterial());
        obiectDTO.setValoare(obiect.getValoare());
        obiectDTO.setGreutate(obiect.getGreutate());
        obiectDTO.setNumeArtist(obiect.getNumeArtist());
        obiectDTO.setTematica(obiect.getTematica());
        obiectDTO.setGen(obiect.getGen());
        obiectDTO.setCasaDiscuri(obiect.getCasaDiscuri());
        obiectDTO.setTara(obiect.getTara());
        obiectDTO.setAn(obiect.getAn());
        obiectDTO.setStare(obiect.getStare());
        obiectDTO.setRaritate(obiect.getRaritate());
        obiectDTO.setPretAchizitie(obiect.getPretAchizitie().floatValue());
        obiectDTO.setVisibility(obiect.getVisibility());
        obiectDTO.setCreatedAt(obiect.getCreatedAt());
        obiectDTO.setUpdatedAt(obiect.getUpdatedAt());

        loadObjectImage(obiect, obiectDTO);

        try {
            obiectDTO.setViews(objectViewRepository.getViewCount(obiect.getId()));
            obiectDTO.setLikes(objectLikeRepository.getLikeCount(obiect.getId()));
        } catch (CustomException e) {
            Logger.warning("Nu s-au putut încărca statisticile pentru obiectul cu ID " + obiect.getId() + ": " + e.getMessage());
            obiectDTO.setViews(0L);
            obiectDTO.setLikes(0L);
        }

        setVisibleFields(obiect, obiectDTO);

        return obiectDTO;
    }

    private void loadObjectImage(Obiect obiect, ObjectDTO obiectDTO) {
        String imagePath = obiect.getImage();
        try {
            if (imagePath != null && !imagePath.isBlank()) {
                Path file = Path.of(imagePath);
                if (Files.exists(file)) {
                    byte[] imageBytes = Files.readAllBytes(file);

                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    obiectDTO.setImage(base64Image);

                    String imageName = file.getFileName().toString();
                    obiectDTO.setImageName(imageName);
                    Logger.debug("Imagine încărcată cu succes pentru obiectul cu ID: " + obiect.getId());
                } else {
                    Logger.warning("Fișierul imagine nu există la calea: " + imagePath);
                }
            }
        } catch (IOException e) {
            Logger.warning("Nu s-a putut accesa imaginea obiectului cu ID " + obiect.getId() + ": " + e.getMessage());
        }
    }

    private void setVisibleFields(Obiect obiect, ObjectDTO obiectDTO) {
        try {
            Map<String, Boolean> visibleFields = new HashMap<>();

            Optional<Collection> collectionOpt = collectionRepository.findById(Long.valueOf(obiect.getIdColectie()));

            if (collectionOpt.isPresent()) {
                Collection collection = collectionOpt.get();

                if (collection.isCustomType()) {
                    loadCustomCollectionFields(collection.getId(), visibleFields);
                } else {
                    setFieldsForPredefinedCollection(collection.getIdTip(), visibleFields);
                }
            }

            obiectDTO.setVisibleFields(visibleFields);
        } catch (CustomException e) {
            Logger.warning("Nu s-au putut încărca câmpurile vizibile pentru obiectul cu ID " + obiect.getId() + ": " + e.getMessage());
            obiectDTO.setVisibleFields(new HashMap<>());
        }
    }

    private void loadCustomCollectionFields(Long collectionId, Map<String, Boolean> visibleFields) throws CustomException {
        Optional<CustomCollection> customFieldsOpt = customCollectionRepository.findByCollectionId(collectionId);

        if (customFieldsOpt.isPresent()) {
            CustomCollection customFields = customFieldsOpt.get();
            visibleFields.put("material", customFields.getMaterial());
            visibleFields.put("valoare", customFields.getValoare());
            visibleFields.put("greutate", customFields.getGreutate());
            visibleFields.put("nume_artist", customFields.getNumeArtist());
            visibleFields.put("tematica", customFields.getTematica());
            visibleFields.put("gen", customFields.getGen());
            visibleFields.put("casa_discuri", customFields.getCasaDiscuri());
            visibleFields.put("tara", customFields.getTara());
            visibleFields.put("an", customFields.getAn());
            visibleFields.put("stare", customFields.getStare());
            visibleFields.put("raritate", customFields.getRaritate());
            visibleFields.put("pret_achizitie", customFields.getPretAchizitie());
        }
    }

    private void setFieldsForPredefinedCollection(Integer collectionType, Map<String, Boolean> visibleFields) {
        switch (collectionType) {
            case 1: // Monede
                setVisibleFieldsForMonede(visibleFields);
                break;
            case 2: // Tablouri
                setVisibleFieldsForTablouri(visibleFields);
                break;
            case 3: // Timbre
                setVisibleFieldsForTimbre(visibleFields);
                break;
            case 4: // Viniluri
                setVisibleFieldsForViniluri(visibleFields);
                break;
            default:
                Logger.warning("Tip de colecție necunoscut: " + collectionType);
                break;
        }
    }

    private Map<String, Object> obiectToMap(Obiect obiect) throws CustomException {
        Map<String, Object> map = new HashMap<>();

        map.put("id", obiect.getId());
        map.put("id_colectie", obiect.getIdColectie());
        map.put("nume_colectie", obiect.getName());
        map.put("descriere", obiect.getDescriere());
        map.put("material", obiect.getMaterial());
        map.put("valoare", obiect.getValoare());
        map.put("greutate", obiect.getGreutate());
        map.put("nume_artist", obiect.getNumeArtist());
        map.put("tematica", obiect.getTematica());
        map.put("gen", obiect.getGen());
        map.put("casa_discuri", obiect.getCasaDiscuri());
        map.put("tara", obiect.getTara());
        map.put("an", obiect.getAn());
        map.put("stare", obiect.getStare());
        map.put("raritate", obiect.getRaritate());
        map.put("pret_achizitie", obiect.getPretAchizitie());
        map.put("visibility", obiect.getVisibility());
        map.put("created_at", obiect.getCreatedAt().toLocalDateTime().toLocalDate());
        map.put("updated_at", obiect.getUpdatedAt().toLocalDateTime().toLocalDate());

        loadObjectImageToMap(obiect, map);

        try {
            Map<String, Boolean> visibleFields = new HashMap<>();

            Optional<Collection> collectionOpt = collectionRepository.findById(Long.valueOf(obiect.getIdColectie()));

            if (collectionOpt.isPresent()) {
                Collection collection = collectionOpt.get();

                if (collection.isCustomType()) {
                    loadCustomCollectionFields(collection.getId(), visibleFields);
                } else {
                    setFieldsForPredefinedCollection(collection.getIdTip(), visibleFields);
                }
            }

            map.put("visible_fields", visibleFields);
        } catch (CustomException e) {
            Logger.warning("Nu s-au putut încărca câmpurile vizibile pentru obiectul cu ID " + obiect.getId() + ": " + e.getMessage());
            map.put("visible_fields", new HashMap<>());
        }

        try {
            map.put("views", objectViewRepository.getViewCount(obiect.getId()));
            map.put("likes", objectLikeRepository.getLikeCount(obiect.getId()));
        } catch (CustomException e) {
            map.put("views", 0);
            map.put("likes", 0);
            Logger.warning("Nu s-au putut încărca statisticile pentru obiectul cu ID " + obiect.getId() + ": " + e.getMessage());
        }

        return map;
    }

    private void loadObjectImageToMap(Obiect obiect, Map<String, Object> map) {
        String imagePath = obiect.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Path file = Path.of(imagePath);
                if (Files.exists(file)) {
                    byte[] imageBytes = Files.readAllBytes(file);
                    map.put("image", Base64.getEncoder().encodeToString(imageBytes));
                    map.put("image_name", file.getFileName().toString());
                    Logger.debug("Imagine încărcată cu succes pentru obiectul cu ID: " + obiect.getId());
                } else {
                    Logger.warning("Fișierul imagine nu există la calea: " + imagePath);
                    map.put("image_path", imagePath);
                }
            } catch (IOException e) {
                Logger.warning("Nu s-a putut citi imaginea pentru obiectul cu ID " + obiect.getId() + ": " + e.getMessage());
                map.put("image_path", imagePath);
            }
        }
    }

    private void setVisibleFieldsForMonede(Map<String, Boolean> visibleFields) {
        visibleFields.put("material", true);
        visibleFields.put("valoare", true);
        visibleFields.put("greutate", true);
        visibleFields.put("nume_artist", false);
        visibleFields.put("tematica", false);
        visibleFields.put("gen", false);
        visibleFields.put("casa_discuri", false);
        visibleFields.put("tara", true);
        visibleFields.put("an", true);
        visibleFields.put("stare", true);
        visibleFields.put("raritate", true);
        visibleFields.put("pret_achizitie", true);
    }

    private void setVisibleFieldsForTablouri(Map<String, Boolean> visibleFields) {
        visibleFields.put("material", false);
        visibleFields.put("valoare", false);
        visibleFields.put("greutate", false);
        visibleFields.put("nume_artist", true);
        visibleFields.put("tematica", false);
        visibleFields.put("gen", false);
        visibleFields.put("casa_discuri", false);
        visibleFields.put("tara", true);
        visibleFields.put("an", true);
        visibleFields.put("stare", true);
        visibleFields.put("raritate", true);
        visibleFields.put("pret_achizitie", true);
    }

    private void setVisibleFieldsForTimbre(Map<String, Boolean> visibleFields) {
        visibleFields.put("material", false);
        visibleFields.put("valoare", false);
        visibleFields.put("greutate", false);
        visibleFields.put("nume_artist", false);
        visibleFields.put("tematica", false);
        visibleFields.put("gen", false);
        visibleFields.put("casa_discuri", false);
        visibleFields.put("tara", true);
        visibleFields.put("an", true);
        visibleFields.put("stare", true);
        visibleFields.put("raritate", true);
        visibleFields.put("pret_achizitie", true);
    }

    private void setVisibleFieldsForViniluri(Map<String, Boolean> visibleFields) {
        visibleFields.put("material", false);
        visibleFields.put("valoare", false);
        visibleFields.put("greutate", false);
        visibleFields.put("nume_artist", true);
        visibleFields.put("tematica", false);
        visibleFields.put("gen", true);
        visibleFields.put("casa_discuri", true);
        visibleFields.put("tara", true);
        visibleFields.put("an", true);
        visibleFields.put("stare", true);
        visibleFields.put("raritate", true);
        visibleFields.put("pret_achizitie", true);
    }

    private void updateObjectFields(Obiect target, Obiect source) {
        if (source.getDescriere() != null) {
            target.setDescriere(source.getDescriere());
        }
        if (source.getMaterial() != null) {
            target.setMaterial(source.getMaterial());
        }
        if (source.getValoare() != 0.0) {
            target.setValoare(source.getValoare());
        }
        if (source.getGreutate() != 0) {
            target.setGreutate(source.getGreutate());
        }
        if (source.getNumeArtist() != null) {
            target.setNumeArtist(source.getNumeArtist());
        }
        if (source.getTematica() != null) {
            target.setTematica(source.getTematica());
        }
        if (source.getGen() != null) {
            target.setGen(source.getGen());
        }
        if (source.getCasaDiscuri() != null) {
            target.setCasaDiscuri(source.getCasaDiscuri());
        }
        if (source.getTara() != null) {
            target.setTara(source.getTara());
        }
        if (source.getAn() != null) {
            target.setAn(source.getAn());
        }
        if (source.getStare() != null) {
            target.setStare(source.getStare());
        }
        if (source.getRaritate() != null) {
            target.setRaritate(source.getRaritate());
        }
        if (source.getPretAchizitie() != null) {
            target.setPretAchizitie(source.getPretAchizitie());
        }
        if (source.getVisibility() != null) {
            target.setVisibility(source.getVisibility());
        }
    }

    private String processObjectImage(Long obiectId, MultipartParser.MultipartFile imageFile) throws IOException, CustomException {
        if (imageFile == null) {
            Logger.debug("Nicio imagine furnizată, se folosește imaginea implicită");
            return getPath() + "/default_obict_image/picture.png";
        }

        String filename = imageFile.getFilename();
        if (filename == null || filename.isEmpty()) {
            Logger.debug("Nume fișier imagine gol, se folosește imaginea implicită");
            return getPath() + "/default_obict_image/picture.png";
        }

        String extension = imageFile.getFileExtension(filename).toLowerCase();

        if (!Arrays.asList("jpg", "jpeg", "png", "gif").contains(extension)) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidFileFormat",
                    "Format fișier neacceptat. Sunt acceptate doar: JPG, JPEG, PNG, GIF"
            );
        }

        String uniqueFilename = UUID.randomUUID() + "." + extension;

        if (obiectId == null) {
            Path tempDir = Paths.get(getPath() + File.separator + "temp");
            ensureDirectoryExists(tempDir);

            Path destination = tempDir.resolve(uniqueFilename);
            Files.copy(imageFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return destination.toString();
        }
        else {
            Path obiectDir = Paths.get(getPath() + File.separator + "obiecte");
            ensureDirectoryExists(obiectDir);

            Path destination = obiectDir.resolve(uniqueFilename);
            Files.copy(imageFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return destination.toString();
        }
    }

    private Collection verifyCollectionOwnership(Long userId, Long collectionId) throws CustomException {
        Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);

        if (collectionOpt.isEmpty()) {
            throw new Exception400.NotFoundException(
                    "ResourceNotFound",
                    "CollectionNotFound",
                    "Colecția nu a fost găsită"
            );
        }

        Collection collection = collectionOpt.get();

        if (!collection.getIdUser().equals(userId)) {
            throw new Exception400.ForbiddenException(
                    "AccessDenied",
                    "NotOwner",
                    "Nu aveți acces la această colecție"
            );
        }

        if (collection.isCustomType()) {
            Optional<CustomCollection> customFields = customCollectionRepository.findByCollectionId(collectionId);
            customFields.ifPresent(collection::setCustomFields);
        }

        return collection;
    }

    private void validateObjectData(Obiect obiect, Collection collection) throws CustomException {
        if (obiect.getName() == null || obiect.getName().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingName",
                    "Numele colecției este obligatoriu"
            );
        }

        if (obiect.getDescriere() == null || obiect.getDescriere().trim().isEmpty()) {
            Logger.warning("Descriere lipsă, se folosește valoarea implicită");
            obiect.setDescriere("NO DESCRIPTION");
        }

        if (collection.isCustomType() && collection.getCustomFields() != null) {
            validateCustomCollectionFields(obiect, collection.getCustomFields());
        } else {
            validatePredefinedCollectionFields(obiect, collection.getIdTip());
        }
    }

    private void validateCustomCollectionFields(Obiect obiect, CustomCollection customFields) throws CustomException {
        validateField(customFields.getMaterial(), obiect.getMaterial(), "material");
        validateNumericField(customFields.getValoare(), obiect.getValoare(), "valoare");
        validateNumericField(customFields.getGreutate(), obiect.getGreutate(), "greutate");
        validateField(customFields.getNumeArtist(), obiect.getNumeArtist(), "nume artist");
        validateField(customFields.getTematica(), obiect.getTematica(), "tematica");
        validateField(customFields.getGen(), obiect.getGen(), "gen");
        validateField(customFields.getCasaDiscuri(), obiect.getCasaDiscuri(), "casa de discuri");
        validateField(customFields.getTara(), obiect.getTara(), "tara");
        validateDateField(customFields.getAn(), obiect.getAn());
        validateField(customFields.getStare(), obiect.getStare(), "stare");
        validateField(customFields.getRaritate(), obiect.getRaritate(), "raritate");
        validateNumericField(customFields.getPretAchizitie(), obiect.getPretAchizitie(), "pret achizitie");

        cleanupFields(obiect, customFields);
    }

    private void validateField(Boolean fieldRequired, String fieldValue, String fieldName) throws CustomException {
        if (Boolean.TRUE.equals(fieldRequired) && (fieldValue == null || fieldValue.trim().isEmpty())) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul '" + fieldName + "' este obligatoriu pentru această colecție"
            );
        }
    }

    private void validateNumericField(Boolean fieldRequired, Number fieldValue, String fieldName) throws CustomException {
        if (Boolean.TRUE.equals(fieldRequired) && fieldValue == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul '" + fieldName + "' este obligatoriu pentru această colecție"
            );
        }
    }

    private void validateDateField(Boolean fieldRequired, Object fieldValue) throws CustomException {
        if (Boolean.TRUE.equals(fieldRequired) && fieldValue == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul '" + "an" + "' este obligatoriu pentru această colecție"
            );
        }
    }

    private void cleanupFields(Obiect obiect, CustomCollection customFields) {
        if (Boolean.FALSE.equals(customFields.getMaterial())) obiect.setMaterial(null);
        if (Boolean.FALSE.equals(customFields.getValoare())) obiect.setValoare(0.0f);
        if (Boolean.FALSE.equals(customFields.getGreutate())) obiect.setGreutate(0);
        if (Boolean.FALSE.equals(customFields.getNumeArtist())) obiect.setNumeArtist(null);
        if (Boolean.FALSE.equals(customFields.getTematica())) obiect.setTematica(null);
        if (Boolean.FALSE.equals(customFields.getGen())) obiect.setGen(null);
        if (Boolean.FALSE.equals(customFields.getCasaDiscuri())) obiect.setCasaDiscuri(null);
        if (Boolean.FALSE.equals(customFields.getTara())) obiect.setTara(null);
        if (Boolean.FALSE.equals(customFields.getAn())) obiect.setAn(null);
        if (Boolean.FALSE.equals(customFields.getStare())) obiect.setStare(null);
        if (Boolean.FALSE.equals(customFields.getRaritate())) obiect.setRaritate(null);
        if (Boolean.FALSE.equals(customFields.getPretAchizitie())) obiect.setPretAchizitie(0.0);
    }

    private void validatePredefinedCollectionFields(Obiect obiect, Integer collectionType) throws CustomException {
        switch (collectionType) {
            case 1: // Monede
                validateMonedeFields(obiect);
                break;
            case 2: // Tablouri
                validateTablouriFields(obiect);
                break;
            case 3: // Timbre
                validateTimbreFields(obiect);
                break;
            case 4: // Viniluri
                validateViniluriFields(obiect);
                break;
            default:
                Logger.warning("Tip de colecție necunoscut: " + collectionType);
                break;
        }
    }

    private void validateMonedeFields(Obiect obiect) throws CustomException {
        if (obiect.getMaterial() == null || obiect.getMaterial().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'material' este obligatoriu pentru monede"
            );
        }

        if (obiect.getValoare() == 0.0) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'valoare' este obligatoriu pentru monede"
            );
        }

        if (obiect.getGreutate() == 0.0) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'greutate' este obligatoriu pentru monede"
            );
        }

        if (obiect.getTara() == null || obiect.getTara().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'tara' este obligatoriu pentru monede"
            );
        }

        if (obiect.getAn() == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'an' este obligatoriu pentru monede"
            );
        }

        if (obiect.getStare() == null || obiect.getStare().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'stare' este obligatoriu pentru monede"
            );
        }

        if (obiect.getRaritate() == null || obiect.getRaritate().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'raritate' este obligatoriu pentru monede"
            );
        }

        obiect.setNumeArtist(null);
        obiect.setTematica(null);
        obiect.setGen(null);
        obiect.setCasaDiscuri(null);
    }

    private void validateTablouriFields(Obiect obiect) throws CustomException {
        if (!(obiect.getPretAchizitie()!=null)) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'pretAchizitite' este obligatoriu pentru tablouri"
            );
        }

        if (obiect.getTara() == null || obiect.getTara().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'tara' este obligatoriu pentru tablouri"
            );
        }

        if (obiect.getAn() == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'an' este obligatoriu pentru tablouri"
            );
        }

        if (obiect.getStare() == null || obiect.getStare().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'stare' este obligatoriu pentru tablouri"
            );
        }

        if (obiect.getRaritate() == null || obiect.getRaritate().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'raritate' este obligatoriu pentru tablouri"
            );
        }

        obiect.setMaterial(null);
        obiect.setValoare(0.0f);
        obiect.setGreutate(0);
        obiect.setTematica(null);
        obiect.setGen(null);
        obiect.setCasaDiscuri(null);
    }

    private void validateTimbreFields(Obiect obiect) throws CustomException {
        if (obiect.getNumeArtist() == null || obiect.getNumeArtist().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'nume autor' este obligatoriu pentru timbre"
            );
        }

        if (obiect.getGen() == null || obiect.getGen().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'gen' este obligatoriu pentru timbre"
            );
        }

        if (obiect.getAn() == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'an publicare' este obligatoriu pentru timbre"
            );
        }

        if (obiect.getStare() == null || obiect.getStare().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'stare' este obligatoriu pentru timbre"
            );
        }

        obiect.setMaterial(null);
        obiect.setValoare(0.0f);
        obiect.setGreutate(0);
        obiect.setNumeArtist(null);
        obiect.setGen(null);
        obiect.setCasaDiscuri(null);
    }

    private void validateViniluriFields(Obiect obiect) throws CustomException {
        if (obiect.getNumeArtist() == null || obiect.getNumeArtist().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'nume artist' este obligatoriu pentru viniluri"
            );
        }

        if (obiect.getGen() == null || obiect.getGen().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'gen muzical' este obligatoriu pentru viniluri"
            );
        }

        if (obiect.getCasaDiscuri() == null || obiect.getCasaDiscuri().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'casa de discuri' este obligatoriu pentru viniluri"
            );
        }

        if (obiect.getAn() == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'an lansare' este obligatoriu pentru viniluri"
            );
        }

        if (obiect.getStare() == null || obiect.getStare().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingField",
                    "Câmpul 'stare' este obligatoriu pentru viniluri"
            );
        }

        obiect.setMaterial(null);
        obiect.setValoare(0.0f);
        obiect.setGreutate(0);
        obiect.setTematica(null);
    }

    private void ensureDirectoryExists(Path directoryPath) throws IOException {
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
            Logger.debug("Director creat: " + directoryPath);
        }
    }

    public List<ExplorerObjectDTO> filters(Map<String, String> filtre, List<ExplorerObjectDTO> objects) {
        if (filtre == null || objects == null) {
            Logger.warning("Parametri null la filtrarea obiectelor");
            return new ArrayList<>();
        }

        List<ExplorerObjectDTO> filteredObjects = new ArrayList<>(objects);

        if (filtre.containsKey("colectie")) {
            String tipColectie = filtre.get("colectie");
            Logger.debug("Filtrare după tipul colecției: " + tipColectie);

            filteredObjects = filteredObjects.parallelStream()
                    .filter(e -> e.getTipColectie().equals(tipColectie))
                    .collect(Collectors.toList());
        }

        if (filtre.containsKey("data")) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate date = LocalDate.parse(filtre.get("data"), inputFormatter);
                Logger.debug("Filtrare după data: " + date);

                filteredObjects = filteredObjects.parallelStream()
                        .filter(e -> date.isBefore(e.getCreated()))
                        .sorted(Comparator.comparing(ExplorerObjectDTO::getCreated))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                Logger.warning("Format dată invalid la filtrare: " + filtre.get("data"));
            }
        }

        if (filtre.containsKey("sort")) {
            String sort = filtre.get("sort");
            Logger.debug("Sortare după: " + sort);

            switch (sort) {
                case "populare":
                    filteredObjects = filteredObjects.stream()
                            .sorted(Comparator.comparing(ExplorerObjectDTO::getLikes).reversed())
                            .collect(Collectors.toList());
                    break;
                case "trend":
                    filteredObjects = filteredObjects.stream()
                            .sorted(Comparator.comparing(ExplorerObjectDTO::getViews).reversed())
                            .collect(Collectors.toList());
                    break;
                case "value":
                    filteredObjects = filteredObjects.stream()
                            .sorted(Comparator.comparing(ExplorerObjectDTO::getValue).reversed())
                            .collect(Collectors.toList());
                    break;
                case "date":
                    filteredObjects = filteredObjects.stream()
                            .sorted(Comparator.comparing(ExplorerObjectDTO::getCreated).reversed())
                            .collect(Collectors.toList());
                    break;
                default:
                    Logger.warning("Criteriu de sortare necunoscut: " + sort);
                    break;
            }
        }

        if (filtre.containsKey("name")) {
            String name = filtre.get("name");
            Logger.debug("Filtrare după numele: " + name);

            filteredObjects = filteredObjects.parallelStream()
                    .filter(e -> e.getName().contains(name))
                    .collect(Collectors.toList());
        }

        Logger.debug("După aplicarea filtrelor au rămas " + filteredObjects.size() + " obiecte");
        return filteredObjects;
    }
}