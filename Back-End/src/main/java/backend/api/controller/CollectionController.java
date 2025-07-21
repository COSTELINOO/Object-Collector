package backend.api.controller;

import backend.api.config.controllerConfig.ControllerInterface;
import backend.api.config.controllerConfig.Response200;
import backend.api.config.controllerConfig.Response400;
import backend.api.config.controllerConfig.Response500;
import backend.api.dataTransferObject.CollectionDTO;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Logger;
import backend.api.service.CollectionService;
import backend.api.service.StatisticsService;
import backend.api.util.json.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static backend.api.config.controllerConfig.ControllerInterface.*;
import static backend.api.config.jwtConfig.JwtUtil.getId;


public class CollectionController implements ControllerInterface {

    // Rutele pentru endpoint-uri
    private static final String PATH_PUBLIC_COLLECTIONS = "/all-collection";
    private static final String PATH_PUBLIC_COLLECTION_BY_ID = "/all-collection/";
    private static final String PATH_MY_COLLECTIONS = "/user-collection";
    private static final String PATH_MY_COLLECTION_BY_ID = "/user-collection/";

    // Pattern pentru a extrage ID-ul din URL
    private static final Pattern ID_PATTERN = Pattern.compile("/(?:all-collection|user-collection)/(\\d+)$");

    private final CollectionService collectionService;
    private final StatisticsService statisticsService;

    public CollectionController() {
        this.collectionService = new CollectionService();
        this.statisticsService = new StatisticsService();
    }

    @Override
    public void handle(HttpExchange exchange) throws CustomException  {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String remoteAddress = exchange.getRemoteAddress().toString();

        Logger.request("Collection request: " + method + " " + path + " from " + remoteAddress);

        try {
            if (isBasicMethod(exchange, method)) {
                return;
            }

            InputStream is = exchange.getRequestBody();

            if (PATH_MY_COLLECTIONS.equals(path) && "POST".equals(method)) {
                Post(exchange, is);
            }
            else if ((PATH_PUBLIC_COLLECTIONS.equals(path) || PATH_MY_COLLECTIONS.equals(path) ||
                    path.startsWith(PATH_PUBLIC_COLLECTION_BY_ID) || path.startsWith(PATH_MY_COLLECTION_BY_ID)) &&
                    "GET".equals(method)) {
                Get(exchange, is);
            }
            else if (path.startsWith(PATH_MY_COLLECTION_BY_ID) && "PUT".equals(method)) {
                Put(exchange, is);
            }
            else if (path.startsWith(PATH_MY_COLLECTION_BY_ID) && "DELETE".equals(method)) {
                Delete(exchange, is);
            }
            else {
                Logger.warning("Metodă sau endpoint nesuportat: " + method + " " + path);
                Response400.sendMethodNotAllowed(exchange,
                        "{\"error\":\"Metoda sau endpoint-ul nu este permis\"}");
            }

            try {
                    if (method.equals("POST")||method.equals("PUT")||method.equals("DELETE")) {
                        statisticsService.getGeneralStatistics();
                    }

            }
            catch (CustomException e) {
                throw new CustomException("Error",e.getDescriere(), e);
            }
            catch (Exception e) {
                Logger.warning("Eroare la actualizarea statisticilor: " + e.getMessage());
            }
        }
        catch (CustomException e) {
            handleException(exchange, e);
        }
        catch (Exception e) {
            Logger.error("Eroare neașteptată în CollectionController: " + e.getMessage());
            Response500.sendInternalServerError(exchange,
                    "{\"error\":\"Eroare internă\"}");
        }
    }

    @Override
    public void Get(HttpExchange exchange, InputStream is) throws  CustomException {
        try {
            String path = exchange.getRequestURI().getPath();
            Logger.info("Procesare cerere GET pentru colecții, path: " + path);

            if (PATH_PUBLIC_COLLECTIONS.equals(path)) {
                List<CollectionDTO> colectiiPublice = collectionService.getAllPublicCollections();
                URI uri = exchange.getRequestURI();

                if (uri.getQuery() != null) {
                    colectiiPublice = collectionService.filters(queryToMap(uri.getQuery()), colectiiPublice);
                }

                Map<String,Object>map = new HashMap<>();
                map.put("publicCollections",colectiiPublice);
                Logger.success("Colecții publice obținute cu succes: " + colectiiPublice.size() + " colecții");
                Response200.sendOk(exchange, JsonUtil.toJson(map) );
                return;
            }

            if (PATH_MY_COLLECTIONS.equals(path)) {
                Long userId = getId(getToken(exchange));
                Map<String, Object> response = collectionService.getMyCollections(userId);
                Logger.success("Colecțiile utilizatorului au fost obținute cu succes");
                Response200.sendOk(exchange,JsonUtil.toJson(response) );
                return;
            }

            Matcher matcher = ID_PATTERN.matcher(path);
            if (matcher.find()) {
                Long collectionId = Long.parseLong(matcher.group(1));

                if (path.startsWith(PATH_PUBLIC_COLLECTION_BY_ID)) {
                    Map<String, Object> response = collectionService.getPublicCollectionById(collectionId);
                    Logger.success("Colecția publică a fost obținută cu succes");
                    Response200.sendOk(exchange, JsonUtil.toJson(response) );
                    return;
                }

                if (path.startsWith(PATH_MY_COLLECTION_BY_ID)) {
                    Long userId = getId(getToken(exchange));

                    Map<String, Object> response = collectionService.getMyCollectionById(userId, collectionId);
                    Logger.success("Colecția utilizatorului a fost obținută cu succes");
                    Response200.sendOk(exchange,JsonUtil.toJson(response) );
                    return;
                }
            }

            throw new Exception400.BadRequestException("InvalidEndpoint", "Endpoint invalid",
                    "Endpoint-ul specificat nu este valid pentru operațiuni cu colecții");
        }
        catch (CustomException e) {
            throw new CustomException("CollectionRetrievalError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("CollectionRetrievalError", "Eroare la obținerea colecțiilor", e);
        }
    }

    @Override
    public void Post(HttpExchange exchange, InputStream is) throws CustomException {
        try {
            Long userId = getId(getToken(exchange));

            Map<String, Object> response = collectionService.createCollection(userId, is);
            Logger.success("Colecție creată cu succes pentru utilizatorul: " + userId);
            Response200.sendCreated(exchange, JsonUtil.toJson(response) );
        }
        catch (CustomException e) {
            throw new CustomException("CollectionCreateError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("CollectionCreateError", "Eroare la crearea colecției", e);
        }
    }

    @Override
    public void Put(HttpExchange exchange, InputStream is) throws CustomException {
        try {
            String path = exchange.getRequestURI().getPath();
            Matcher matcher = ID_PATTERN.matcher(path);

            if (matcher.find()) {
                Long collectionId = Long.parseLong(matcher.group(1));
                Long userId = getId(getToken(exchange));

                Map<String, Object> response = collectionService.updateCollection(userId, collectionId, is);
                Logger.success("Colecție actualizată cu succes");
                Response200.sendOk(exchange,JsonUtil.toJson(response) );
            } else {
                throw new Exception400.BadRequestException("InvalidCollectionId", "ID colecție invalid",
                        "ID-ul colecției nu este valid în calea specificată");
            }
        }
        catch (CustomException e) {
            throw new CustomException("CollectionUpdateError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("CollectionUpdateError", "Eroare la actualizarea colecției", e);
        }
    }

    @Override
    public void Patch(HttpExchange exchange, InputStream is) throws  CustomException {
        Logger.warning("Metodă PATCH neimplementată pentru CollectionController");
        Response400.sendMethodNotAllowed(exchange,
                "{\"error\":\"Metoda PATCH nu este implementată pentru colecții\"}");
    }

    @Override
    public void Delete(HttpExchange exchange, InputStream is) throws  CustomException {
        try {
            String path = exchange.getRequestURI().getPath();
            Matcher matcher = ID_PATTERN.matcher(path);

            if (matcher.find()) {
                Long collectionId = Long.parseLong(matcher.group(1));
                Long userId = getId(getToken(exchange));

                Map<String, Object> response = collectionService.deleteCollection(userId, collectionId);
                Logger.success("Colecție ștearsă cu succes");
                Response200.sendOk(exchange, JsonUtil.toJson(response) );
            } else {
                throw new Exception400.BadRequestException("InvalidCollectionId", "ID colecție invalid",
                        "ID-ul colecției nu este valid în calea specificată");
            }
        }
        catch (CustomException e) {
            throw new CustomException("CollectionDeleteError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("CollectionDeleteError", "Eroare la ștergerea colecției", e);
        }
    }
}