package backend.api.controller;

import backend.api.config.controllerConfig.ControllerInterface;
import backend.api.config.controllerConfig.Response200;
import backend.api.config.controllerConfig.Response400;
import backend.api.config.controllerConfig.Response500;
import backend.api.dataTransferObject.UserDTO;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Logger;
import backend.api.service.UserService;
import backend.api.util.json.JsonUtil;
import backend.api.config.jwtConfig.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static backend.api.config.controllerConfig.ControllerInterface.*;
import static backend.api.config.jwtConfig.JwtUtil.getId;


public class AuthController implements ControllerInterface {

    // Rute pentru endpoint-uri
    private static final String PATH_LOGIN = "/auth/login";
    private static final String PATH_REGISTER = "/auth/register";
    private static final String PATH_RESET_PASSWORD = "/auth/reset-password";
    private static final String PATH_CHANGE_PASSWORD = "/auth/change-password";
    private static final String PATH_CHANGE_EMAIL = "/auth/change-email";
    private static final String PATH_CHANGE_USERNAME = "/auth/change-username";
    private static final String PATH_CHANGE_PICTURE = "/auth/change-picture";
    private static final String PATH_USER_INFO = "/auth/info";
    private static final String PATH_DELETE_USER = "/auth/delete";
    private  static final String PATH_LOGOUT = "/auth/logout";
    private static final Pattern PATH_USER_INFO_BY_ID = Pattern.compile("/auth/info/(\\d+)$");

    private final UserService userService;

    public AuthController() {
        this.userService = new UserService();
    }

    @Override
    public void handle(HttpExchange exchange) throws CustomException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String remoteAddress = exchange.getRemoteAddress().toString();

        Logger.request("Auth request: " + method + " " + path + " from " + remoteAddress);

        try {
            // Verifica dacă e: GET,PUT,POST,DELETE,PATCH
            if (isBasicMethod(exchange, method)) {
                return;
            }

            InputStream is = exchange.getRequestBody();

            // Login, register, reset password code si reset password + metoda POST + logout
            if ((PATH_LOGIN.equals(path)|| PATH_LOGOUT.equals(path) || PATH_REGISTER.equals(path) ||
                    PATH_RESET_PASSWORD.equals(path) || path.startsWith(PATH_RESET_PASSWORD + "/")) &&
                    "POST".equals(method)) {
                Post(exchange, is);
            }
            // Change: password, username, email, profile picture + metoda "PUT"
            else if ((PATH_CHANGE_PASSWORD.equals(path) || PATH_CHANGE_EMAIL.equals(path) ||
                    PATH_CHANGE_USERNAME.equals(path) || PATH_CHANGE_PICTURE.equals(path)) &&
                    "PUT".equals(method)) {
                Put(exchange, is);
            }
            // Obtine informatiile despre user-ul autentificat
            else if (PATH_USER_INFO.equals(path) && "GET".equals(method)) {
                Get(exchange, is);
            }
            else if (path.matches("/auth/info/\\d+$") && "GET".equals(method)) {
                Get(exchange, is);
            }
            // sterge contul curent
            else if (PATH_DELETE_USER.equals(path) && "DELETE".equals(method)) {
                Delete(exchange, is);
            }
            else if ("PATCH".equals(method)) {
                Patch(exchange, is);
            }
            else {
                Logger.warning("Metodă sau endpoint nesuportat: " + method + " " + path);
                Response400.sendMethodNotAllowed(exchange,
                        "{\"error\":\"Metoda nu este permisă\"}");
            }
        }
        catch (CustomException e) {
            handleException(exchange, e);
        }
        catch (Exception e) {
            Logger.exception("UnexpectedException");
            Logger.error("Eroare neașteptată în AuthController: " + e.getMessage());

            Response500.sendInternalServerError(exchange,
                    "{\"error\":\"Eroare internă de server\"}");
        }
    }

    @Override
    public void Get(HttpExchange request, InputStream is) throws CustomException {
        String path = request.getRequestURI().getPath();

        try {
            // Validare token si obtinerea id-ului user-ului
            Long id = getId(getToken(request));
            Logger.info("Procesare cerere GET pentru utilizatorul cu ID: " + id);

            if (PATH_USER_INFO.equals(path)) {
                UserDTO user = userService.getUserInformation(id);

                // Se trimite id-ul, username-ul, email-ul + poza de profil
                Logger.success("Informațiile utilizatorului au fost obținute cu succes");
                Response200.sendOk(request, JsonUtil.toJson(user) );
            }

            Matcher userMatcher = PATH_USER_INFO_BY_ID.matcher(path);
            if (userMatcher.find()) {
                if (id == null) {
                    throw new Exception400.UnauthorizedException("AuthenticationError",
                            "Autentificare necesară", "Trebuie să fiți autentificat pentru a vizualiza informații despre utilizator");
                }

                Long requestedUserId = Long.parseLong(userMatcher.group(1));
                UserDTO user = userService.getUserInformation(requestedUserId);

                Logger.success("Informațiile utilizatorului au fost obținute cu succes");
                Response200.sendOk(request,
                        JsonUtil.toJson(user) );
            }
        }
        catch (CustomException e) {
            throw new CustomException("UpdateUserError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("UserInfoError", "Eroare la obținerea informațiilor utilizatorului", e);
        }
    }

    @Override
    public void Post(HttpExchange request, InputStream is) throws  CustomException {
        try {
            String path = request.getRequestURI().getPath();
            Logger.info("Procesare cerere POST pentru endpoint: " + path);

            // /auth/login
            switch (path) {
                case PATH_LOGIN -> {
                    Map<String, Object> response = userService.userLogin(is);
                    Logger.success("Autentificare reușită");
                    Response200.sendOk(request,
                            JsonUtil.toJson(response));
                }

                // auth/register
                case PATH_REGISTER -> {
                    Map<String, Object> response = userService.userRegister(is);
                    Logger.success("Înregistrare reușită");
                    Response200.sendCreated(request,
                            JsonUtil.toJson(response));
                }

                // trimitere cod de resetare parola
                case PATH_RESET_PASSWORD -> {
                    Map<String, Object> response = userService.resetRequest(is);
                    Logger.success("Cerere de resetare parolă procesată cu succes");
                    Response200.sendOk(request,
                            JsonUtil.toJson(response));
                }

                case PATH_LOGOUT -> {
                    Long id = getId(getToken(request));

                    Map<String,Object> message = userService.deconectare(id);

                    Response200.sendOk(request, JsonUtil.toJson(message) );

                    Logger.success("Deconectarea s-a efectuat cu succes");

                }

                // resetarea parolei
                case null, default -> {
                    Map<String, Object> response = userService.resetPassword(path, is);
                    Logger.success("Parolă resetată cu succes");
                    Response200.sendOk(request,
                            JsonUtil.toJson(response));
                }
            }
        }
        catch (CustomException e) {
            throw new CustomException("UpdateUserError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("ProcessRequestError", "Eroare la procesarea cererii", e);
        }
    }

    @Override
    public void Put(HttpExchange request, InputStream is) throws CustomException {
        try {
            String token = getToken(request);
            Long userId = getId(token);
            String path = request.getRequestURI().getPath();

            Logger.info("Procesare cerere PUT pentru utilizatorul cu ID: " + userId + ", endpoint: " + path);

            if (PATH_CHANGE_PASSWORD.equals(path)) {
                Map<String, Object> response = userService.changePassword(userId, is);
                Logger.success("Parolă schimbată cu succes");
                Response200.sendOk(request,
                        JsonUtil.toJson(response) );
            }
            else if (PATH_CHANGE_EMAIL.equals(path)) {
                Map<String, Object> response = userService.changeEmail(userId, is);
                Logger.success("Email schimbat cu succes");
                Response200.sendOk(request,
                        JsonUtil.toJson(response) );
            }
            else if (PATH_CHANGE_USERNAME.equals(path)) {
                Map<String, Object> response = userService.changeUsername(userId, is);
                Logger.success("Nume utilizator schimbat cu succes");
                Response200.sendOk(request,
                        JsonUtil.toJson(response) );
            }
            else if (PATH_CHANGE_PICTURE.equals(path)) {
                String contentType = request.getRequestHeaders().getFirst("Content-Type");
                Map<String, Object> response = userService.changePicture(
                        userId, JwtUtil.getUsername(token), is, contentType);
                Logger.success("Imagine profil schimbată cu succes");
                Response200.sendOk(request,
                        JsonUtil.toJson(response) );
            }
        }
        catch (CustomException e) {
            throw new CustomException("UpdateUserError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("UpdateUserError", "Eroare la actualizarea datelor utilizatorului", e);
        }
    }

    @Override
    public void Patch(HttpExchange request, InputStream is) throws  CustomException {
        Logger.warning("Metodă PATCH neimplementată pentru AuthController");
        Response400.sendMethodNotAllowed(request,
                "{\"error\":\"Metoda PATCH nu este implementată\"}");
    }

    @Override
    public void Delete(HttpExchange request, InputStream is) throws  CustomException {
        try {
            String token = getToken(request);
            Long userId = getId(token);

            Map<String, Object> response = userService.deleteUser(userId, is);
            Logger.success("Cont utilizator șters cu succes");
            Response200.sendOk(request,
                    JsonUtil.toJson(response) );
        }
        catch (CustomException e) {
            throw new CustomException("UpdateUserError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("DeleteUserError", "Eroare la ștergerea contului", e);
        }
    }
}