package backend.api.util.email;

import backend.api.exception.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean validateEmail(String email) {
        if (email == null) {
            Logger.warning("Validare email: email null");
            return false;
        }

        if (email.isEmpty()) {
            Logger.warning("Validare email: email gol");
            return false;
        }

        if (email.length() < 6) {
            Logger.warning("Validare email: email prea scurt - " + email.length() + " caractere");
            return false;
        }

        int atCount = countOccurrences(email);
        if (atCount != 1) {
            Logger.warning("Validare email: număr incorect de caractere @ - " + atCount);
            return false;
        }

        Matcher matcher = EMAIL_PATTERN.matcher(email);
        boolean matches = matcher.matches();

        if (!matches) {
            Logger.warning("Validare email: format invalid - " + email);
        }

        return matches;
    }

    private static int countOccurrences(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == '@') {
                count++;
            }
        }
        return count;
    }
}