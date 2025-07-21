package backend.api.config.controllerConfig;

import com.sun.net.httpserver.HttpExchange;
import backend.api.exception.CustomException;
import static backend.api.config.controllerConfig.ControllerInterface.sendFixedStatusResponse;

public class Response200 {

    public static void sendOk(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 200, response);
    }

    public static void sendCreated(HttpExchange exchange, String response) throws CustomException {
        sendFixedStatusResponse(exchange, 201, response);
    }

}