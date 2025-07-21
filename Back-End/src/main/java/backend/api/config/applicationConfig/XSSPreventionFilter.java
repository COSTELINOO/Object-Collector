package backend.api.config.applicationConfig;

import backend.api.config.controllerConfig.Response400;
import backend.api.exception.CustomException;
import backend.api.exception.Logger;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class XSSPreventionFilter extends Filter {

    // Pattern-uri pentru identificarea scripturilor malitioase
    //regex=expresie regula
    private static final Pattern[] XSS_PATTERNS = {
            // Tag-uri script ->.=orice caracter, *=0 sau mai multe, ?=se opreste la primul
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            // Expresii src='...'
            // \r=inceputul liniei curente, \\\'=',
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Expresii src="..."
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Tag-uri </script> izolate
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            // Tag-uri <script> izolate cu atribute
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Expresii eval(...)
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Expresii expression(...)
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Protocol javascript:
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            // Protocol vbscript:
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            // Handlere onload=
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Handlere onerror=
            Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Handlere onclick=
            Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Handlere onmouseover=
            Pattern.compile("onmouseover(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Tag-uri iframe
            Pattern.compile("<iframe(.*?)>(.*?)</iframe>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // data:
            Pattern.compile("data:", Pattern.CASE_INSENSITIVE),

            // Orice alte handlere de eveniment on*
            // \\w+=unul sau mai multe caractere alfanumerice(0-9,a-z,_A-Z]
            // \\s*=0 sau mai multe caractere albe(tab,spatiu,new line,etc)
            // Comentarii HTML care ar putea fi folosite pentru a ascunde scripturi
            Pattern.compile("<!--(.*?)-->", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // Funcția alert
            Pattern.compile("alert\\s*\\(.*?\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };




    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {

            // verificam URI-ul cererii
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getRawQuery();
            String path = requestURI.getPath();

            // verificam calea
            if (containsXSS(path)) {
                String remoteAddress = exchange.getRemoteAddress().toString();
                String requestMethod = exchange.getRequestMethod();

                Logger.malicious("[XSS DETECTAT] Cale: " + path +
                        " | Metodă: " + requestMethod +
                        " | Adresa: " + remoteAddress);

                Response400.sendForbidden(exchange,
                        "{\"error\": \"Cerere respinsă: conținut potențial malițios detectat\"}");
                return;
            }

            // Verificam query string-ul
            if (query != null && !query.isEmpty()) {
                if (containsXSS(query)) {
                    Logger.malicious("[XSS DETECTAT] Query: " + query);
                    Response400.sendForbidden(exchange,
                            "{\"error\": \"Cerere respinsă: Conținut potențial malițios detectat\"}");
                    return;
                }

                // Verificam fiecare parametru individual
                Map<String, List<String>> params = parseQueryParams(query);

                //verificam cheile si valorile parametrilor
                for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                    String key = entry.getKey();

                    //verificam cheile
                    if (containsXSS(key)) {
                        Logger.malicious("[XSS DETECTAT] Nume parametru: " + key);
                        Response400.sendForbidden(exchange,
                                "{\"error\": \"Cerere respinsă: Conținut potențial malițios detectat\"}");
                        return;
                    }

                    //verificam valorile cheilor
                    for (String value : entry.getValue()) {
                        if (containsXSS(value)) {
                            Logger.malicious("[XSS DETECTAT] Valoare parametru: " + value);
                            Response400.sendForbidden(exchange,
                                    "{\"error\": \"Cerere respinsă: Conținut potențial malițios detectat\"}");
                            return;
                        }
                    }
                }
            }

            // Verificam headerele
            for (String headerName : exchange.getRequestHeaders().keySet()) {
                List<String> headerValues = exchange.getRequestHeaders().get(headerName);
                for (String value : headerValues) {
                if (containsXSS(value)) {
                        Logger.malicious("[XSS DETECTAT] Header " + headerName + ": " + value);
                        Response400.sendForbidden(exchange,
                                "{\"error\": \"Cerere respinsă: Conținut potențial malițios detectat\"}");
                        return;
                    }
                }
            }


            //daca executia filtrului s-a realizat cu succes, anunta programul sa treaca la filtrul urmator
            chain.doFilter(exchange);

        } catch (Exception e) {
            Logger.error("Eroare în XSSPreventionFilter: " + e.getMessage());
            try {
                Response400.sendBadRequest(exchange,
                        "{\"error\": \"Eroare la procesarea cererii\", \"message\": \"" + e.getMessage() + "\"}");
            } catch (CustomException ce) {
                Logger.error("Eroare la trimiterea răspunsului: " + ce.getMessage());
                throw new IOException(ce);
            }
        }
    }

    private boolean containsXSS(String value) {
        if (value == null) {
            return false;
        }

        // gestionare caractere UNICODE
        String normalizedValue = Normalizer.normalize(value, Normalizer.Form.NFD);

        if (normalizedValue.contains("\0")) {
            return true;
        }

        // Verificam toate pattern-urile XSS
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(normalizedValue).find()) {
                return true;
            }
        }


        return false;
    }

    //parsare
    private Map<String, List<String>> parseQueryParams(String query) {
        Map<String, List<String>> queryParams = new LinkedHashMap<>();

        if (query == null || query.isEmpty()) {
            return queryParams;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? pair.substring(0, idx) : pair;
            String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : "";

            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);

            List<String> values = queryParams.computeIfAbsent(key, _ -> new ArrayList<>());
            values.add(value);
        }

        return queryParams;
    }

    @Override
    public String description() {
        return "Filtru pentru prevenirea atacurilor XSS (Cross-Site Scripting) prin respingerea cererilor suspecte";
    }


}