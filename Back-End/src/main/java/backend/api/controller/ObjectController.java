package backend.api.controller;

import backend.api.config.controllerConfig.ControllerInterface;
import backend.api.config.controllerConfig.Response200;
import backend.api.config.controllerConfig.Response400;
import backend.api.config.controllerConfig.Response500;
import backend.api.dataTransferObject.ExplorerObjectDTO;
import backend.api.dataTransferObject.ObjectDTO;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Logger;
import backend.api.model.GlobalStatistics.GeneralStatistics;
import backend.api.service.ObjectService;
import backend.api.service.StatisticsService;
import backend.api.util.files.XmlGeneralStatistics;
import backend.api.util.json.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static backend.api.config.controllerConfig.ControllerInterface.*;
import static backend.api.config.jwtConfig.JwtUtil.getId;


public class ObjectController implements ControllerInterface {

    // Rutele pentru endpoint-uri
    private static final String PATH_PUBLIC_OBJECTS = "/objects";
    private static final String PATH_MY_OBJECTS = "/my-objects";

    // Pattern-uri pentru a extrage ID-urile din URL-uri
    private static final Pattern PUBLIC_OBJECT_ID_PATTERN = Pattern.compile("/objects/(\\d+)$");
    private static final Pattern PUBLIC_OBJECT_LIKE_PATTERN = Pattern.compile("/objects/(\\d+)/like$");
    private static final Pattern MY_COLLECTION_OBJECTS_PATTERN = Pattern.compile("/my-collection/(\\d+)/objects$");
    private static final Pattern MY_COLLECTION_OBJECT_PATTERN = Pattern.compile("/my-collection/(\\d+)/objects/(\\d+)$");
    private static final Pattern COLLECTION_OBJECTS_PATTERN = Pattern.compile("/collection/(\\d+)/objects$");
    private static final Pattern COLLECTION_OBJECT_PATTERN = Pattern.compile("/collection/(\\d+)/objects/(\\d+)$");
    private static final Pattern COLLECTION_OBJECT_LIKE_PATTERN = Pattern.compile("/collection/(\\d+)/objects/(\\d+)/like$");
    private static final Pattern MY_OBJECT_ID_PATTERN = Pattern.compile("/my-objects/(\\d+)$");

    private final ObjectService obiectService;
    private final StatisticsService statisticsService;


    public ObjectController() {
        this.obiectService = new ObjectService();
        this.statisticsService = new StatisticsService();
    }

    @Override
    public void handle(HttpExchange exchange) throws CustomException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String remoteAddress = exchange.getRemoteAddress().toString();

        Logger.request("Object request: " + method + " " + path + " from " + remoteAddress);

        try {
            if (isBasicMethod(exchange, method)) {
                return;
            }

            InputStream is = exchange.getRequestBody();

             if (path.matches("/my-collection/\\d+/objects$") && "GET".equals(method)) {
                Get(exchange, is);
            }
            // 8. GET: /my-collection/{id}/objects/{id1} - returneaza obiectul din colectia cu id-ul id si obiectul cu id-ul id1
            else if (path.matches("/my-collection/\\d+/objects/\\d+$") && "GET".equals(method)) {
                Get(exchange, is);
            }
            // 9. POST: /my-collection/{id}/objects - adauga un obiect nou in colectia cu id-ul id
            else if (path.matches("/my-collection/\\d+/objects$") && "POST".equals(method)) {
                Post(exchange, is);
            }
            // 10. PUT: /my-collection/{id}/objects/{id1} - modifica un obiect in colecția cu id-ul id si id-ul obiectului id1
            else if (path.matches("/my-collection/\\d+/objects/\\d+$") && "PUT".equals(method)) {
                Put(exchange, is);
            }
            // 11. DELETE: /my-collection/{id}/objects/{id1} - sterge obiectul cu id-ul id1 din colectia cu id-ul id
            else if (path.matches("/my-collection/\\d+/objects/\\d+$") && "DELETE".equals(method)) {
                Delete(exchange, is);
            }
            // 4. GET: /collection/{id1}/objects - returneaza toate obiectele din colecția cu id-ul id1
             else if (path.matches("/collection/\\d+/objects$") && "GET".equals(method)) {
                Get(exchange, is);
            }
            // 5. GET: /collection/{id1}/objects/{id2} - returneaza obiectul cu id-ul id2 din colectia cu id-ul id1
            else if (path.matches("/collection/\\d+/objects/\\d+$") && "GET".equals(method)) {
                Get(exchange, is);
            }
            // 6. POST: /collection/{id1}/objects/{id2}/like - adauga un like obiectului
            else if (path.matches("/collection/\\d+/objects/\\d+/like$") && "POST".equals(method)) {
                Post(exchange, is);
            }
            // 7. GET: /my-collection/{id}/objects - returneaza toate obiectele din colectia cu id-ul id a utilizatorului autentificat

            // 12. GET: /my-objects - returneaza toate obiectele utilizatorului
            else if (PATH_MY_OBJECTS.equals(path) && "GET".equals(method)) {
                Get(exchange, is);
            }
            // 13. GET: /my-objects/{id1} - returneaza obiectul cu id-ul id1 al utilizatorului autentificat
            else if (path.matches("/my-objects/\\d+$") && "GET".equals(method)) {
                Get(exchange, is);
            }
            // 14. PUT: /my-objects/{id1} - modifica un obiect cu id-ul obiectului id1
            else if (path.matches("/my-objects/\\d+$") && "PUT".equals(method)) {
                Put(exchange, is);
            }
            // 15. DELETE: /my-objects/{id1} - sterge obiectul cu id-ul id1
            else if (path.matches("/my-objects/\\d+$") && "DELETE".equals(method)) {
                Delete(exchange, is);
            }

            // 1. GET: /objects - returneaza toate obiectele ce au visibility=true
           else  if (PATH_PUBLIC_OBJECTS.equals(path) && "GET".equals(method)) {
                Get(exchange, is);
            }
            // 2. GET: /objects/{id} - returneaza obiectul cu visibility=true si cu id-ul respectiv
            else if (path.matches("/objects/\\d+$") && "GET".equals(method)) {
                Get(exchange, is);
            }
            // 3. POST: /objects/{id}/like - adauga un like obiectului
            else if (path.matches("/objects/\\d+/like$") && "POST".equals(method)) {
                Post(exchange, is);
            }

            else {
                // Daca nu se potriveste cu niciun endpoint valid
                Logger.warning("Endpoint sau metodă nevalidă: " + method + " " + path);
                Response400.sendMethodNotAllowed(exchange,
                        "{\"error\":\"Endpoint sau metodă nevalidă\"}");
            }

            try {
                if (method.equals("POST")) {
                    GeneralStatistics tempStats = new GeneralStatistics();
                    if (path.contains("/like")) {
                        statisticsService.updateTopLikedObjects(tempStats);
                        statisticsService.updateTopLikedCollections(tempStats);
                    } else {
                        statisticsService.updateGeneralObjectStatistics(tempStats);
                    }
                    XmlGeneralStatistics.generateSimpleRssXml(tempStats);
                }
                else if (method.equals("PUT")) {
                    GeneralStatistics tempStats = new GeneralStatistics();
                    statisticsService.updateGeneralObjectStatistics(tempStats);
                    XmlGeneralStatistics.generateSimpleRssXml(tempStats);
                }
                else if (method.equals("DELETE")) {
                    GeneralStatistics tempStats = new GeneralStatistics();
                    statisticsService.updateGeneralObjectStatistics(tempStats);
                    XmlGeneralStatistics.generateSimpleRssXml(tempStats);
                }
            } catch (Exception e) {
                Logger.warning("Eroare la actualizarea statisticilor pentru obiecte: " + e.getMessage());
            }
        }
        catch (CustomException e) {
            handleException(exchange, e);
        }
        catch (Exception e) {
            Logger.exception("UnexpectedException");
            Logger.error("Eroare neașteptată în ObjectController: " + e.getMessage());
            Response500.sendInternalServerError(exchange,
                    "{\"error\":\"Eroare internă: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public void Get(HttpExchange exchange, InputStream is) throws  CustomException {
        try {
            String path = exchange.getRequestURI().getPath();
            Logger.info("Procesare cerere GET pentru obiecte, path: " + path);

            Long userId = null;
            try {
                userId = getId(getToken(exchange));
            } catch (Exception e) {
                Logger.debug("Utilizator neautentificat pentru cererea: " + path);
            }

            // 4. GET: /collection/{id1}/objects - returneaza toate obiectele din colectia cu id-ul id1
            Matcher collectionObjectsMatcher = COLLECTION_OBJECTS_PATTERN.matcher(path);
            if (collectionObjectsMatcher.find()) {
                if (userId == null) {
                    throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                            "Trebuie să fiți autentificat pentru a vizualiza obiectele colecției");
                }

                Long collectionId = Long.parseLong(collectionObjectsMatcher.group(1));
                Map<String, Object> response = obiectService.getCollectionObjects(collectionId);

                Logger.success("Obiecte din colecție obținute cu succes");
                Response200.sendOk(exchange, JsonUtil.toJson(response));
                return;
            }

            // 5. GET: /collection/{id1}/objects/{id2} - returneaza obiectul cu id-ul id2 din colectia cu id-ul id1
            Matcher collectionObjectMatcher = COLLECTION_OBJECT_PATTERN.matcher(path);
            if (collectionObjectMatcher.find()) {
                if (userId == null) {
                    throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                            "Trebuie să fiți autentificat pentru a vizualiza obiectul din colecție");
                }

                Long collectionId = Long.parseLong(collectionObjectMatcher.group(1));
                Long obiectId = Long.parseLong(collectionObjectMatcher.group(2));
                ObjectDTO obiect = obiectService.getCollectionObjectById(collectionId, obiectId, userId);

                Logger.success("Obiect din colecție obținut cu succes");
                Response200.sendOk(exchange,
                        "{\"object\":" + JsonUtil.toJson(obiect) + "}");
                return;
            }

            if (userId == null) {
                throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                        "Trebuie să fiți autentificat pentru a accesa acest endpoint");
            }

            // 7. GET: /my-collection/{id}/objects - returneaza toate obiectele din colectia utilizatorului
            Matcher myCollectionObjectsMatcher = MY_COLLECTION_OBJECTS_PATTERN.matcher(path);
            if (myCollectionObjectsMatcher.find()) {
                Long collectionId = Long.parseLong(myCollectionObjectsMatcher.group(1));
                Map<String, Object> response = obiectService.getMyCollectionObjects(userId, collectionId);

                Logger.success("Obiecte din colecția proprie obținute cu succes");
                Response200.sendOk(exchange, JsonUtil.toJson(response) );
                return;
            }

            // 8. GET: /my-collection/{id}/objects/{id1} - returneaza obiectul din colectia utilizatorului
            Matcher myCollectionObjectMatcher = MY_COLLECTION_OBJECT_PATTERN.matcher(path);
            if (myCollectionObjectMatcher.find()) {
                Long collectionId = Long.parseLong(myCollectionObjectMatcher.group(1));
                Long obiectId = Long.parseLong(myCollectionObjectMatcher.group(2));
                ObjectDTO obiect = obiectService.getMyCollectionObjectById(userId, collectionId, obiectId);

                Logger.success("Obiect din colecția proprie obținut cu succes");
                Response200.sendOk(exchange,
                        "{\"myObject\":" + JsonUtil.toJson(obiect) + "}");
                return;
            }

            // 1. GET: /objects - returneaza toate obiectele ce au visibility=true
            if (PATH_PUBLIC_OBJECTS.equals(path)) {
                if (userId == null) {
                    throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                            "Trebuie să fiți autentificat pentru a vizualiza obiectele publice");
                }

                List<ExplorerObjectDTO> response = obiectService.getAllPublicObjects();

                URI uri = exchange.getRequestURI();
                if (uri.getQuery() != null) {
                    Logger.info("Aplicare filtre pentru obiecte: " + uri.getQuery());
                    response = obiectService.filters(queryToMap(uri.getQuery()), response);
                }

                Logger.success("Obiecte publice obținute cu succes: " + response.size() + " obiecte");
                Response200.sendOk(exchange,
                        "{\"objects\":" + JsonUtil.toJson(response) + "}");
                return;
            }

            // 2. GET: /objects/{id} - returneaza obiectul cu visibility=true si id-ul respectiv
            Matcher publicObjectMatcher = PUBLIC_OBJECT_ID_PATTERN.matcher(path);
            if (publicObjectMatcher.find()) {
                if (userId == null) {
                    throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                            "Trebuie să fiți autentificat pentru a vizualiza obiectul");
                }

                Long obiectId = Long.parseLong(publicObjectMatcher.group(1));
                ObjectDTO obiect = obiectService.getPublicObjectById(obiectId, userId);

                Logger.success("Obiect public obținut cu succes");
                Response200.sendOk(exchange,
                        "{\"object\":" + JsonUtil.toJson(obiect) + "}");
                return;
            }



            // 12. GET: /my-objects - returneaza toate obiectele utilizatorului
           else  if (PATH_MY_OBJECTS.equals(path)) {
                Map<String, Object> response = obiectService.getAllMyObjects(userId);

                Logger.success("Obiectele utilizatorului obținute cu succes");
                Response200.sendOk(exchange,JsonUtil.toJson(response) );
                return;
            }

            // 13. GET: /my-objects/{id1} - returneaza obiectul cu id-ul id1 al utilizatorului
            Matcher myObjectMatcher = MY_OBJECT_ID_PATTERN.matcher(path);
            if (myObjectMatcher.find()) {
                Long obiectId = Long.parseLong(myObjectMatcher.group(1));
                ObjectDTO obiect = obiectService.getMyObjectById(userId, obiectId);

                Logger.success("Obiect propriu obținut cu succes");
                Response200.sendOk(exchange,
                        "{\"myObject\":" + JsonUtil.toJson(obiect) + "}");
                return;
            }

            throw new Exception400.BadRequestException("InvalidEndpoint", "Endpoint invalid",
                    "Endpoint-ul specificat nu este valid pentru operațiuni cu obiecte");
        }
        catch (CustomException e) {
            throw new CustomException("ObjectRetrievalError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("ObjectRetrievalError", "Eroare la obținerea obiectelor", e);
        }
    }

    @Override
    public void Post(HttpExchange exchange, InputStream is) throws  CustomException {
        try {
            String path = exchange.getRequestURI().getPath();
            Logger.info("Procesare cerere POST pentru obiecte, path: " + path);

            Long userId = null;
            try {
                userId = getId(getToken(exchange));
            } catch (Exception e) {
                Logger.debug("Utilizator neautentificat pentru cererea: " + path);
            }

            // 3. POST: /objects/{id}/like - adauga un like obiectului
            Matcher objectLikeMatcher = PUBLIC_OBJECT_LIKE_PATTERN.matcher(path);
            if (objectLikeMatcher.find()) {
                if (userId == null) {
                    throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                            "Trebuie să fiți autentificat pentru a aprecia obiectul");
                }

                Long obiectId = Long.parseLong(objectLikeMatcher.group(1));
                Map<String, Object> response = obiectService.likeObject(obiectId, userId);

                Logger.success("Apreciere adăugată cu succes la obiect");
                Response200.sendOk(exchange,JsonUtil.toJson(response) );
                return;
            }

            // 6. POST: /collection/{id1}/objects/{id2}/like - adauga un like obiectului din colectie
            Matcher collectionObjectLikeMatcher = COLLECTION_OBJECT_LIKE_PATTERN.matcher(path);
            if (collectionObjectLikeMatcher.find()) {
                if (userId == null) {
                    throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                            "Trebuie să fiți autentificat pentru a aprecia obiectul din colecție");
                }

                Long collectionId = Long.parseLong(collectionObjectLikeMatcher.group(1));
                Long obiectId = Long.parseLong(collectionObjectLikeMatcher.group(2));
                Map<String, Object> response = obiectService.likeCollectionObject(collectionId, obiectId, userId);

                Logger.success("Apreciere adăugată cu succes la obiectul din colecție");
                Response200.sendOk(exchange, JsonUtil.toJson(response) );
                return;
            }

            if (userId == null) {
                throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                        "Trebuie să fiți autentificat pentru a accesa acest endpoint");
            }

            // 9. POST: /my-collection/{id}/objects - adauga un obiect nou in colectia utilizatorului
            Matcher myCollectionMatcher = MY_COLLECTION_OBJECTS_PATTERN.matcher(path);
            if (myCollectionMatcher.find()) {
                Long collectionId = Long.parseLong(myCollectionMatcher.group(1));
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

                Map<String, Object> response = obiectService.createObject(userId, collectionId, is, contentType);

                Logger.success("Obiect creat cu succes în colecție");
                Response200.sendCreated(exchange, JsonUtil.toJson(response) );
                return;
            }

            throw new Exception400.BadRequestException("InvalidEndpoint", "Endpoint invalid",
                    "Endpoint-ul specificat nu este valid pentru operațiunea POST");
        }
        catch (CustomException e) {
            throw new CustomException("ObjectCreateError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("ObjectCreateError", "Eroare la procesarea cererii", e);
        }
    }

    @Override
    public void Put(HttpExchange exchange, InputStream is) throws  CustomException {
        try {
            String path = exchange.getRequestURI().getPath();
            Logger.info("Procesare cerere PUT pentru obiecte, path: " + path);

            Long userId;
            try {
                userId = getId(getToken(exchange));
            } catch (Exception e) {
                throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                        "Trebuie să fiți autentificat pentru a actualiza obiecte");
            }

            // 10. PUT: /my-collection/{id}/objects/{id1} - modifica un obiect in colectia utilizatorului
            Matcher myCollectionObjectMatcher = MY_COLLECTION_OBJECT_PATTERN.matcher(path);
            if (myCollectionObjectMatcher.find()) {
                Long collectionId = Long.parseLong(myCollectionObjectMatcher.group(1));
                Long obiectId = Long.parseLong(myCollectionObjectMatcher.group(2));
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

                Map<String, Object> response = obiectService.updateObject(userId, collectionId, obiectId, is, contentType);

                Logger.success("Obiect actualizat cu succes");
                Response200.sendOk(exchange,JsonUtil.toJson(response) );
                return;
            }

            // 14. PUT: /my-objects/{id1} - modifica un obiect direct fara a specifica colectia
            Matcher myObjectMatcher = MY_OBJECT_ID_PATTERN.matcher(path);
            if (myObjectMatcher.find()) {
                Long obiectId = Long.parseLong(myObjectMatcher.group(1));
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

                Map<String, Object> response = obiectService.updateMyObject(userId, obiectId, is, contentType);

                Logger.success("Obiect propriu actualizat cu succes");
                Response200.sendOk(exchange,JsonUtil.toJson(response) );
                return;
            }

            throw new Exception400.BadRequestException("InvalidEndpoint", "Endpoint invalid",
                    "Endpoint-ul specificat nu este valid pentru operațiunea PUT");
        }
        catch (CustomException e) {
            throw new CustomException("ObjecUpdateError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("ObjectUpdateError", "Eroare la actualizarea obiectului", e);
        }
    }

    @Override
    public void Patch(HttpExchange exchange, InputStream is) throws CustomException {
        Logger.warning("Metodă PATCH neimplementată pentru ObjectController");
        Response400.sendMethodNotAllowed(exchange,
                "{\"error\":\"Metoda PATCH nu este suportată pentru obiecte\"}");
    }

    @Override
    public void Delete(HttpExchange exchange, InputStream is) throws  CustomException {
        try {
            String path = exchange.getRequestURI().getPath();
            Logger.info("Procesare cerere DELETE pentru obiecte, path: " + path);

            Long userId;
            try {
                userId = getId(getToken(exchange));
            } catch (Exception e) {
                throw new Exception400.UnauthorizedException("AuthenticationError", "Autentificare necesară",
                        "Trebuie să fiți autentificat pentru a șterge obiecte");
            }

            // 11. DELETE: /my-collection/{id}/objects/{id1} - sterge obiectul din colectia utilizatorului
            Matcher myCollectionObjectMatcher = MY_COLLECTION_OBJECT_PATTERN.matcher(path);
            if (myCollectionObjectMatcher.find()) {
                Long collectionId = Long.parseLong(myCollectionObjectMatcher.group(1));
                Long obiectId = Long.parseLong(myCollectionObjectMatcher.group(2));

                Map<String, Object> response = obiectService.deleteObject(userId, collectionId, obiectId);

                Logger.success("Obiect șters cu succes din colecție");
                Response200.sendOk(exchange, JsonUtil.toJson(response) );
                return;
            }

            // 15. DELETE: /my-objects/{id1} - sterge obiectul direct fara a specifica colectia
            Matcher myObjectMatcher = MY_OBJECT_ID_PATTERN.matcher(path);
            if (myObjectMatcher.find()) {
                Long obiectId = Long.parseLong(myObjectMatcher.group(1));

                Map<String, Object> response = obiectService.deleteMyObject(userId, obiectId);

                Logger.success("Obiect propriu șters cu succes");
                Response200.sendOk(exchange, JsonUtil.toJson(response) );
                return;
            }

            throw new Exception400.BadRequestException("InvalidEndpoint", "Endpoint invalid",
                    "Endpoint-ul specificat nu este valid pentru operațiunea DELETE");
        }
        catch (CustomException e) {
            throw new CustomException("ObjectDeleteError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("ObjectDeleteError", "Eroare la ștergerea obiectului", e);
        }
    }
}