package backend.api.repository;

import backend.api.config.databaseConfig.DatabaseManager;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.ObjectView;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class ObjectViewRepository {

    public void save(ObjectView view) throws CustomException {
        if (view == null || view.getIdObject() == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidViewError",
                    "Vizualizare invalidă pentru salvare"
            );
        }

        String sql = "INSERT INTO views (id_obiect, data, ora, id_user) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, view.getIdObject());
            pstmt.setDate(2, view.getData());
            pstmt.setTime(3, view.getOra());

            if (view.getIdUser() != null) {
                pstmt.setLong(4, view.getIdUser());
            } else {
                pstmt.setNull(4, java.sql.Types.BIGINT);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception500.InternalServerErrorException(
                        "DatabaseError",
                        "InsertFailedError",
                        "Salvarea vizualizării a eșuat: nicio linie afectată"
                );
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    Logger.success("Vizualizare salvată cu succes, ID: " + id);
                } else {
                    throw new Exception500.InternalServerErrorException(
                            "DatabaseError",
                            "InsertFailedError",
                            "Nu s-a putut obține ID-ul vizualizării create"
                    );
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la salvarea vizualizării: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getViewCount(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea numărului de vizualizări"
            );
        }

        String sql = "SELECT COUNT(*) FROM views WHERE id_obiect = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Long count = rs.getLong(1);
                    Logger.debug("Număr vizualizări pentru obiectul cu ID " + objectId + ": " + count);
                    return count;
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea numărului de vizualizări: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getTopViewerId(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea top viewer-ului"
            );
        }

        String sql = """
        SELECT id_user
        FROM views
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
                    Logger.debug("Top viewer pentru obiectul cu ID " + objectId + ": utilizatorul cu ID " + userId);
                    return userId;
                }
                Logger.warning("Nu există vizualizări pentru obiectul cu ID: " + objectId);
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea top viewer-ului: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getLastViewerId(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea ultimului viewer"
            );
        }

        String sql;
        sql = """
        SELECT *
        FROM views
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
                    if (rs.wasNull()) {
                        Logger.warning("Ultima vizualizare pentru obiectul cu ID " + objectId + " a fost anonimă");
                        return 0L;
                    }
                    Logger.debug("Ultimul viewer pentru obiectul cu ID " + objectId + ": utilizatorul cu ID " + userId);
                    return userId;
                }
                Logger.warning("Nu există vizualizări pentru obiectul cu ID: " + objectId);
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea ultimului viewer: " + e.getMessage(),
                    e
            );
        }
    }

    public LocalDate getLastViewerData(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea datei ultimei vizualizări"
            );
        }

        String sql;
        sql = """
        SELECT *
        FROM views
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
                        Logger.debug("Data ultimei vizualizări pentru obiectul cu ID " + objectId + ": " + localDate);
                        return localDate;
                    }
                }
                Logger.warning("Nu există vizualizări pentru obiectul cu ID: " + objectId);
                return null;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea datei ultimei vizualizări: " + e.getMessage(),
                    e
            );
        }
    }

    public LocalTime getLastViewerOra(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea orei ultimei vizualizări"
            );
        }

        String sql;
        sql = """
        SELECT *
        FROM views
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
                        Logger.debug("Ora ultimei vizualizări pentru obiectul cu ID " + objectId + ": " + localTime);
                        return localTime;
                    }
                }
                Logger.warning("Nu există vizualizări pentru obiectul cu ID: " + objectId);
                return null;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea orei ultimei vizualizări: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getDistinctViewCount(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru obținerea numărului de vizualizări unice"
            );
        }

        String sql = "SELECT COUNT(DISTINCT id_user) FROM views WHERE id_obiect = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Long count = rs.getLong(1);
                    Logger.debug("Număr utilizatori unici care au vizualizat obiectul cu ID " + objectId + ": " + count);
                    return count;
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea numărului de vizualizări unice: " + e.getMessage(),
                    e
            );
        }
    }

    public void deleteAllByObjectId(Long objectId) throws CustomException {
        if (objectId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectIdError",
                    "ID obiect invalid pentru ștergerea vizualizărilor"
            );
        }

        String sql = "DELETE FROM views WHERE id_obiect = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, objectId);
            int affectedRows = pstmt.executeUpdate();

            Logger.debug("S-au șters " + affectedRows + " vizualizări pentru obiectul cu ID: " + objectId);
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la ștergerea vizualizărilor: " + e.getMessage(),
                    e
            );
        }
    }
}