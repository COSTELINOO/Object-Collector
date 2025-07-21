package backend.api.config.jwtConfig;

import backend.api.exception.CustomException;
import backend.api.exception.Logger;
import backend.api.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    // generare cheie pentru semnatura token-ului
    //functie din libraria jwt, creeaza o cheie secreta dupa algoritmul de mai jos
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // valabilitate token: 8 ore (28800000 = 1000 * 60 * 60 * 8)
    private static final long EXPIRATION_TIME = 28800000;

    public static String generateToken(User user) throws CustomException {
        try {
            if (user == null) {
                throw new CustomException("TokenGenerationError", "Utilizator invalid",
                        "Nu se poate genera token pentru un utilizator null");
            }

            if (user.getUsername() == null || user.getEmail() == null || user.getId() == null) {
                Logger.exception("TokenGenerationException");
                Logger.error("Date incomplete pentru generarea token-ului: " +
                        (user.getUsername() == null ? "username lipsă, " : "") +
                        (user.getEmail() == null ? "email lipsă, " : "") +
                        (user.getId() == null ? "id lipsă" : ""));
                throw new CustomException("TokenGenerationError", "Date utilizator incomplete",
                        "Date incomplete pentru generarea token-ului");
            }

            Date now = new Date();
            Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

            // claim-uri token
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", user.getEmail());
            claims.put("username", user.getUsername());
            claims.put("id", user.getId());
            claims.put("codeSession", user.getCodeSession());

            //setare informatii care vreau sa fie pastrate in token
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getUsername())
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(SECRET_KEY)
                    .compact();

            Logger.success("Token JWT generat cu succes pentru utilizatorul: " + user.getUsername());
            return token;
        } catch (Exception e) {
            throw new CustomException("TokenGenerationError", "Eroare la generarea token-ului", e);
        }
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            Logger.warning("Token expirat: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            Logger.warning("Token cu format nesuportat: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            Logger.warning("Token malformat: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            Logger.warning("Eroare de semnătură a token-ului: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Logger.warning("Eroare neașteptată la validarea token-ului: " + e.getMessage());
            return false;
        }
    }

    public static String getUsername(String token) throws CustomException {
        try {
            return getClaims(token).get("username", String.class);
        } catch (Exception e) {
            throw new CustomException("TokenClaimError", "Eroare la citirea token-ului", e);
        }
    }

    public static String getEmail(String token) throws CustomException {
        try {
            return getClaims(token).get("email", String.class);
        } catch (Exception e) {
            throw new CustomException("TokenClaimError", "Eroare la citirea token-ului", e);
        }
    }

    public static Long getId(String token) throws CustomException {
        try {
            return getClaims(token).get("id", Long.class);
        } catch (Exception e) {
            throw new CustomException("TokenClaimError", "Eroare la citirea token-ului", e);
        }
    }

    public static Long getCodeSession(String token) throws CustomException {
        try {
            return getClaims(token).get("codeSession", Long.class);
        } catch (Exception e) {
            throw new CustomException("TokenClaimError", "Eroare la citirea token-ului", e);
        }
    }

    private static Claims getClaims(String token)   {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}