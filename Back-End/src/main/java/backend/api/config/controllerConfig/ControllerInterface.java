package backend.api.config.controllerConfig;

import backend.api.config.jwtConfig.JwtUtil;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.User;
import backend.api.repository.UserRepository;
import backend.api.util.json.JsonUtil;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public interface ControllerInterface extends HttpHandler {

      UserRepository userRepository = new UserRepository();

      void Get(HttpExchange request, InputStream is) throws IOException, CustomException;

      void Post(HttpExchange request, InputStream is) throws IOException, CustomException;

      void Put(HttpExchange request, InputStream is) throws IOException, CustomException;

      void Patch(HttpExchange request, InputStream is) throws IOException, CustomException;

      void Delete(HttpExchange request, InputStream is) throws IOException, CustomException;

      static void handleException(HttpExchange request, CustomException e) {
            if (e == null) {
                  Logger.error("S-a apelat handleException cu o excepție null");
                  return;
            }

            try {
                  // 400 (client errors)
                  if (e instanceof Exception400.BadRequestException) {
                        Logger.exception("BadRequestException");
                        Logger.error("Cerere invalidă: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());

                        Response400.sendBadRequest(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  } else if (e instanceof Exception400.UnauthorizedException) {
                        Logger.exception("UnauthorizedException");
                        Logger.error("Autentificare eșuată: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response400.sendUnauthorized(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  } else if (e instanceof Exception400.NotFoundException) {
                        Logger.exception("NotFoundException");
                        Logger.error("Resursă negăsită: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response400.sendNotFound(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  } else if (e instanceof Exception400.ForbiddenException) {
                        Logger.exception("ForbiddenException");
                        Logger.error("Acces interzis: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response400.sendForbidden(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  } else if (e instanceof Exception400.MethodNotAllowedException) {
                        Logger.exception("MethodNotAllowedException");
                        Logger.error("Metodă nepermisă: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response400.sendMethodNotAllowed(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  } else if (e instanceof Exception400.ConflictException) {
                        Logger.exception("ConflictException");
                        Logger.error("Conflict de resurse: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response400.sendConflict(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  }

                  //  500 (server errors)
                  else if (e instanceof Exception500.InternalServerErrorException) {
                        Logger.exception("InternalServerErrorException");
                        Logger.error("Eroare internă server: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response500.sendInternalServerError(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  } else if (e instanceof Exception500.ServiceUnavailableException) {
                        Logger.exception("ServiceUnavailableException");
                        Logger.error("Serviciu indisponibil: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response500.sendServiceUnavailable(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  } else if (e instanceof Exception500.BadGatewayException) {
                        Logger.exception("BadGatewayException");
                        Logger.error("Gateway invalid: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response500.sendBadGateway(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  } else {
                        // orice alta exceptie
                        Logger.exception(e.getNume());
                        Logger.error("Excepție neprevăzută: " + e.getDescriere());
                        Logger.error(e.getTip());
                        Logger.error(e.getNume());
                        Logger.error(e.getMessage());
                        Response500.sendInternalServerError(request, "{\"error\": \""+ e.getNume()+"\"}" );
                  }
            } catch (Exception ex) {
                  Logger.exception("ExceptionHandlingError");
                  Logger.error("Eroare la gestionarea excepției: " + ex.getMessage());
                  Logger.error(e.getTip());
                  Logger.error(e.getNume());
                  Logger.error(e.getMessage());
            }
      }

      static void sendFixedStatusResponse(HttpExchange exchange, int statusCode, String response) throws CustomException {
            try {
                  Logger.info("Trimitere răspuns către client...");
                  //transformam stringul in vectori de octati
                  byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

                  //  content type JSON
                  exchange.getResponseHeaders().set("Content-Type", "application/json");
                  exchange.sendResponseHeaders(statusCode, responseBytes.length);

                  try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                  }

                  Logger.success("Răspunsul a fost trimis cu succes");
            } catch (IOException e) {
                  throw new CustomException("ResponseSendError", "Eroare la trimiterea mesajului către client", e);
            }
      }

      static void sendDownloadResponseBinary(HttpExchange exchange, int statusCode, byte[] responseBytes, String contentType, String filename) throws CustomException {
            try {
                  Logger.info("Trimitere fișier pentru descărcare: " + filename);

                  exchange.getResponseHeaders().set("Content-Type", contentType);
                  exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                  exchange.sendResponseHeaders(statusCode, responseBytes.length);

                  try (OutputStream os = exchange.getResponseBody()) {
                        os.write(responseBytes);
                  }

                  Logger.success("Fișierul a fost trimis cu succes pentru descărcare");
            } catch (IOException e)
            {
                  throw new CustomException("FileSendError", "Eroare la trimiterea fișierului către client", e);
            }
      }

      static void sendFileResponse(HttpExchange exchange, int statusCode, byte[] response, String contentType) throws CustomException {
            try {
                  Logger.info("Trimitere răspuns cu conținut de tip " + contentType);

                  exchange.getResponseHeaders().set("Content-Type", contentType);
                  exchange.sendResponseHeaders(statusCode, response.length);

                  try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response);
                  }

                  Logger.success("Răspunsul a fost trimis cu succes");
            } catch (IOException e) {
                  throw new CustomException("ResponseSendError", "Eroare la trimiterea răspunsului către client", e);
            }
      }

      static boolean isBasicMethod(HttpExchange exchange, String crud) throws CustomException {
            if (!(crud.equals("GET") || crud.equals("POST") || crud.equals("PUT") ||
                    crud.equals("DELETE") || crud.equals("PATCH"))) {
                  Logger.warning("Metoda HTTP neacceptată: " + crud);

                  Map<String, String> errorResponse = new HashMap<>();
                  errorResponse.put("error", "Method not allowed");
                  Response400.sendMethodNotAllowed(exchange, JsonUtil.toJson(errorResponse));
                  return true;
            }
            return false;
      }

      static String getToken(HttpExchange request) throws CustomException {
            try {
                  String authHeader = request.getRequestHeaders().getFirst("Authorization");

                  if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                        Map<String, String> errorResponse = new HashMap<>();
                        errorResponse.put("error", "Token lipsă sau invalid");

                        throw new Exception400.UnauthorizedException(
                                "AuthenticationError",
                                JsonUtil.toJson(errorResponse),
                                "Utilizatorul nu este autentificat în aplicație"
                        );
                  }

                  String token = authHeader.substring(7);

                  String username = JwtUtil.getUsername(token);
                  Long codeSession = JwtUtil.getCodeSession(token);

                  Optional<User> user = userRepository.findByUsername(username);

                  if (user.isEmpty() || !Objects.equals(user.get().getCodeSession(), codeSession)) {
                        Map<String, String> errorResponse = new HashMap<>();

                        errorResponse.put("error", "Ai fost deconectat de la sesiune");

                        throw new Exception400.UnauthorizedException(
                                "SessionExpiredError",
                                JsonUtil.toJson(errorResponse),
                                "Utilizatorul a fost deconectat de la sesiune"
                        );
                  }

                  return token;
            } catch (Exception400.UnauthorizedException e) {
                  throw e;
            } catch (Exception e) {
                  throw new CustomException("AuthenticationError", "Eroare la validarea autentificării", e);
            }
      }

      static Map<String, String> queryToMap(String query) {

            Map<String, String> result = new HashMap<>();
            if (query == null) return result;

            for (String param : query.split("&")) {
                  String[] pair = param.split("=");
                  if (pair.length > 1) {
                        result.put(pair[0], pair[1]);
                  } else {
                        result.put(pair[0], "");
                  }
            }
            return result;
      }
}