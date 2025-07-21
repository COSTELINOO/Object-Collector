package backend.api.controller;

import backend.api.config.controllerConfig.ControllerInterface;
import backend.api.config.controllerConfig.Response400;
import backend.api.config.controllerConfig.Response500;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Logger;
import backend.api.service.StatisticsService;

import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static backend.api.config.controllerConfig.ControllerInterface.*;
import static backend.api.config.jwtConfig.JwtUtil.getId;

public class StatisticsController implements ControllerInterface {
    private static final String PATH_PERSONAL_PDF = "/statistics/personal/pdf";
    private static final String PATH_PUBLIC_PDF = "/statistics/public/pdf";
    private static final String PATH_PERSONAL_CSV = "/statistics/personal/csv";
    private static final String PATH_PUBLIC_CSV = "/statistics/public/csv";
    private static final String PATH_RSS = "/statistics/public/rss";

    private final StatisticsService statisticsService;

    public StatisticsController() {
        this.statisticsService = new StatisticsService();
    }

    @Override
    public void handle(HttpExchange exchange) throws CustomException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String remoteAddress = exchange.getRemoteAddress().toString();

        Logger.request("Statistics request: " + method + " " + path + " from " + remoteAddress);

        try {
            // Verifica daca e: GET,PUT,POST,DELETE,PATCH
            if (ControllerInterface.isBasicMethod(exchange, method)) {
                return;
            }

            // Preia continutul din body/metadata
            InputStream is = exchange.getRequestBody();

            if ("POST".equals(method)) {
                Post(exchange, is);
            }
            else if ("PUT".equals(method)) {
                Put(exchange, is);
            }
            else if (path.startsWith("/statistics") && "GET".equals(method)) {
                Get(exchange, is);
            }
            else if ("DELETE".equals(method)) {
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
            Logger.error("Eroare neașteptată în StatisticsController: " + e.getMessage());
            Response500.sendInternalServerError(exchange,
                    "{\"error\":\"Eroare internă\"}");
        }
    }

    @Override
    public void Get(HttpExchange request, InputStream is) throws CustomException {
        String path = request.getRequestURI().getPath();
        Logger.info("Procesare cerere GET pentru statistici, path: " + path);

        try {
            Long id = getId(ControllerInterface.getToken(request));

            if (PATH_PERSONAL_PDF.equals(path)) {
                byte[] pdf = statisticsService.personalPdf(id);

                String filename = "statistici_user_" + id + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                        ".pdf";

                Logger.success("Statistici personale PDF generate cu succes");
                ControllerInterface.sendDownloadResponseBinary(request, 200, pdf, "application/pdf", filename);
                return;
            }
            else if (PATH_PUBLIC_PDF.equals(path)) {
                byte[] pdf = statisticsService.getStatisticsPdf();

                String filename = "statistici_generale_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                        ".pdf";

                Logger.success("Statistici generale PDF generate cu succes");
                ControllerInterface.sendDownloadResponseBinary(request, 200, pdf, "application/pdf", filename);
                return;
            }
            else if (PATH_PERSONAL_CSV.equals(path)) {
                byte[] csv = statisticsService.personalCsv(id);

                String filename = "statistici_user_" + id + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                        ".csv";

                Logger.success("Statistici personale CSV generate cu succes");
                ControllerInterface.sendDownloadResponseBinary(request, 200, csv, "application/csv", filename);
                return;
            }
            else if (PATH_PUBLIC_CSV.equals(path)) {
                byte[] csv = statisticsService.getStatisticsCsv();

                String filename = "statistici_generale_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                        ".csv";

                Logger.success("Statistici generale CSV generate cu succes");
                ControllerInterface.sendDownloadResponseBinary(request, 200, csv, "application/csv", filename);
                return;
            }
            else if (PATH_RSS.equals(path)) {
                byte[] xmlData = statisticsService.getStatisticsXml(false);

                Logger.success("Flux RSS generat cu succes");
                ControllerInterface.sendFileResponse(request, 200, xmlData, "application/rss+xml");
                return;
            }

            throw new Exception400.BadRequestException("InvalidEndpoint", "Endpoint invalid",
                    "Endpoint-ul specificat nu este valid pentru statistici");
        }
        catch (CustomException e) {
            throw new CustomException("StatisticsGenerationError",e.getDescriere(), e);
        }
        catch (Exception e) {
            throw new CustomException("StatisticsGenerationError", "Eroare la generarea statisticilor", e);
        }
    }

    @Override
    public void Patch(HttpExchange request, InputStream is) throws CustomException {
        Logger.warning("Metodă PATCH neimplementată pentru StatisticsController");
        Response400.sendMethodNotAllowed(request,
                "{\"error\":\"Metoda PATCH nu este implementată pentru statistici\"}");
    }

    @Override
    public void Post(HttpExchange request, InputStream is) throws CustomException {
        Logger.warning("Metodă POST neimplementată pentru StatisticsController");
        Response400.sendMethodNotAllowed(request,
                "{\"error\":\"Metoda POST nu este implementată pentru statistici\"}");
    }

    @Override
    public void Put(HttpExchange request, InputStream is) throws CustomException {
        Logger.warning("Metodă PUT neimplementată pentru StatisticsController");
        Response400.sendMethodNotAllowed(request,
                "{\"error\":\"Metoda PUT nu este implementată pentru statistici\"}");
    }

    @Override
    public void Delete(HttpExchange request, InputStream is) throws CustomException {
        Logger.warning("Metodă DELETE neimplementată pentru StatisticsController");
        Response400.sendMethodNotAllowed(request,
                "{\"error\":\"Metoda DELETE nu este implementată pentru statistici\"}");
    }
}