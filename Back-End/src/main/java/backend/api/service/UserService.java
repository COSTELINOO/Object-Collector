package backend.api.service;

import backend.api.config.jwtConfig.JwtUtil;
import backend.api.dataTransferObject.UserDTO;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.mapper.UserMapper;
import backend.api.model.User;
import backend.api.repository.UserRepository;
import backend.api.util.json.JsonUtil;
import backend.api.util.multipart.MultipartParser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import static backend.api.config.applicationConfig.Properties.getPath;
import static backend.api.util.email.EmailRegister.sendConfirmationEmail;
import static backend.api.util.email.EmailResetPassword.sendResetCodeMail;
import static backend.api.util.email.EmailValidator.validateEmail;

public class UserService {
    private final UserRepository userRepository;


    public UserService() {
        this.userRepository = new UserRepository();
    }

    public Map<String,Object> deconectare(Long id) throws CustomException {
        if (id == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidUserId",
                    "ID utilizator invalid"
            );
        }

        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            throw new Exception400.NotFoundException(
                    "ResourceNotFound",
                    "UserNotFound",
                    "Utilizatorul nu a fost găsit"
            );
        }

        User user = userOpt.get();

        long code = 100000 + new Random().nextInt(900000);

        user.setCodeSession(code);

        userRepository.update(user);


        Map<String, Object> response = new HashMap<>();

        response.put("message", "Deconectare reușită");

        Logger.success("Codel sesiunii a fost actualizat pentru utilizatorul: " + user.getId());

        return response;

    }

    public UserDTO getUserInformation(Long id) throws CustomException {
        if (id == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidUserId",
                    "ID utilizator invalid"
            );
        }


        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            throw new Exception400.NotFoundException(
                    "ResourceNotFound",
                    "UserNotFound",
                    "Utilizatorul nu a fost găsit"
            );
        }

        User user = userOpt.get();
        UserDTO userDTO = UserMapper.toDTO(user);

        Logger.success("Informații obținute cu succes pentru utilizatorul cu ID: " + id);
        return userDTO;
    }

    public Map<String, Object> userLogin(InputStream is) throws CustomException {
        if (is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru autentificare"
            );
        }

        Logger.info("Procesare cerere de autentificare");

        try {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            User userInput = JsonUtil.fromJson(requestBody, User.class);
            String username = userInput.getUsername() != null ? userInput.getUsername().trim() : "";
            String email = userInput.getEmail() != null ? userInput.getEmail().trim() : "";
            String password = userInput.getPassword() != null ? userInput.getPassword().trim() : "";

            if ((username.isEmpty() && email.isEmpty()) || password.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncompleteCredentials",
                        "Username-ul/email-ul și parola sunt necesare"
                );
            }

            if ((!username.isEmpty() && username.length() < 5) ||
                    (!email.isEmpty() && (email.length() < 10 || !validateEmail(email))) ||
                    password.length() < 7) {
                throw new Exception400.UnauthorizedException(
                        "ValidationError",
                        "InvalidFormat",
                        "Format invalid: username ≥ 5, parola ≥ 7, email valid"
                );
            }

            Optional<User> userOpt;
            if (!username.isEmpty()) {
                userOpt = userRepository.findByUsername(username);
            } else {
                userOpt = userRepository.findByEmail(email);
            }

            if (userOpt.isEmpty() || !BCrypt.checkpw(password, userOpt.get().getPassword())) {
                throw new Exception400.BadRequestException(
                        "AuthenticationError",
                        "InvalidCredentials",
                        "Username/email sau parolă incorecte"
                );
            }

            User user = userOpt.get();
            long code = 100000 + new Random().nextInt(900000);
            user.setCodeSession(code);
            userRepository.update(user);

            String token = JwtUtil.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Logger.success("Autentificare reușită pentru utilizatorul cu ID: " + user.getId());
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<String, Object> userRegister(InputStream is) throws CustomException {
        if (is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru înregistrare"
            );
        }


        try {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            User userInput = JsonUtil.fromJson(requestBody, User.class);
            String username = userInput.getUsername() != null ? userInput.getUsername().trim() : "";
            String email = userInput.getEmail() != null ? userInput.getEmail().trim() : "";
            String password = userInput.getPassword() != null ? userInput.getPassword().trim() : "";

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncompleteData",
                        "Username, email și parolă sunt necesare"
                );
            }

            if (username.length() < 5 || password.length() < 7 || email.length() < 10 || !validateEmail(email)) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidFormat",
                        "Format invalid: username ≥ 5, parolă ≥ 7, email valid"
                );
            }

            if (userRepository.findByEmail(email).isPresent()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "DuplicateEmail",
                        "Email-ul este deja folosit"
                );
            }

            if (userRepository.findByUsername(username).isPresent()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "DuplicateUsername",
                        "Username-ul este deja folosit"
                );
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            long sessionCode = 100000 + new Random().nextInt(900000);

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(hashedPassword);
            newUser.setCodeSession(sessionCode);
            newUser.setCodeReset(0L);

            setupDefaultProfileImage(newUser);

            sendConfirmationEmail(newUser);
            Logger.debug("Email de confirmare trimis la: " + email);

            userRepository.save(newUser);

            String token = JwtUtil.generateToken(newUser);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Logger.success("Înregistrare reușită pentru utilizatorul cu ID: " + newUser.getId());
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }  catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "UnexpectedError",
                    "RegistrationError",
                    "Eroare neașteptată la înregistrare: " + e.getMessage(),
                    e
            );
        }
    }

    private void setupDefaultProfileImage(User newUser) throws IOException {
        String defaultImagePath = getPath() + "/default_profile_image/picture.png";
        newUser.setProfilePicture(defaultImagePath);

        Path userDir = Paths.get(getPath()).resolve("users");
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        Path sourceImage = Paths.get(defaultImagePath);
        String uniqueFilename = UUID.randomUUID() + ".png";
        Path destinationImage = userDir.resolve(uniqueFilename);

        if (Files.exists(sourceImage)) {
            Files.copy(sourceImage, destinationImage, StandardCopyOption.REPLACE_EXISTING);
            newUser.setProfilePicture(destinationImage.toString());
        } else {
            Logger.warning("Imaginea de profil implicită nu există la calea: " + sourceImage);
        }
    }

    public Map<String, Object> resetRequest(InputStream is) throws CustomException {
        if (is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru resetarea parolei"
            );
        }


        try {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            Map<String, String> requestMap = JsonUtil.fromJson(requestBody, Map.class);
            String email = requestMap.get("email");

            if (email == null || email.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "MissingEmail",
                        "Email-ul este necesar"
                );
            }

            if (email.length() < 10 || !validateEmail(email)) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidEmail",
                        "Email invalid"
                );
            }

            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "EmailNotFound",
                        "Nu există niciun utilizator cu acest email"
                );
            }

            User user = userOptional.get();

            Long resetCode = 100000 + new Random().nextLong(900000);
            user.setCodeReset(resetCode);
            Logger.debug("Cod de resetare generat pentru utilizatorul cu ID: " + user.getId());

            userRepository.update(user);

            // Trimite email-ul cu codul de resetare
            sendResetCodeMail(user.getEmail(), resetCode.toString());
            Logger.debug("Email de resetare trimis la: " + email);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Codul de resetare a fost trimis");

            Logger.success("Cerere de resetare a parolei procesată cu succes pentru utilizatorul cu ID: " + user.getId());
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<String, Object> resetPassword(String path, InputStream is) throws CustomException {
        if (path == null || is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru resetarea parolei"
            );
        }


        try {
            String prefix = "/auth/reset-password/";
            if (!path.startsWith(prefix)) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidPath",
                        "Path invalid"
                );
            }

            String codeText = path.substring(prefix.length()).trim();
            if (!codeText.matches("\\d{6}")) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidResetCode",
                        "Codul de resetare trebuie să fie un număr din 6 cifre"
                );
            }

            long code = Long.parseLong(codeText);

            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            User input = JsonUtil.fromJson(requestBody, User.class);
            String email = input.getEmail() != null ? input.getEmail().trim() : "";
            String newPassword = input.getPassword() != null ? input.getPassword().trim() : "";


            if (email.isEmpty() || newPassword.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncompleteData",
                        "Email-ul și parola sunt necesare"
                );
            }

            if (email.length() < 10 || !validateEmail(email)) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidEmail",
                        "Email invalid"
                );
            }

            if (newPassword.length() < 7) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidPassword",
                        "Parola trebuie să conțină cel puțin 7 caractere"
                );
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "EmailNotFound",
                        "Email-ul nu există"
                );
            }

            User user = userOpt.get();

            if (user.getCodeReset() != code) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidResetCode",
                        "Codul de resetare este incorect"
                );
            }

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedPassword);
            user.setCodeReset(0L);
            Logger.debug("Parolă actualizată pentru utilizatorul cu ID: " + user.getId());

            userRepository.update(user);

            String token = JwtUtil.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Logger.success("Parolă resetată cu succes pentru utilizatorul cu ID: " + user.getId());
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<String, Object> changePassword(Long id, InputStream is) throws CustomException {
        if (id == null || is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru schimbarea parolei"
            );
        }


        try {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            Map<String, String> passwordData = JsonUtil.fromJson(requestBody, Map.class);
            String currentPassword = passwordData.get("password");
            String newPassword = passwordData.get("newPassword");

            if (currentPassword == null || currentPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncompleteData",
                        "Parola curentă și cea nouă sunt necesare"
                );
            }

            if (newPassword.length() < 7) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidPassword",
                        "Parola nouă trebuie să conțină cel puțin 7 caractere"
                );
            }

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                Logger.exception("NotFoundException");
                Logger.error("Utilizatorul cu ID " + id + " nu a fost găsit");
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "UserNotFound",
                        "Utilizator negăsit"
                );
            }

            User user = userOpt.get();

            if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
                Logger.exception("ValidationException");
                Logger.error("Parolă curentă incorectă pentru utilizatorul cu ID: " + id);
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncorrectPassword",
                        "Parola curentă este incorectă"
                );
            }

            String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.setPassword(hashedNewPassword);

            userRepository.update(user);

            String token = JwtUtil.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Logger.success("Parolă schimbată cu succes pentru utilizatorul cu ID: " + id);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<String, Object> changeEmail(Long id, InputStream is) throws CustomException {
        if (id == null || is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru schimbarea email-ului"
            );
        }


        try {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            Map<String, String> emailData = JsonUtil.fromJson(requestBody, Map.class);
            String password = emailData.get("password");
            String newEmail = emailData.get("newEmail");

            if (password == null || password.isEmpty() || newEmail == null || newEmail.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncompleteData",
                        "Parola și noul email sunt obligatorii"
                );
            }

            if (newEmail.length() < 10 || !validateEmail(newEmail)) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidEmail",
                        "Email invalid"
                );
            }

            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "DuplicateEmail",
                        "Email-ul este deja folosit de alt utilizator"
                );
            }

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "UserNotFound",
                        "Utilizator negăsit"
                );
            }

            User user = userOpt.get();

            if (!BCrypt.checkpw(password, user.getPassword())) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncorrectPassword",
                        "Parola este incorectă"
                );
            }

            user.setEmail(newEmail);
            Logger.debug("Email actualizat pentru utilizatorul cu ID: " + id);

            userRepository.update(user);

            String token = JwtUtil.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Logger.success("Email schimbat cu succes pentru utilizatorul cu ID: " + id);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<String, Object> changeUsername(Long id, InputStream is) throws CustomException {
        if (id == null || is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru schimbarea username-ului"
            );
        }


        try {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            Map<String, String> usernameData = JsonUtil.fromJson(requestBody, Map.class);
            String password = usernameData.get("password");
            String newUsername = usernameData.get("newUsername");

            if (password == null || password.isEmpty() || newUsername == null || newUsername.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncompleteData",
                        "Parola și noul nume de utilizator sunt obligatorii"
                );
            }

            if (newUsername.length() < 5) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidUsername",
                        "Numele de utilizator trebuie să conțină cel puțin 5 caractere"
                );
            }

            if (userRepository.findByUsername(newUsername).isPresent()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "DuplicateUsername",
                        "Numele de utilizator este deja folosit"
                );
            }

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "UserNotFound",
                        "Utilizator negăsit"
                );
            }

            User user = userOpt.get();

            if (!BCrypt.checkpw(password, user.getPassword())) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncorrectPassword",
                        "Parola este incorectă"
                );
            }


            user.setUsername(newUsername);
            userRepository.update(user);

            updateUserDirectories( newUsername);

            String token = JwtUtil.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);

            Logger.success("Username schimbat cu succes pentru utilizatorul cu ID: " + id);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    private void updateUserDirectories( String newUsername) {
        Path oldUserDir = Paths.get(getPath() +  File.separator+"users");
        Path newUserDir = Paths.get(getPath() +  File.separator+"users");

        if (Files.exists(oldUserDir)) {
            try {
                if (!Files.exists(newUserDir)) {
                    Files.createDirectories(newUserDir);
                    Logger.debug("Creat director nou pentru utilizatorul: " + newUsername);
                }

                Files.list(oldUserDir).forEach(file -> {
                    try {
                        Path destination = newUserDir.resolve(file.getFileName());
                        Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        Logger.warning("Eroare la copierea fișierului " + file.getFileName() + ": " + e.getMessage());
                    }
                });

                deleteDirectory(oldUserDir);
            } catch (IOException e) {
                Logger.warning("Eroare la actualizarea directoarelor utilizatorului: " + e.getMessage());
            }
        }
    }

    public Map<String, Object> changePicture(Long id, String username, InputStream is, String contentType) throws CustomException {
        if (id == null || username == null || is == null || contentType == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru schimbarea imaginii de profil"
            );
        }

        if (!contentType.startsWith("multipart/form-data")) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidContentType",
                    "Trebuie să încarci un fișier prin multipart/form-data"
            );
        }

        Logger.info("Procesare cerere de schimbare a imaginii de profil pentru utilizatorul cu ID: " + id);

        try {
            MultipartParser parser = new MultipartParser(is, contentType);
            MultipartParser.MultipartFile file = parser.getFile("file");

            if (file == null) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "MissingFile",
                        "Nu a fost găsit niciun fișier în cerere"
                );
            }

            String filename = file.getFilename();
            String extension = file.getFileExtension(filename).toLowerCase();

            if (!Arrays.asList("jpg", "jpeg", "png", "gif").contains(extension)) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidFileFormat",
                        "Format fișier neacceptat. Sunt acceptate doar: JPG, JPEG, PNG, GIF"
                );
            }

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "UserNotFound",
                        "Utilizator negăsit"
                );
            }
            User user = userOpt.get();

            Path usersDir = Paths.get(getPath(), "users");
            if (!Files.exists(usersDir)) {
                Files.createDirectories(usersDir);
            }

            String uniqueFilename = user.getUsername() + "_" + UUID.randomUUID() + "." + extension;
            Path destination = usersDir.resolve(uniqueFilename);
            Logger.debug("Destinație fișier imagine: " + destination);

            String oldProfilePicture = user.getProfilePicture();
            String defaultImagePath = Paths.get(getPath(), "default_profile_image", "picture.png")
                    .toString().replace('\\',  File.separator.charAt(0));

            if (oldProfilePicture != null && !oldProfilePicture.equals(defaultImagePath)) {
                try {
                    Path oldImagePath = Paths.get(oldProfilePicture.replace('/', '\\'));
                    if (Files.exists(oldImagePath)) {
                        Files.deleteIfExists(oldImagePath);
                    }
                } catch (IOException e) {
                    Logger.warning("Nu s-a putut șterge imaginea veche: " + e.getMessage());
                }
            }

            try (InputStream fileStream = file.getInputStream()) {
                Files.copy(fileStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            String newImagePath = destination.toString().replace('\\',  File.separator.charAt(0));
            user.setProfilePicture(newImagePath);
            userRepository.update(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Imaginea de profil a fost schimbată cu succes");
            response.put("imagePath", newImagePath);

            Logger.success("Imagine de profil schimbată cu succes pentru utilizatorul cu ID: " + id);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "FileError",
                    "ImageProcessingError",
                    "Eroare la procesarea imaginii: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<String, Object> deleteUser(Long id, InputStream is) throws CustomException {
        if (id == null || is == null) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "InvalidRequest",
                    "Cerere invalidă pentru ștergerea utilizatorului"
            );
        }


        try {
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            if (requestBody.trim().isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "EmptyRequest",
                        "Corpul cererii este gol"
                );
            }

            Map<String, String> deleteData = JsonUtil.fromJson(requestBody, Map.class);
            if (deleteData == null) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "InvalidJsonFormat",
                        "Format JSON invalid"
                );
            }

            String password = deleteData.get("password");
            if (password == null || password.isEmpty()) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "MissingPassword",
                        "Parola este necesară pentru confirmarea ștergerii contului"
                );
            }

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                throw new Exception400.NotFoundException(
                        "ResourceNotFound",
                        "UserNotFound",
                        "Utilizator negăsit"
                );
            }

            User user = userOpt.get();

            if (!BCrypt.checkpw(password, user.getPassword())) {
                throw new Exception400.BadRequestException(
                        "ValidationError",
                        "IncorrectPassword",
                        "Parola este incorectă"
                );
            }

            deleteUserProfileImage(user);

            userRepository.delete(id);
            Logger.debug("Utilizator șters din baza de date: " + id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Utilizatorul a fost șters cu succes");

            Logger.success("Utilizator șters cu succes: " + id);
            return response;
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "IOError",
                    "RequestReadError",
                    "Eroare la procesarea datelor: " + e.getMessage(),
                    e
            );
        }
    }

    private void deleteUserProfileImage(User user) {
        String profilePicture = user.getProfilePicture();
        String defaultImagePath = Paths.get(getPath(), "default_profile_image", "picture.png")
                .toString().replace('\\',  File.separator.charAt(0));

        if (profilePicture != null && !profilePicture.equals(defaultImagePath)) {
            try {
                Path imagePath = Paths.get(profilePicture.replace('/', '\\'));
                if (Files.exists(imagePath)) {
                    Files.deleteIfExists(imagePath);
                    Logger.debug("Imaginea utilizatorului a fost ștearsă: " + imagePath);
                }
            } catch (IOException e) {
                Logger.warning("Nu s-a putut șterge imaginea utilizatorului: " + e.getMessage());
            }
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    deleteDirectory(path);
                } else {
                    Files.delete(path);
                }
            }
        }

        Files.delete(directory);
        Logger.debug("Director șters: " + directory);
    }
}