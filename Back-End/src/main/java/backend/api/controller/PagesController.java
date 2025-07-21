package backend.api.controller;

import backend.api.config.applicationConfig.Properties;
import backend.api.exception.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PagesController implements HttpHandler {

    private static final String WEB_ROOT = Properties.getFrontPath() ;
    private static final String DEFAULT_PAGE = "page_login.html";
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        // Maparea extensiilor la tipurile
        MIME_TYPES.put("html", "text/html; charset=UTF-8");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();

        // Trateaza ruta principala redirectionand la pagina implicita
        if ("/".equals(requestPath)) {
            requestPath = File.separator+ DEFAULT_PAGE;
        }

        // Elimina prefixul "/" pentru a obtine calea relativa
        String relativePath = requestPath.startsWith(File.separator) ? requestPath.substring(1) : requestPath;

        // Construiește calea completă la fișier
        Path filePath = Paths.get(WEB_ROOT, relativePath);
        File file = filePath.toFile();

        if (file.exists() && !file.isDirectory()) {

            String contentType = getContentType(file.getName());

            //citeste continutul fisierului
            byte[] fileContent = Files.readAllBytes(filePath);

            // Seteaza raspunsul
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileContent.length);


            // Scrie continutul fsșierului în răspuns
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileContent);
            }

            Logger.debug("Ruta fișier static: " + requestPath);
        } else {

            if (!relativePath.contains(".")) {
                Path indexPath = Paths.get(WEB_ROOT, DEFAULT_PAGE);
                if (Files.exists(indexPath)) {
                    byte[] indexContent = Files.readAllBytes(indexPath);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, indexContent.length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(indexContent);
                    }
                    Logger.debug("Pagina principala " + requestPath + " -> " + DEFAULT_PAGE);
                    return;
                }
            }

            // Fisierul nu exista sau este un director
            String notFoundResponse = "<html><body><h1>404 Not Found</h1><p>Pagina căutată nu există: " + requestPath + "</p></body></html>";
            byte[] responseBytes = notFoundResponse.getBytes();

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(404, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            Logger.warning("Fișier static negăsit: " + requestPath);
        }
    }

    private String getContentType(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            String extension = fileName.substring(lastDot + 1).toLowerCase();
            String contentType = MIME_TYPES.get(extension);
            if (contentType != null) {
                return contentType;
            }
        }
        return "application/octet-stream";
    }
}