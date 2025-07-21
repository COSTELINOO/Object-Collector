package backend.api.config.controllerConfig;

import backend.api.exception.CustomException;
import com.sun.net.httpserver.HttpExchange;

import static backend.api.config.controllerConfig.ControllerInterface.sendFixedStatusResponse;


public class Response400 {

    public static void sendBadRequest(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 400, response);
    }

    public static void sendUnauthorized(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 401, response);
    }

    public static void sendForbidden(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 403, response);
    }

    public static void sendNotFound(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 404, response);
    }

    public static void sendMethodNotAllowed(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 405, response);
    }

    public static void sendConflict(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 409, response);
    }
}