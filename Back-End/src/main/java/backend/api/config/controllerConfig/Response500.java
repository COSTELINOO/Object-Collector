package backend.api.config.controllerConfig;

import backend.api.exception.CustomException;
import com.sun.net.httpserver.HttpExchange;

import static backend.api.config.controllerConfig.ControllerInterface.sendFixedStatusResponse;


public class Response500 {

    public static void sendInternalServerError(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 500, response);
    }

    public static void sendBadGateway(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 502, response);
    }

    public static void sendServiceUnavailable(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 503, response);
    }
}