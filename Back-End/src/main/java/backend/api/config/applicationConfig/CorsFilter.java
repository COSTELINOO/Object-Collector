package backend.api.config.applicationConfig;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import backend.api.exception.Logger;
import java.io.IOException;

//filtru cors
public class CorsFilter extends Filter {

    @Override
    //
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            Logger.debug("Procesare cerere CORS: " + exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());

            // [FUNCTII PREDEFINITE]: adaugare + configurare headers CORS
            //adauga headerele cors la header-ele de raspuns, pentru a fi compatibile cu browser-ul
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", Properties.getCorsAllowOrigin());
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", Properties.getCorsAllowHeaders());
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", Properties.getCorsAllowMethods());


            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                Logger.info("Cerere OPTION detectată, se trimite răspuns 204");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            //dreprezinta legatura dintre lantul de filtre
            //daca executia filtrului s-a realizat cu succes, anunta programul sa treaca la filtrul urmator
            chain.doFilter(exchange);
        }
        catch (IOException e) {
            Logger.exception("CorsFilterException");
            Logger.error("Eroare în filtrul CORS: " + e.getMessage());
            throw e;
        }
        catch (Exception e) {
            Logger.exception("UnexpectedException");
            Logger.error("Eroare neașteptată în filtrul CORS: " + e.getMessage());
            throw new IOException("Eroare neașteptată în filtrul CORS", e);
        }
    }

    @Override
    public String description() {
        return "Filtru pentru gestionarea Cross-Origin Resource Sharing (CORS)";
    }
}