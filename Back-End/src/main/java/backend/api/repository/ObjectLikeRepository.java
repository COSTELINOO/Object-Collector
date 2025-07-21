package backend.api.repository;

import backend.api.config.databaseConfig.DatabaseManager;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class ObjectLikeRepository {

    public void addLike(Long objectId, Long userId) throws CustomException {
        if (objectId == null || userId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidParametersError",
                    "Parametri invalizi pentru adăugarea unei aprecieri"
            );
        }

        String sql = "INSERT INTO likes (id_obiect, data, ora, id_user) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, objectId);
            pstmt.setDate(2, new Date(System.currentTimeMillis()));
            pstmt.setTime(3, new Time(System.currentTimeMillis()));
            pstmt.setLong(4, userId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception500.InternalServerErrorException(
                        "DatabaseError",
                        "InsertFailedError",
                        "Adăugarea aprecierii a eșuat: nicio linie afectată"
                );
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    Logger.success("Apreciere adăugată cu succes, ID: " + id);
                } else {
                    throw new Exception500.InternalServerErrorException(
                            "DatabaseError",
                            "InsertFailedError",
                            "Nu s-a putut obține ID-ul aprecierii create"
                    );
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la adăugarea aprecierii: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getDistinctLikeCount(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea numărului de aprecieri unice"
            );
        }

        String sql = "SELECT COUNT(DISTINCT id_user) FROM likes WHERE id_obiect = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long count;
                    count = rs.getLong(1);
                    return count;
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea numărului de aprecieri unice: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getTopLikerId(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea top liker-ului"
            );
        }

        String sql = """
        SELECT id_user
        FROM likes
        WHERE id_obiect = ?
        GROUP BY id_user
        ORDER BY COUNT(*) DESC
        LIMIT 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Long userId = rs.getLong("id_user");
                    Logger.debug("Top liker pentru obiectul cu ID " + objectId + ": utilizatorul cu ID " + userId);
                    return userId;
                }
                Logger.warning("Nu există aprecieri pentru obiectul cu ID: " + objectId);
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea top liker-ului: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getLikeCount(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea numărului de aprecieri"
            );
        }

        String sql = "SELECT COUNT(*) FROM likes WHERE id_obiect = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Long count = rs.getLong(1);
                    Logger.debug("Număr total de aprecieri pentru obiectul cu ID " + objectId + ": " + count);
                    return count;
                }
                Logger.warning("Nu există vizualizari pentru obiectul cu ID: " + objectId);
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea numărului total de aprecieri: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getLastLikerId(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea ultimului liker"
            );
        }

        String sql = """
        SELECT id_user
        FROM likes
        WHERE id_obiect = ?
        ORDER BY data DESC, ora DESC
        LIMIT 1
        """;


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Long userId = rs.getLong("id_user");
                    Logger.debug("Ultimul liker pentru obiectul cu ID " + objectId + ": utilizatorul cu ID " + userId);
                    return userId;
                }
                Logger.debug("Nu există aprecieri pentru obiectul cu ID: " + objectId);
                return null;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea ultimului liker: " + e.getMessage(),
                    e
            );
        }
    }

    public LocalDate getLastLikerData(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea datei ultimei aprecieri"
            );
        }

        String sql;
        sql = """
        SELECT *
        FROM likes
        WHERE id_obiect = ?
        ORDER BY data DESC, ora DESC
        LIMIT 1
        """;


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Date date = rs.getDate("data");
                    if (date != null) {
                        LocalDate localDate = date.toLocalDate();
                        Logger.debug("Data ultimei aprecieri pentru obiectul cu ID " + objectId + ": " + localDate);
                        return localDate;
                    }
                }
                Logger.warning("Nu există aprecieri pentru obiectul cu ID: " + objectId);
                return null;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea datei ultimei aprecieri: " + e.getMessage(),
                    e
            );
        }
    }

    public LocalTime getLastLikerOra(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea orei ultimei aprecieri"
            );
        }

        String sql;
        sql = """
        SELECT *
        FROM likes
        WHERE id_obiect = ?
        ORDER BY data DESC, ora DESC
        LIMIT 1
        """;


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Time time = rs.getTime("ora");
                    if (time != null) {
                        LocalTime localTime = time.toLocalTime();
                        Logger.debug("Ora ultimei aprecieri pentru obiectul cu ID " + objectId + ": " + localTime);
                        return localTime;
                    }
                }
                Logger.warning("Nu există aprecieri pentru obiectul cu ID: " + objectId);
                return null;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea orei ultimei aprecieri: " + e.getMessage(),
                    e
            );
        }
    }

    public void deleteAllByObjectId(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru ștergerea aprecierilor"
            );
        }

        String sql = "DELETE FROM likes WHERE id_obiect = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);
            int affectedRows = pstmt.executeUpdate();

            Logger.debug("S-au șters " + affectedRows + " aprecieri pentru obiectul cu ID: " + objectId);
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la ștergerea aprecierilor: " + e.getMessage(),
                    e
            );
        }
    }
}