package backend.api.service;

import backend.api.dataTransferObject.CollectionDTO;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.mapper.CollectionMapper;
import backend.api.model.AtributeObiecte;
import backend.api.model.Collection;
import backend.api.model.CustomCollection;
import backend.api.model.Obiect;
import backend.api.repository.CollectionRepository;
import backend.api.repository.CustomCollectionRepository;
import backend.api.repository.ObjectRepository;
import backend.api.repository.ObjectLikeRepository;
import backend.api.repository.ObjectViewRepository;
import backend.api.util.json.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final CustomCollectionRepository customCollectionRepository;
    private final ObjectRepository obiectRepository;
    private final ObjectLikeRepository objectLikeRepository;
    private final ObjectViewRepository objectViewRepository;

    public CollectionService() {
        this.collectionRepository = new CollectionRepository();
        this.customCollectionRepository = new CustomCollectionRepository();
        this.obiectRepository = new ObjectRepository();
        this.objectLikeRepository = new ObjectLikeRepository();
        this.objectViewRepository = new ObjectViewRepository();
    }

    private Long calculateTotalLikes(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "ParameterError",
                    "InvalidCollectionId",
                    "ID colecție invalid pentru calcularea numărului de aprecieri"
            );
        }



            List<Obiect> objects = obiectRepository.findAllByCollectionId(collectionId);

        Long totalLikes = objects.parallelStream()
                .mapToLong(obiect -> objectLikeRepository.getLikeCount(obiect.getId()))
                .sum();

            Logger.debug("Număr total de aprecieri pentru colecția " + collectionId + ": " + totalLikes);
            return totalLikes;

    }

    private Long calculateTotalViews(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "ParameterError",
                    "InvalidCollectionId",
                    "ID colecție invalid pentru calcularea numărului de vizualizări"
            );
        }



            List<Obiect> objects = obiectRepository.findAllByCollectionId(collectionId);

        Long totalViews = objects.parallelStream()
                .mapToLong(obiect -> objectViewRepository.getViewCount(obiect.getId()))
                .sum();

            Logger.debug("Număr total de vizualizări pentru colecția " + collectionId + ": " + totalViews);
            return totalViews;

    }

    private Map<String, Object> collectionToMapWithStats(Collection collection) throws CustomException {
        if (collection == null) {
            throw new Exception500.InternalServerErrorException(
                    "ParameterError",
                    "NullCollection",
                    "Colecție invalidă pentru convertire"
            );
        }



            Map<String, Object> collectionMap = new HashMap<>();
            collectionMap.put("id", collection.getId());
            collectionMap.put("idUser", collection.getIdUser());
            collectionMap.put("idTip", collection.getIdTip());
            collectionMap.put("nume", collection.getNume());
            collectionMap.put("visibility", collection.getVisibility());
            collectionMap.put("createdAt", collection.getCreatedAt().toLocalDateTime().toLocalDate());

            if (collection.getCustomFields() != null) {
                Map<String, Object> customFieldsMap = new HashMap<>();
                CustomCollection customFields = collection.getCustomFields();
                customFieldsMap.put("material", customFields.getMaterial());
                customFieldsMap.put("valoare", customFields.getValoare());
                customFieldsMap.put("greutate", customFields.getGreutate());
                customFieldsMap.put("numeArtist", customFields.getNumeArtist());
                customFieldsMap.put("tematica", customFields.getTematica());
                customFieldsMap.put("gen", customFields.getGen());
                customFieldsMap.put("casaDiscuri", customFields.getCasaDiscuri());
                customFieldsMap.put("tara", customFields.getTara());
                customFieldsMap.put("an", customFields.getAn());
                customFieldsMap.put("stare", customFields.getStare());
                customFieldsMap.put("raritate", customFields.getRaritate());
                customFieldsMap.put("pretAchizitie", customFields.getPretAchizitie());
                collectionMap.put("customFields", customFieldsMap);
            }

            collectionMap.put("total_likes", calculateTotalLikes(collection.getId()));
            collectionMap.put("total_views", calculateTotalViews(collection.getId()));
            collectionMap.put("count", collection.getCount());
            collectionMap.put("value", collection.getValue());


            Logger.debug("Colecție convertită cu succes în Map cu statistici");
            return collectionMap;

    }

    private List<Map<String, Object>> objectsToMapWithImages(List<Obiect> objects) throws CustomException {
        if (objects == null) {
            throw new Exception500.InternalServerErrorException(
                    "ParameterError",
                    "NullObjectsList",
                    "Listă de obiecte invalidă pentru convertire"
            );
        }

        List<Map<String, Object>> objectMaps = objects.parallelStream().map(obiect -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", obiect.getId());
            map.put("id_colectie", obiect.getIdColectie());
            map.put("name", obiect.getName());
            map.put("descriere", obiect.getDescriere());
            map.put("pret_achizitie", obiect.getPretAchizitie());

            String imagePath = obiect.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    Path file = Path.of(imagePath);
                    if (Files.exists(file)) {
                        byte[] imageBytes = Files.readAllBytes(file);
                        map.put("image", Base64.getEncoder().encodeToString(imageBytes));
                        map.put("image_name", file.getFileName().toString());
                        Logger.debug("Imagine procesată cu succes pentru obiectul cu ID: " + obiect.getId());
                    } else {
                        Logger.warning("Fișierul imagine nu există la calea: " + imagePath);
                        map.put("image_path", imagePath);
                    }
                } catch (IOException e) {
                    Logger.warning("Nu s-a putut citi imaginea pentru obiectul cu ID " + obiect.getId() + ": " + e.getMessage());
                    map.put("image_path", imagePath);
                }
            }

            map.put("views", objectViewRepository.getViewCount(obiect.getId()));
            map.put("likes", objectLikeRepository.getLikeCount(obiect.getId()));
            return map;
        }).toList();
        Logger.debug("Obiecte convertite cu succes în Map-uri cu imagini");
        return objectMaps;
    }

    public List<CollectionDTO> getAllPublicCollections() throws CustomException {
        try {
            List<Collection> collections = collectionRepository.findAllPublic();
            List<Long> ids = collections.stream().map(Collection::getId).collect(Collectors.toList());

            Map<Long, Long> likesMap = collectionRepository.getLikesForCollectionIds(ids);
            Map<Long, Long> viewsMap = collectionRepository.getViewsForCollectionIds(ids);
            Map<Long, Double> valueMap = collectionRepository.getValueForCollectionIds(ids);
            Map<Long, Long> countMap = collectionRepository.getCountForCollectionIds(ids);
            Map<Long, String> usernameMap = collectionRepository.getUsernamesForCollectionIds(ids);

            List<CollectionDTO> dtos = collections.stream().map(collection -> {
                long likes = likesMap.getOrDefault(collection.getId(), 0L);
                long views = viewsMap.getOrDefault(collection.getId(), 0L);
                double value = valueMap.getOrDefault(collection.getId(), 0.0);
                long count = countMap.getOrDefault(collection.getId(), 0L);
                String username = usernameMap.getOrDefault(collection.getId(), "");
                return CollectionMapper.toDTO(collection, likes, views, value, username, count);
            }).collect(Collectors.toList());

            Logger.success("S-au obținut " + dtos.size() + " colecții publice");
            return dtos;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "ServiceError",
                    "UnexpectedException",
                    "Eroare neașteptată la obținerea colecțiilor publice",
                    e
            );
        }
    }

    public Map<String, Object> getMyCollections(Long userId) throws CustomException {
        if (userId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidUserId",
                    "ID utilizator invalid"
            );
        }


        List<Collection> collections = collectionRepository.findAllByUserId(userId);
        List<Long> ids = collections.stream().map(Collection::getId).toList();

        Map<Long, Long> likesMap = collectionRepository.getLikesForCollectionIds(ids);
        Map<Long, Long> viewsMap = collectionRepository.getViewsForCollectionIds(ids);
        Map<Long, Long> countMap = collectionRepository.getCountForCollectionIds(ids);
        Map<Long, Double> valueMap = collectionRepository.getValueForCollectionIds(ids);

        List<Map<String, Object>> collectionsWithStats = collections.stream().map(collection -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", collection.getId());
            map.put("idUser", collection.getIdUser());
            map.put("idTip", collection.getIdTip());
            map.put("nume", collection.getNume());
            map.put("visibility", collection.getVisibility());
            map.put("createdAt", collection.getCreatedAt().toLocalDateTime().toLocalDate());
            map.put("total_likes", likesMap.getOrDefault(collection.getId(), 0L));
            map.put("total_views", viewsMap.getOrDefault(collection.getId(), 0L));
            map.put("count", countMap.getOrDefault(collection.getId(), 0L));
            map.put("value", valueMap.getOrDefault(collection.getId(), 0.0));
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("collections", collectionsWithStats);

        Logger.success("S-au obținut " + collections.size() + " colecții pentru utilizatorul cu ID: " + userId);
        return response;
    }

    public Map<String, Object> getPublicCollectionById(Long collectionId) throws CustomException {
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

        if (collection.isCustomType()) {
            Optional<CustomCollection> customFields = customCollectionRepository.findByCollectionId(collection.getId());
            customFields.ifPresent(collection::setCustomFields);
        }
        else if (collection.isMonedeType())
        {
            collection.setCustomFields(AtributeObiecte.getCustomCollection(AtributeObiecte.getMonede()));
        }
        else if (collection.isTablouriType())
        {
            collection.setCustomFields(AtributeObiecte.getCustomCollection(AtributeObiecte.getTablouri()));
        }

        else if (collection.isTimbreType())
        {
            collection.setCustomFields(AtributeObiecte.getCustomCollection(AtributeObiecte.getTimbre()));
        }

        else if (collection.isVinilType())
        {
            collection.setCustomFields(AtributeObiecte.getCustomCollection(AtributeObiecte.getViniluri()));
        }

        collection.setCount(collectionRepository.countObjects(collectionId));
        collection.setValue(collectionRepository.getValue(collection.getId()));


        List<Obiect> objects = obiectRepository.findAllByCollectionId(collectionId).parallelStream().filter(obiect->obiect.getVisibility().equals(true)).collect(Collectors.toList());
        List<Map<String, Object>> objectMaps = objectsToMapWithImages(objects);

        Map<String, Object> response = new HashMap<>();
        response.put("collection", collectionToMapWithStats(collection));
        response.put("objects", objectMaps);

        Logger.success("Colecția publică cu ID " + collectionId + " a fost obținută cu succes");
        return response;
    }

    public Map<String, Object> getMyCollectionById(Long userId, Long collectionId) throws CustomException {
        if (userId == null || collectionId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID utilizator sau ID colecție invalid"
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

        if (!collection.getIdUser().equals(userId)) {
            throw new Exception400.ForbiddenException(
                    "AccessDenied",
                    "NotOwner",
                    "Nu aveți acces la această colecție"
            );
        }

        if (collection.isCustomType()) {
            Optional<CustomCollection> customFields = customCollectionRepository.findByCollectionId(collection.getId());
            customFields.ifPresent(collection::setCustomFields);
        }
        else if (collection.isMonedeType())
        {
            collection.setCustomFields(AtributeObiecte.getCustomCollection(AtributeObiecte.getMonede()));
        }
        else if (collection.isTablouriType())
        {
            collection.setCustomFields(AtributeObiecte.getCustomCollection(AtributeObiecte.getTablouri()));
        }

        else if (collection.isTimbreType())
        {
            collection.setCustomFields(AtributeObiecte.getCustomCollection(AtributeObiecte.getTimbre()));
        }

        else if (collection.isVinilType())
        {
            collection.setCustomFields(AtributeObiecte.getCustomCollection(AtributeObiecte.getViniluri()));
        }

        collection.setCount(collectionRepository.countObjects(collectionId));
        collection.setValue(collectionRepository.getValue(collection.getId()));

        List<Obiect> objects = obiectRepository.findAllByCollectionId(collectionId);
        List<Map<String, Object>> objectMaps = objectsToMapWithImages(objects);

        Map<String, Object> response = new HashMap<>();
        response.put("collection", collectionToMapWithStats(collection));
        response.put("objects", objectMaps);

        Logger.success("Colecția proprie cu ID " + collectionId + " a fost obținută cu succes pentru utilizatorul cu ID " + userId);
        return response;
    }

    public Map<String, Object> createCollection(Long userId, InputStream is) throws CustomException {
        if (userId == null || is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID utilizator sau date colecție invalide"
            );
        }


        try {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            Map<String, Object> requestData;
            requestData = JsonUtil.fromJson(requestBody, Map.class);

            String nume = (String) requestData.get("nume");
            int idTip = ((Number) requestData.get("idTip")).intValue();
            Boolean visibility = (Boolean) requestData.getOrDefault("visibility", false);

            if (nume == null || nume.trim().isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidName",
                        "Numele colecției este obligatoriu"
                );
            }

            if (idTip < 1 || idTip > 5) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidType",
                        "Tipul colecției este invalid"
                );
            }

            Collection collection = new Collection(userId, idTip, nume, visibility);
            collection.setCreatedAt(Timestamp.from(Instant.now()));

            Long collectionId = collectionRepository.save(collection);
            collection.setId(collectionId);

            if (collection.isCustomType() && requestData.containsKey("customFields")) {
                Map<String, Boolean> customFieldsData = (Map<String, Boolean>) requestData.get("customFields");
                CustomCollection customFields = new CustomCollection();
                customFields.setIdColectie(collectionId);

                customFields.setMaterial(getBooleanOrFalse(customFieldsData, "material"));
                customFields.setValoare(getBooleanOrFalse(customFieldsData, "valoare"));
                customFields.setGreutate(getBooleanOrFalse(customFieldsData, "greutate"));
                customFields.setNumeArtist(getBooleanOrFalse(customFieldsData, "numeArtist"));
                customFields.setTematica(getBooleanOrFalse(customFieldsData, "tematica"));
                customFields.setGen(getBooleanOrFalse(customFieldsData, "gen"));
                customFields.setCasaDiscuri(getBooleanOrFalse(customFieldsData, "casaDiscuri"));
                customFields.setTara(getBooleanOrFalse(customFieldsData, "tara"));
                customFields.setAn(getBooleanOrFalse(customFieldsData, "an"));
                customFields.setStare(getBooleanOrFalse(customFieldsData, "stare"));
                customFields.setRaritate(getBooleanOrFalse(customFieldsData, "raritate"));
                customFields.setPretAchizitie(getBooleanOrFalse(customFieldsData, "pretAchizitie"));

                customCollectionRepository.save(customFields);
                collection.setCustomFields(customFields);

                Logger.debug("Câmpuri personalizate salvate pentru colecția cu ID: " + collectionId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("collection", collectionToMapWithStats(collection));

            Logger.success("Colecția cu ID " + collectionId + " a fost creată cu succes pentru utilizatorul cu ID " + userId);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<String, Object> updateCollection(Long userId, Long collectionId, InputStream is) throws CustomException {
        if (userId == null || collectionId == null || is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID utilizator, ID colecție sau date colecție invalide"
            );
        }


        try {
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

            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            Map<String, Object> requestData = JsonUtil.fromJson(requestBody, Map.class);

            if (requestData.containsKey("nume")) {
                String nume = (String) requestData.get("nume");
                if (nume == null || nume.trim().isEmpty()) {
                    throw new Exception400.BadRequestException(
                            "ValidationError",
                            "InvalidName",
                            "Numele colecției nu poate fi gol"
                    );
                }
                collection.setNume(nume);
                Logger.debug("Numele colecției cu ID " + collectionId + " actualizat la: " + nume);
            }

            if (requestData.containsKey("visibility")) {
                collection.setVisibility((Boolean)requestData.get("visibility"));
                Logger.debug("Vizibilitatea colecției cu ID " + collectionId + " actualizată la: " + collection.getVisibility());
            }

            collectionRepository.update(collection);

            if (collection.isCustomType() && requestData.containsKey("customFields")) {
                Map<String, Boolean> customFieldsData = (Map<String, Boolean>) requestData.get("customFields");

                Optional<CustomCollection> existingCustomFields = customCollectionRepository.findByCollectionId(collectionId);
                CustomCollection customFields;

                if (existingCustomFields.isPresent()) {
                    customFields = existingCustomFields.get();
                    Logger.debug("Câmpuri personalizate existente găsite pentru colecția cu ID: " + collectionId);
                } else {
                    customFields = new CustomCollection();
                    customFields.setIdColectie(collectionId);
                    Logger.debug("Creare câmpuri personalizate noi pentru colecția cu ID: " + collectionId);
                }

                updateCustomFieldsIfPresent(customFields, customFieldsData);

                if (existingCustomFields.isPresent()) {
                    customCollectionRepository.update(customFields);
                    Logger.debug("Câmpuri personalizate actualizate pentru colecția cu ID: " + collectionId);
                } else {
                    customCollectionRepository.save(customFields);
                    Logger.debug("Câmpuri personalizate noi salvate pentru colecția cu ID: " + collectionId);
                }

                collection.setCustomFields(customFields);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("collection", collectionToMapWithStats(collection));

            Logger.success("Colecția cu ID " + collectionId + " a fost actualizată cu succes pentru utilizatorul cu ID " + userId);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    private void updateCustomFieldsIfPresent(CustomCollection customFields, Map<String, Boolean> customFieldsData) {
        if (customFieldsData.containsKey("material")) {
            customFields.setMaterial(customFieldsData.get("material"));
        }
        if (customFieldsData.containsKey("valoare")) {
            customFields.setValoare(customFieldsData.get("valoare"));
        }
        if (customFieldsData.containsKey("greutate")) {
            customFields.setGreutate(customFieldsData.get("greutate"));
        }
        if (customFieldsData.containsKey("numeArtist")) {
            customFields.setNumeArtist(customFieldsData.get("numeArtist"));
        }
        if (customFieldsData.containsKey("tematica")) {
            customFields.setTematica(customFieldsData.get("tematica"));
        }
        if (customFieldsData.containsKey("gen")) {
            customFields.setGen(customFieldsData.get("gen"));
        }
        if (customFieldsData.containsKey("casaDiscuri")) {
            customFields.setCasaDiscuri(customFieldsData.get("casaDiscuri"));
        }
        if (customFieldsData.containsKey("tara")) {
            customFields.setTara(customFieldsData.get("tara"));
        }
        if (customFieldsData.containsKey("an")) {
            customFields.setAn(customFieldsData.get("an"));
        }
        if (customFieldsData.containsKey("stare")) {
            customFields.setStare(customFieldsData.get("stare"));
        }
        if (customFieldsData.containsKey("raritate")) {
            customFields.setRaritate(customFieldsData.get("raritate"));
        }
        if (customFieldsData.containsKey("pretAchizitie")) {
            customFields.setPretAchizitie(customFieldsData.get("pretAchizitie"));
        }
    }

    public Map<String, Object> deleteCollection(Long userId, Long collectionId) throws CustomException {
        if (userId == null || collectionId == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidParameters",
                    "ID utilizator sau ID colecție invalid"
            );
        }

            Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);
            if (collectionOpt.isEmpty()) {
                Logger.exception("NotFoundException");
                Logger.error("Colecția cu ID " + collectionId + " nu a fost găsită");
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
                customCollectionRepository.deleteByCollectionId(collectionId);
                Logger.debug("Câmpuri personalizate șterse pentru colecția cu ID: " + collectionId);
            }

            obiectRepository.deleteAllByCollectionId(collectionId);
            Logger.debug("Obiecte șterse pentru colecția cu ID: " + collectionId);

            collectionRepository.delete(collectionId);
            Logger.debug("Colecția cu ID " + collectionId + " a fost ștearsă");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Colecția a fost ștearsă cu succes");

            Logger.success("Colecția cu ID " + collectionId + " a fost ștearsă cu succes pentru utilizatorul cu ID " + userId);
            return response;
        }

    private Boolean getBooleanOrFalse(Map<String, Boolean> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return false;
    }

    public List<CollectionDTO> filters(Map<String, String> filtre, List<CollectionDTO> collections) {
        if (filtre == null || collections == null) {
            Logger.warning("Parametri null la filtrarea colecțiilor");
            if (collections == null) throw new AssertionError();
            return new ArrayList<>(collections);
        }

        List<CollectionDTO> filteredCollections = new ArrayList<>(collections);

        if (filtre.containsKey("colectie")) {
            String tipColectie = filtre.get("colectie");
            Logger.debug("Filtrare după tipul colecției: " + tipColectie);
            filteredCollections = filteredCollections.parallelStream()
                    .filter(e -> e.getTipColectie().equals(tipColectie))
                    .collect(Collectors.toList());
        }

        if (filtre.containsKey("data")) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate date = LocalDate.parse(filtre.get("data"), inputFormatter);
                Logger.debug("Filtrare după data: " + date);

                filteredCollections = filteredCollections.parallelStream()
                        .filter(e -> date.isBefore(e.getCreated()))
                        .sorted(Comparator.comparing(CollectionDTO::getCreated))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                Logger.warning("Format dată invalid la filtrare: " + filtre.get("data"));
            }
        }

        if (filtre.containsKey("sort")) {
            String sort = filtre.get("sort");
            Logger.debug("Sortare după: " + sort);

            List<CollectionDTO> sortedList = new ArrayList<>(filteredCollections);

            switch (sort) {
                case "populare":
                    sortedList.sort(Comparator.comparing(CollectionDTO::getLikes).reversed());
                    break;
                case "trend":
                    sortedList.sort(Comparator.comparing(CollectionDTO::getView).reversed());
                    break;
                case "value":
                    sortedList.sort(Comparator.comparing(CollectionDTO::getValue).reversed());
                    break;
                case "date":
                    sortedList.sort(Comparator.comparing(CollectionDTO::getCreated).reversed());
                    break;
                default:
                    Logger.warning("Criteriu de sortare necunoscut: " + sort);
                    break;
            }

            filteredCollections = sortedList;
        }

        if (filtre.containsKey("name")) {
            String name = filtre.get("name");
            Logger.debug("Filtrare după numele: " + name);
            filteredCollections = filteredCollections.parallelStream()
                    .filter(e -> e.getName().contains(name))
                    .toList();
        }

        Logger.debug("După aplicarea filtrelor au rămas " + filteredCollections.size() + " colecții");
        return filteredCollections;
    }
}