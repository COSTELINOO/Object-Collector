package backend.api.util.email;

import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.User;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;


public class EmailRegister {

    private static final String EMAIL_SUBJECT = "Confirmare înregistrare cont";
    private static final String EMAIL_TEMPLATE = """
            Stimată domnule/Stimată doamnă,

            Vă mulțumim pentru înregistrarea în cadrul aplicației noastre.

            Prin prezenta vă confirmăm crearea cu succes a contului dumneavoastră cu următoarele detalii:

            Email: %s
            Nume utilizator: %s
            Data și ora înregistrării: %s

            Cu stimă,
            Echipa aplicației
            """;


    public static void sendConfirmationEmail(User user) throws CustomException {
        if (user == null || user.getEmail() == null || user.getUsername() == null) {
            throw new Exception500.InternalServerErrorException(
                    "EmailError",
                    "InvalidUserData",
                    "Date utilizator invalide pentru trimiterea email-ului de confirmare"
            );
        }

        Logger.info("Pregătire email de confirmare pentru: " + user.getEmail());

        try {
            //obtinem username-ul si parola
            final String username = backend.api.config.applicationConfig.Properties.getStaffEmail();
            final String password = backend.api.config.applicationConfig.Properties.getStaffPassword();

            if (username.isEmpty() || password.isEmpty()) {
                throw new Exception500.InternalServerErrorException(
                        "EmailError",
                        "MissingCredentials",
                        "Credențiale email lipsă în configurație"
                );
            }

            Session session = createEmailSession(username, password);
            Logger.debug("Sesiune email creată");

            Message message = createConfirmationMessage(session, username, user);
            Logger.debug("Mesaj email creat");

            Transport.send(message);
            Logger.success("Email de confirmare trimis cu succes către: " + user.getEmail());
        } catch (MessagingException e) {
            throw new Exception500.InternalServerErrorException(
                    "EmailError",
                    "SendingFailed",
                    "Eroare la trimiterea email-ului de confirmare",
                    e
            );
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "EmailError",
                    "UnexpectedError",
                    "Eroare neașteptată la trimiterea email-ului de confirmare",
                    e
            );
        }
    }


    private static Properties configureEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");

        //forma de criptare pe care gmail o cere obligatoriu, oferind o conexiune securizata
        props.put("mail.smtp.starttls.enable", "true");

        props.put("mail.smtp.host", "smtp.gmail.com");

        //portul standard pentru starttls
        props.put("mail.smtp.port", "587");
        return props;
    }

    //creeaza o sesiune de trimitere email, cu configuratiile necesare si cu username-ul si parola data
    private static Session createEmailSession(final String username, final String password) {
        return Session.getInstance(configureEmailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private static Message createConfirmationMessage(Session session, String fromEmail, User user) throws MessagingException {

        // Initializeaza un email MIME (text simplu sau cu atasamente, HTML etc.) folosind sesiunea data
        Message message = new MimeMessage(session);

        //seteaza expeditorul
        message.setFrom(new InternetAddress(fromEmail));

        //seteaza destinatarul
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
        message.setSubject(EMAIL_SUBJECT);

        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

        String emailBody = String.format(EMAIL_TEMPLATE,
                user.getEmail(),
                user.getUsername(),
                currentDateTime
        );

        message.setText(emailBody);
        return message;
    }
}