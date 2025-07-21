package backend.api.repository;

import backend.api.config.databaseConfig.DatabaseManager;
import backend.api.exception.CustomException;
import backend.api.exception.Exception400;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.Collection;
import backend.api.model.User;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;


public class UserRepository {

    private final CollectionRepository collectionRepository;

    public UserRepository() {
        this.collectionRepository = new CollectionRepository();
    }

    public void save(User user) throws CustomException {
        if (user == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "NullUserError",
                    "Nu se poate salva un utilizator null"
            );
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingUsernameError",
                    "Username-ul nu poate fi gol"
            );
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new Exception400.BadRequestException(
                    "ValidationError",
                    "MissingEmailError",
                    "Email-ul nu poate fi gol"
            );
        }

        String sql = "INSERT INTO users(username, password, email, cod_token, cod_resetare, image) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getCodeSession());
            pstmt.setLong(5, user.getCodeReset());
            pstmt.setString(6, user.getProfilePicture());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    Logger.success("Utilizator salvat cu succes, ID: " + user.getId());
                } else {
                    throw new Exception500.InternalServerErrorException(
                            "DatabaseError",
                            "InsertFailedError",
                            "Nu s-a putut obține ID-ul utilizatorului creat"
                    );
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                if (e.getMessage().contains("username")) {
                    throw new Exception400.ConflictException(
                            "UniqueConstraintError",
                            "DuplicateUsername",
                            "Username-ul este deja utilizat"
                    );
                } else if (e.getMessage().contains("email")) {
                    throw new Exception400.ConflictException(
                            "UniqueConstraintError",
                            "DuplicateEmail",
                            "Email-ul este deja utilizat"
                    );
                }
            }

            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la salvarea utilizatorului: " + e.getMessage(),
                    e
            );
        }
    }

    public void update(User user) throws CustomException {
        if (user == null || user.getId() == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidUserError",
                    "Nu se poate actualiza un utilizator invalid"
            );
        }

        String sql = "UPDATE users SET username = ?, password = ?, email = ?, cod_token = ?, cod_resetare = ?, image = ?, updated_at=CURRENT_TIMESTAMP " +
                "WHERE id = ?";


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setLong(4, user.getCodeSession());
            pstmt.setLong(5, user.getCodeReset());
            pstmt.setString(6, user.getProfilePicture());
            pstmt.setLong(7, user.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                Logger.warning("Actualizarea utilizatorului cu ID " + user.getId() + " nu a modificat nicio linie");
            } else {
                Logger.success("Utilizator actualizat cu succes, ID: " + user.getId());
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                if (e.getMessage().contains("username")) {
                    throw new Exception400.ConflictException(
                            "UniqueConstraintError",
                            "DuplicateUsername",
                            "Username-ul este deja utilizat"
                    );
                } else if (e.getMessage().contains("email")) {
                    throw new Exception400.ConflictException(
                            "UniqueConstraintError",
                            "DuplicateEmail",
                            "Email-ul este deja utilizat"
                    );
                }
            }

            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la actualizarea utilizatorului: " + e.getMessage(),
                    e
            );
        }
    }

    public LocalDate getCreatedAt(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate obține data creării pentru un utilizator cu ID null"
            );
        }

        String sql = "SELECT created_at FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("created_at");
                    if (timestamp != null) {
                        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
                        Logger.debug("Data creării pentru utilizatorul cu ID " + id + ": " + localDate);
                        return localDate;
                    }
                }
                Logger.warning("Nu s-a găsit data creării pentru utilizatorul cu ID: " + id);
                return null;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea datei creării: " + e.getMessage(),
                    e
            );
        }
    }

    public Long likes(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se pot număra aprecierile unui utilizator cu ID null"
            );
        }

        List<Collection> collections = collectionRepository.findAllByUserId(id);

        Long count = collections.parallelStream()
                .mapToLong(collection -> {
                    try {
                        return collectionRepository.getLikes(collection.getId());
                    } catch (CustomException e) {
                        Logger.warning("Eroare la numărarea aprecierilor pentru colecția " + collection.getId() + ": " + e.getMessage());
                        return 0L;
                    }
                })
                .sum();

        Logger.debug("Număr aprecieri pentru utilizatorul cu ID " + id + ": " + count);
        return count;
    }

    public Long views(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se pot număra vizualizările unui utilizator cu ID null"
            );
        }

        List<Collection> collections = collectionRepository.findAllByUserId(id);

        Long count = collections.parallelStream()
                .mapToLong(collection -> {
                    try {
                        return collectionRepository.getViews(collection.getId());
                    } catch (CustomException e) {
                        Logger.warning("Eroare la numărarea vizualizărilor pentru colecția " + collection.getId() + ": " + e.getMessage());
                        return 0L;
                    }
                })
                .sum();

        Logger.debug("Număr vizualizări pentru utilizatorul cu ID " + id + ": " + count);
        return count;
    }

    public Double value(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate calcula valoarea pentru un utilizator cu ID null"
            );
        }

        List<Collection> collections = collectionRepository.findAllByUserId(id);

        Double value = collections.parallelStream()
                .mapToDouble(collection -> {
                    try {
                        return collectionRepository.getValue(collection.getId());
                    } catch (CustomException e) {
                        Logger.warning("Eroare la calcularea valorii pentru colecția " + collection.getId() + ": " + e.getMessage());
                        return 0.0;
                    }
                })
                .sum();

        Logger.debug("Valoare totală pentru utilizatorul cu ID " + id + ": " + value);
        return value;
    }

    public void delete(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate șterge un utilizator cu ID null"
            );
        }

        Logger.debug("Ștergere utilizator cu ID: " + id);
        Connection conn = null;

        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Identificam toate colectiile utilizatorului
            List<Long> collectionIds = new ArrayList<>();
            try (PreparedStatement collectionsQuery = conn.prepareStatement("SELECT id FROM collections WHERE id_user = ?")) {
                collectionsQuery.setLong(1, id);
                try (ResultSet rs = collectionsQuery.executeQuery()) {
                    while (rs.next()) {
                        collectionIds.add(rs.getLong("id"));
                    }
                }
            }

            // 2. Procesam colectiile in batch pentru eficienta sporita
            if (!collectionIds.isEmpty()) {
                try (
                        PreparedStatement objLikesStmt = conn.prepareStatement(
                                "DELETE FROM likes WHERE id_obiect IN (SELECT id FROM objects WHERE id_colectie = ?)");
                        PreparedStatement objViewsStmt = conn.prepareStatement(
                                "DELETE FROM views WHERE id_obiect IN (SELECT id FROM objects WHERE id_colectie = ?)");
                        PreparedStatement customCollStmt = conn.prepareStatement(
                                "DELETE FROM custom_collections WHERE id_colectie = ?");
                        PreparedStatement objStmt = conn.prepareStatement(
                                "DELETE FROM objects WHERE id_colectie = ?")
                ) {

                    for (Long collectionId : collectionIds) {
                        objLikesStmt.setLong(1, collectionId);
                        objLikesStmt.addBatch();

                        objViewsStmt.setLong(1, collectionId);
                        objViewsStmt.addBatch();

                        customCollStmt.setLong(1, collectionId);
                        customCollStmt.addBatch();

                        objStmt.setLong(1, collectionId);
                        objStmt.addBatch();
                    }

                    objLikesStmt.executeBatch();
                    objViewsStmt.executeBatch();
                    customCollStmt.executeBatch();
                    objStmt.executeBatch();
                }
            }

            // 3. Restul operatiunilor de stergere
            try (
                    PreparedStatement likesStmt = conn.prepareStatement("DELETE FROM likes WHERE id_user = ?");
                    PreparedStatement viewsStmt = conn.prepareStatement("DELETE FROM views WHERE id_user = ?");
                    PreparedStatement collDeleteStmt = conn.prepareStatement("DELETE FROM collections WHERE id_user = ?");
                    PreparedStatement userDeleteStmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")
            ) {
                likesStmt.setLong(1, id);
                likesStmt.executeUpdate();

                viewsStmt.setLong(1, id);
                viewsStmt.executeUpdate();

                collDeleteStmt.setLong(1, id);
                int collDeleted = collDeleteStmt.executeUpdate();
                Logger.debug("Șterse " + collDeleted + " colecții ale utilizatorului cu ID: " + id);

                userDeleteStmt.setLong(1, id);
                int affectedRows = userDeleteStmt.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    throw new Exception400.NotFoundException(
                            "ResourceNotFound",
                            "UserNotFound",
                            "Nu există utilizator cu ID-ul " + id
                    );
                }
            }

            conn.commit();
            Logger.success("Utilizator șters cu succes, ID: " + id);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    Logger.error("Eroare la rollback: " + rollbackEx.getMessage());
                }
            }

            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la ștergerea utilizatorului: " + e.getMessage(),
                    e
            );
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    Logger.error("Eroare la închiderea conexiunii: " + closeEx.getMessage());
                }
            }
        }
    }

    public Optional<User> findById(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate găsi un utilizator cu ID null"
            );
        }

        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    Logger.debug("Utilizator găsit cu ID: " + id);
                    return Optional.of(user);
                } else {
                    Logger.warning("Nu s-a găsit niciun utilizator cu ID: " + id);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la căutarea utilizatorului: " + e.getMessage(),
                    e
            );
        }
    }

    public Optional<User> findByUsername(String username) throws CustomException {
        if (username == null || username.trim().isEmpty()) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidUsernameError",
                    "Nu se poate găsi un utilizator cu username null sau gol"
            );
        }

        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    Logger.debug("Utilizator găsit cu username: " + username);
                    return Optional.of(user);
                } else {
                    Logger.warning("Nu s-a găsit niciun utilizator cu username: " + username);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la căutarea utilizatorului după username: " + e.getMessage(),
                    e
            );
        }
    }

    public Optional<User> findByEmail(String email) throws CustomException {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidEmailError",
                    "Nu se poate găsi un utilizator cu email null sau gol"
            );
        }

        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    Logger.debug("Utilizator găsit cu email: " + email);
                    return Optional.of(user);
                } else {
                    Logger.warning("Nu s-a găsit niciun utilizator cu email: " + email);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la căutarea utilizatorului după email: " + e.getMessage(),
                    e
            );
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setCodeSession(rs.getLong("cod_token"));
        user.setCodeReset(rs.getLong("cod_resetare"));
        user.setProfilePicture(rs.getString("image"));

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        return user;
    }


}