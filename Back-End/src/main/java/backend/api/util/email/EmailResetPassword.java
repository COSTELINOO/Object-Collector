package backend.api.util.email;

import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailResetPassword {

    // Subiectul emailului de resetare parola
    private static final String EMAIL_SUBJECT = "Resetare parola - Codul de verificare";

    // Template-ul continutului emailului, cu loc pentru cod si data
    private static final String EMAIL_TEMPLATE = """
            Stimata domnule/Stimata doamna,
            
            Am primit o solicitare de resetare a parolei pentru contul dumneavoastra.
            
            Pentru a continua procesul de resetare a parolei, va rugam sa utilizati codul de verificare:
            
            COD DE RESETARE: %s
            
            Data si ora solicitarii: %s
            
            Daca nu ati solicitat resetarea parolei, va rugam sa ignorati acest email sau sa ne contactati imediat pentru a raporta accesul neautorizat.
            
            Cu stima,
            Echipa aplicatiei
            """;

    // Metoda care trimite emailul cu codul de resetare
    public static void sendResetCodeMail(String email, String code) throws CustomException {
        // Verifica daca emailul sau codul sunt nule sau goale
        if (email == null || email.isEmpty() || code == null || code.isEmpty()) {
            throw new Exception500.InternalServerErrorException(
                    "EmailError",
                    "InvalidParameters",
                    "Email sau cod de resetare invalid"
            );
        }

        // Verifica daca emailul este in format valid
        if (!EmailValidator.validateEmail(email)) {
            throw new Exception500.InternalServerErrorException(
                    "EmailError",
                    "InvalidEmailFormat",
                    "Format email invalid"
            );
        }

        Logger.info("Pregatire email de resetare parola pentru: " + email);

        try {
            // Obtine credentialele din configuratie
            final String username = backend.api.config.applicationConfig.Properties.getStaffEmail();
            final String password = backend.api.config.applicationConfig.Properties.getStaffPassword();

            // Verifica daca lipsesc credentialele
            if (username.isEmpty() || password.isEmpty()) {
                throw new Exception500.InternalServerErrorException(
                        "EmailError",
                        "MissingCredentials",
                        "Credentiale email lipsa in configuratie"
                );
            }

            Logger.debug("Proprietati email configurate");

            // Creeaza sesiunea de email
            Session session = createEmailSession(username, password);
            Logger.debug("Sesiune email creata");

            // Creeaza mesajul cu codul de resetare
            Message message = createResetCodeMessage(session, username, email, code);
            Logger.debug("Mesaj email creat");

            // Trimite emailul
            Transport.send(message);
            Logger.success("Email de resetare parola trimis cu succes catre: " + email);
        } catch (MessagingException e) {
            throw new Exception500.InternalServerErrorException(
                    "EmailError",
                    "SendingFailed",
                    "Eroare la trimiterea email-ului cu codul de resetare",
                    e
            );
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "EmailError",
                    "UnexpectedError",
                    "Eroare neasteptata la trimiterea email-ului de resetare",
                    e
            );
        }
    }

    // Configureaza proprietatile necesare pentru trimiterea emailului
    private static Properties configureEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true"); // Activare autentificare SMTP
        props.put("mail.smtp.starttls.enable", "true"); // Activare criptare TLS
        props.put("mail.smtp.host", "smtp.gmail.com"); // Hostul SMTP pentru Gmail
        props.put("mail.smtp.port", "587"); // Portul standard TLS
        return props;
    }

    // Creeaza o sesiune de email cu autentificare folosind username si parola
    private static Session createEmailSession(final String username, final String password) {
        return Session.getInstance(configureEmailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    // Creeaza obiectul Message ce contine emailul de resetare
    private static Message createResetCodeMessage(Session session, String fromEmail, String toEmail, String code) throws MessagingException {
        // Creeaza un nou email MIME cu sesiunea data
        Message message = new MimeMessage(session);

        // Seteaza adresa expeditorului
        message.setFrom(new InternetAddress(fromEmail));

        // Seteaza destinatarul emailului
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

        message.setSubject(EMAIL_SUBJECT);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        // Formateaza corpul emailului folosind template-ul si datele
        String emailBody = String.format(EMAIL_TEMPLATE, code, formattedDateTime);

        message.setText(emailBody);
        return message;
    }
}
