package backend.api.config.applicationConfig;

import backend.api.config.jwtConfig.JwtFilter;
import backend.api.controller.*;
import backend.api.exception.CustomException;
import backend.api.exception.Logger;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {
    private HttpServer serverManager;

    public void start(String address, int port) throws CustomException {
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException("ServerConfigError", "Adresă server invalidă",
                    "Adresa serverului nu poate fi null sau empty");
        }

        if (port <= 0 || port > 65535) {
            throw new CustomException("ServerConfigError", "Port server invalid",
                    "Portul serverului trebuie să fie între 1 și 65535");
        }

        try {
            // Creare server cu "address:port" (ex: 0.0.0.0:2222)
            Logger.info("Configurare server pentru adresa: " + address + ", cu portul: " + port + "...");
            serverManager = HttpServer.create(new InetSocketAddress(address, port), 0);

            // configurare endpoint-uri existente
            Logger.info("Configurare endpoint-uri acceptate...");
            setupContexts();
            Logger.success("Endpoint-urile au fost configurate cu succes");

            Logger.info("Pornire server la adresa: " + address + ", cu portul: " + port);

            serverManager.setExecutor(Executors.newFixedThreadPool(10));
            //pornire server HTTP
            serverManager.start();
            Logger.success("Serverul a pornit la adresa: " + address + ", cu portul: " + port);

        }
        catch (IOException e) {
            throw new CustomException("ServerStartError", "Crearea serverului a eșuat", e);
        } catch (Exception e) {
            throw new CustomException("ServerStartError", "Eroare neașteptată la pornirea serverului", e);
        }
    }

    public static void create(HttpServer server, String endpoint, HttpHandler controller) throws CustomException {
        if (server == null) {
            throw new CustomException("ServerConfigError", "Server HTTP null",
                    "Serverul HTTP nu poate fi null la crearea endpoint-ului");
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new CustomException("ServerConfigError", "Endpoint invalid",
                    "Endpoint-ul nu poate fi null sau gol");
        }

        if (controller == null) {
            throw new CustomException("ServerConfigError", "Controller null",
                    "Controller-ul nu poate fi null la crearea endpoint-ului: " + endpoint);
        }

        try {
            //creare endpoint public(fara JWT) + configurare CORS
            //creeaza o asociere intre un url si un hnadler(controller)
            //daca ai "URL=endpoint", du-te catre "HANDLER=controller" pentru interogare
            HttpContext context = server.createContext(endpoint, controller);

            //imi extrage filtrele setate si-mi adauga CORS filter si XSS filter
            //acestea se executa inainte de a fi atribuita cererea unui handler/controller
            context.getFilters().add(new CorsFilter());
            context.getFilters().add(new XSSPreventionFilter());
            Logger.info("Endpoint creat (neprotejat): " + endpoint);
        }
        catch (Exception e) {
            throw new CustomException("ServerConfigError", "Eroare la crearea endpoint-ului", e);
        }
    }

    public static void createProtected(HttpServer server, String endpoint, HttpHandler controller) throws CustomException {
        if (server == null) {
            throw new CustomException("ServerConfigError", "Server HTTP null",
                    "Serverul HTTP nu poate fi null la crearea endpoint-ului protejat");
        }

        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new CustomException("ServerConfigError", "Endpoint invalid",
                    "Endpoint-ul nu poate fi null sau gol");
        }

        if (controller == null) {
            throw new CustomException("ServerConfigError", "Controller null",
                    "Controller-ul nu poate fi null la crearea endpoint-ului protejat: " + endpoint);
        }

        try {

            // endpoint protejat(cu JWT) + filtru cors
            HttpContext context = server.createContext(endpoint, controller);
            context.getFilters().add(new CorsFilter());
            context.getFilters().add(new JwtFilter());
            context.getFilters().add(new XSSPreventionFilter());
            Logger.info("Endpoint creat (protejat): " + endpoint);

        } catch (Exception e) {
            throw new CustomException("ServerConfigError", "Eroare la crearea endpoint-ului protejat", e);
        }
    }

    private void setupContexts() throws CustomException {
        try {
            authController();
            collectionController();
            obiectController();
            statisticsController();
            pagesController();
        } catch (Exception e)
        {
            throw new CustomException("ServerConfigError", "Eroare la configurarea endpoint-urilor", e);
        }
    }

    private void authController() throws CustomException {
        AuthController authController = new AuthController();

        Logger.info("Configurare endpoint-uri de autentificare...");

        // endpoint-uri autentificare
        create(serverManager, "/auth/login", authController);
        create(serverManager, "/auth/register", authController);
        create(serverManager, "/auth/reset-password", authController);
        create(serverManager, "/auth/reset-password/", authController);

        // endpoint-uri gestionare account
        createProtected(serverManager, "/auth/change-password", authController);
        createProtected(serverManager, "/auth/change-username", authController);
        createProtected(serverManager, "/auth/change-email", authController);
        createProtected(serverManager, "/auth/change-picture", authController);

        createProtected(serverManager, "/auth/info", authController);
        createProtected(serverManager,"/auth/logout", authController);
        createProtected(serverManager, "/auth/delete", authController);

        Logger.success("Endpoint-uri de autentificare configurate cu succes");
    }

    private void collectionController() throws CustomException {
        CollectionController collectionController = new CollectionController();

        Logger.info("Configurare endpoint-uri pentru colecții...");

        //colectii vizibile global
        createProtected(serverManager, "/all-collection", collectionController);
        createProtected(serverManager, "/all-collection/", collectionController);

        //colectii private
        createProtected(serverManager, "/user-collection", collectionController);
        createProtected(serverManager, "/user-collection/", collectionController);

        Logger.success("Endpoint-urile pentru colecții au fost configurate cu succes");
    }

    private void obiectController() throws CustomException {
        ObjectController obiectController = new ObjectController();

        Logger.info("Configurare endpoint-uri pentru obiecte...");

        //obiecte vizibile global
        createProtected(serverManager, "/objects", obiectController);
        createProtected(serverManager, "/objects/", obiectController);
        createProtected(serverManager, "/object/", obiectController);
        createProtected(serverManager, "/obiect/", obiectController);
        //obiectele publice dintr-o anumita colectie
        createProtected(serverManager, "/collection", obiectController);
        createProtected(serverManager, "/collection/", obiectController);

        //obiectele proprii
        createProtected(serverManager, "/my-objects", obiectController);
        createProtected(serverManager, "/my-objects/", obiectController);


        createProtected(serverManager, "/my-collection", obiectController);

        createProtected(serverManager, "/my-collection/", obiectController);

        Logger.success("Endpoint-urile pentru obiecte au fost configurate cu succes");
    }

    private void statisticsController() throws CustomException {
        StatisticsController statisticsController = new StatisticsController();

        Logger.info("Configurare endpoint-uri pentru statistici...");

        // statistici publice(rss+csv+pdf) + statistici proprii(pdf +csv)
        createProtected(serverManager, "/statistics/public/pdf", statisticsController);
        createProtected(serverManager, "/statistics/personal/pdf", statisticsController);

        createProtected(serverManager, "/statistics/public/csv", statisticsController);
        createProtected(serverManager, "/statistics/personal/csv", statisticsController);

        createProtected(serverManager, "/statistics/public/rss", statisticsController);

        // adaugare endpoint public pentru RSS flux
        create(serverManager, "/statistics/rss", statisticsController);

        Logger.success("Endpoint-urile pentru statistici au fost configurate cu succes");
    }

    private void pagesController() throws CustomException {
        Logger.info("Configurare endpoint-uri pentru conținut static...");
        PagesController pages = new PagesController();
        HttpContext context = serverManager.createContext("/", pages);
        context.getFilters().add(new CorsFilter());
        context.getFilters().add(new XSSPreventionFilter());
    }
    }