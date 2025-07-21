package backend.api.config.jwtConfig;

import backend.api.config.controllerConfig.Response400;
import backend.api.exception.CustomException;
import backend.api.exception.Logger;
import backend.api.model.User;
import backend.api.repository.UserRepository;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class JwtFilter extends Filter {

    private static final UserRepository userRepository = new UserRepository();

    //endpoint-uri publice
    private static final Set<String> PUBLIC_ROUTES = new HashSet<>(
            Arrays.asList("/login", "/register", "/reset-password", "/auth/login",
                    "/auth/register", "/auth/reset-password", "/statistics/rss")
    );

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        Logger.debug("JwtFilter procesează cererea: " + method + " " + path);

        if (isPublicRoute(path)) {
            Logger.debug("Rută publică detectată, se permite accesul fără autentificare: " + path);
            chain.doFilter(exchange);
            return;
        }

        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            //cerere fara criteriu de autentificare( NO AUTH )
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Logger.exception("UnauthorizedException");
                Logger.error("Token lipsă sau format invalid pentru ruta: " + path);

                Response400.sendUnauthorized(exchange, "{\"error\" : \"Token lipsă sau format invalid\" }");
                return;
            }

            String token = authHeader.substring(7);

            //header de autentificare invalid
            if (!JwtUtil.validateToken(token)) {
                Logger.exception("TokenExpiredException");
                Logger.error("Token invalid sau expirat pentru ruta: " + path);
                Response400.sendUnauthorized(exchange, "{\"error\": \"Token invalid sau expirat\" }");
                return;
            }

            // extragen informatiile din token
            String username = JwtUtil.getUsername(token);
            Long codeSessionFromToken = JwtUtil.getCodeSession(token);

            //verificare informatii
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isEmpty() || !user.get().getCodeSession().equals(codeSessionFromToken)) {
                Logger.exception("SessionExpiredException");
                Logger.error("Sesiune invalidă sau deconectată pentru utilizatorul: " + username);
                Response400.sendUnauthorized(exchange, "{\"error\":\"Sesiune invalidă sau deconectată\"}");
                return;
            }

            // setare atribute
            //ca sa nu mai parsam token iar
            exchange.setAttribute("username", username);
            exchange.setAttribute("id", JwtUtil.getId(token));
            exchange.setAttribute("email", JwtUtil.getEmail(token));
            exchange.setAttribute("codeSession", codeSessionFromToken);

            Logger.debug("Autentificare reușită pentru utilizatorul: " + username);

            chain.doFilter(exchange);

        } catch (Exception e) {
            Logger.exception("TokenProcessingException");
            Logger.error("Eroare la procesarea token-ului: " + e.getMessage());
            try {
                Response400.sendUnauthorized(exchange,
                        "{\"error\":\"Eroare la procesarea token-ului\"}");
            } catch (CustomException ce) {
                throw new IOException("Eroare la trimiterea răspunsului", ce);
            }
        }
    }

    private boolean isPublicRoute(String path) {

        // verificam daca endpoint necesita autentificare(este public)
        if (PUBLIC_ROUTES.contains(path)) {
            return true;
        }

        for (String publicRoute : PUBLIC_ROUTES) {
            if (path.endsWith(publicRoute)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String description() {
        return "Filtru pentru autentificare bazată pe JWT";
    }
}