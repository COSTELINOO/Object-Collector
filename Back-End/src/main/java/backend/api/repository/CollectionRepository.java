package backend.api.repository;

import backend.api.config.databaseConfig.DatabaseManager;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.Collection;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CollectionRepository {

    private final ObjectRepository objectRepository;

    public CollectionRepository() {
        this.objectRepository = new ObjectRepository();
    }

    public Long save(Collection collection) throws CustomException {
        if (collection == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "NullCollectionError",
                    "Nu se poate salva o colecție null"
            );
        }

        String sql = "INSERT INTO collections (id_user, id_tip, nume, visibility) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, collection.getIdUser());
            pstmt.setInt(2, collection.getIdTip());
            pstmt.setString(3, collection.getNume());
            pstmt.setBoolean(4, collection.getVisibility());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception500.InternalServerErrorException(
                        "DatabaseError",
                        "InsertFailedError",
                        "Salvarea colecției a eșuat: nicio linie afectată"
                );
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    Logger.success("Colecție salvată cu succes, ID: " + id);
                    return id;
                } else {
                    throw new Exception500.InternalServerErrorException(
                            "DatabaseError",
                            "InsertFailedError",
                            "Nu s-a putut obține ID-ul colecției create"
                    );
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la salvarea colecției: " + e.getMessage(),
                    e
            );
        }
    }

    public void update(Collection collection) throws CustomException {
        if (collection == null || collection.getId() == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidCollectionError",
                    "Nu se poate actualiza o colecție invalidă"
            );
        }

        String sql = "UPDATE collections SET nume = ?, visibility = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, collection.getNume());
            pstmt.setBoolean(2, collection.getVisibility());
            pstmt.setLong(3, collection.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                Logger.warning("Actualizarea colecției cu ID " + collection.getId() + " nu a modificat nicio linie");
            } else {
                Logger.success("Colecție actualizată cu succes, ID: " + collection.getId());
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la actualizarea colecției: " + e.getMessage(),
                    e
            );
        }
    }

    public void delete(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate șterge o colecție cu ID null"
            );
        }

        String sql = "DELETE FROM collections WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                Logger.warning("Ștergerea colecției cu ID " + id + " nu a modificat nicio linie");
            } else {
                Logger.success("Colecție ștearsă cu succes, ID: " + id);
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la ștergerea colecției: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<Long, Long> getViewsForCollectionIds(List<Long> collectionIds) throws CustomException {
        if (collectionIds == null || collectionIds.isEmpty()) return Collections.emptyMap();
        String inSql = collectionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT o.id_colectie, COUNT(v.id) as views " +
                "FROM objects o LEFT JOIN views v ON o.id = v.id_obiect " +
                "WHERE o.id_colectie IN (" + inSql + ") GROUP BY o.id_colectie";
        Map<Long, Long> result = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < collectionIds.size(); i++) {
                pstmt.setLong(i + 1, collectionIds.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong(1), rs.getLong(2));
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError","SqlException",
                    "Eroare la obținerea numărului de vizualizări: " + e.getMessage(), e
            );
        }
        return result;
    }

    public Map<Long, Double> getValueForCollectionIds(List<Long> collectionIds) throws CustomException {
        if (collectionIds == null || collectionIds.isEmpty()) return Collections.emptyMap();
        String inSql = collectionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id_colectie, SUM(pret_achizitie) as value FROM objects " +
                "WHERE id_colectie IN (" + inSql + ") GROUP BY id_colectie";
        Map<Long, Double> result = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < collectionIds.size(); i++) {
                pstmt.setLong(i + 1, collectionIds.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong(1), rs.getDouble(2));
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError","SqlException",
                    "Eroare la obținerea valorilor: " + e.getMessage(), e
            );
        }
        return result;
    }

    public Map<Long, Long> getCountForCollectionIds(List<Long> collectionIds) throws CustomException {
        if (collectionIds == null || collectionIds.isEmpty()) return Collections.emptyMap();
        String inSql = collectionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id_colectie, COUNT(*) as count FROM objects " +
                "WHERE id_colectie IN (" + inSql + ") GROUP BY id_colectie";
        Map<Long, Long> result = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < collectionIds.size(); i++) {
                pstmt.setLong(i + 1, collectionIds.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong(1), rs.getLong(2));
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError","SqlException",
                    "Eroare la obținerea count-ului: " + e.getMessage(), e
            );
        }
        return result;
    }

    public Map<Long, String> getUsernamesForCollectionIds(List<Long> collectionIds) throws CustomException {
        if (collectionIds == null || collectionIds.isEmpty()) return Collections.emptyMap();
        String inSql = collectionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT c.id, u.username FROM collections c " +
                "JOIN users u ON c.id_user = u.id " +
                "WHERE c.id IN (" + inSql + ")";
        Map<Long, String> result = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < collectionIds.size(); i++) {
                pstmt.setLong(i + 1, collectionIds.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong(1), rs.getString(2));
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError","SqlException",
                    "Eroare la obținerea username-urilor: " + e.getMessage(), e
            );
        }
        return result;
    }

    public Map<Long, Long> getLikesForCollectionIds(List<Long> collectionIds) throws CustomException {
        if (collectionIds == null || collectionIds.isEmpty()) return Collections.emptyMap();
        String inSql = collectionIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT o.id_colectie, COUNT(l.id) as likes " +
                "FROM objects o LEFT JOIN likes l ON o.id = l.id_obiect " +
                "WHERE o.id_colectie IN (" + inSql + ") GROUP BY o.id_colectie";
        Map<Long, Long> result = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < collectionIds.size(); i++) {
                pstmt.setLong(i + 1, collectionIds.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong(1), rs.getLong(2));
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError","SqlException",
                    "Eroare la obținerea numărului de aprecieri: " + e.getMessage(), e
            );
        }
        return result;
    }

    public Long getLikes(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError", "InvalidIdError", "Nu se pot obține aprecieri pentru o colecție cu ID null"
            );
        }
        // SQL: calculeaza suma like-urilor pentru toate obiectele din colecție
        String sql = "SELECT COUNT(*) FROM likes WHERE id_obiect IN (SELECT id FROM objects WHERE id_colectie = ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, collectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long likes = rs.getLong(1);
                    Logger.debug("S-au găsit " + likes + " aprecieri pentru colecția cu ID: " + collectionId);
                    return likes;
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError","SqlException",
                    "Eroare la obținerea numărului de aprecieri: " + e.getMessage(), e
            );
        }
    }

    public String getUsername(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate obține username pentru un utilizator cu ID null"
            );
        }

        String sql = "SELECT username FROM users WHERE id = ?";
        String username = "";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                    Logger.debug("Username găsit pentru utilizatorul cu ID " + id + ": " + username);
                } else {
                    Logger.warning("Nu s-a găsit username pentru utilizatorul cu ID: " + id);
                }
                return username;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea username-ului: " + e.getMessage(),
                    e
            );
        }
    }

    public Long getViews(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError", "InvalidIdError", "Nu se pot obține vizualizări pentru o colecție cu ID null"
            );
        }
        String sql = "SELECT COUNT(*) FROM views WHERE id_obiect IN (SELECT id FROM objects WHERE id_colectie = ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, collectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long views = rs.getLong(1);
                    Logger.debug("S-au găsit " + views + " vizualizări pentru colecția cu ID: " + collectionId);
                    return views;
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError","SqlException",
                    "Eroare la obținerea numărului de vizualizări: " + e.getMessage(), e
            );
        }
    }

    public Double getValue(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError", "InvalidIdError", "Nu se poate obține valoarea pentru o colecție cu ID null"
            );
        }
        String sql = "SELECT SUM(pret_achizitie) FROM objects WHERE id_colectie = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, collectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double value = rs.getDouble(1);
                    Logger.debug("Valoarea totală pentru colecția cu ID " + collectionId + ": " + value);
                    return value;
                }
                return 0.0;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError", "SqlException", "Eroare la obținerea valorii totale: " + e.getMessage(), e
            );
        }
    }

    public Optional<Collection> findById(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate găsi o colecție cu ID null"
            );
        }

        String sql;
        sql = "SELECT * FROM collections WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Collection collection = mapResultSetToCollection(rs);
                    Logger.debug("Colecție găsită cu ID: " + id);
                    return Optional.of(collection);
                } else {
                    Logger.warning("Nu s-a găsit nicio colecție cu ID: " + id);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la căutarea colecției: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Collection> findAllPublic() throws CustomException {
        String sql;
        sql = "SELECT * FROM collections WHERE visibility = true ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {


            List<Collection> tempList = new ArrayList<>();
            while (rs.next()) {
                tempList.add(mapResultSetToCollection(rs));
            }


            List<Collection> collections = tempList.parallelStream()
                    .collect(Collectors.toList());

            Logger.debug("S-au găsit " + collections.size() + " colecții publice");
            return collections;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea colecțiilor publice: " + e.getMessage(),
                    e
            );
        }
    }

    public Long countObjects(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se pot număra obiectele unei colecții cu ID null"
            );
        }

        String sql = "SELECT COUNT(*) FROM objects WHERE id_colectie = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la numărarea obiectelor: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Collection> findAllByUserId(Long userId) throws CustomException {
        if (userId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidUserIdError",
                    "Nu se pot găsi colecțiile unui utilizator cu ID null"
            );
        }

        String sql;
        sql = "SELECT * FROM collections WHERE id_user = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // Optimizare: Folosim o structura thread-safe
                CopyOnWriteArrayList<Collection> collectionsThreadSafe = new CopyOnWriteArrayList<>();

                while (rs.next()) {
                    collectionsThreadSafe.add(mapResultSetToCollection(rs));
                }

                List<Collection> collections = collectionsThreadSafe.parallelStream()
                        .collect(Collectors.toList());

                Logger.debug("S-au găsit " + collections.size() + " colecții pentru utilizatorul cu ID: " + userId);
                return collections;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea colecțiilor utilizatorului: " + e.getMessage(),
                    e
            );
        }
    }

    public Long countAllCollections() throws CustomException {
        String sql = "SELECT COUNT(*) FROM collections";
        Logger.debug("Numărare toate colecțiile");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                Long count = rs.getLong(1);
                Logger.debug("Număr total colecții: " + count);
                return count;
            }
            return 0L;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la numărarea tuturor colecțiilor: " + e.getMessage(),
                    e
            );
        }
    }

    public Long countAllCollectionsByUserId(Long id) throws CustomException {
        String sql = "SELECT COUNT(*) FROM collections where id_user = ?";
        Logger.debug("Numărare toate colecțiile");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setLong(1,id);
            try (  ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    Long count = rs.getLong(1);
                    Logger.debug("Număr total colecții: " + count);
                    return count;
                }
                return 0L;
            } }catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la numărarea tuturor colecțiilor: " + e.getMessage(),
                    e
            );
        }
    }

    public Map<String, Double> countAllTypes() throws CustomException {
        String sql;
        sql = "SELECT * FROM collections";

        Map<String, Double> map = new HashMap<>();
        map.put("MONEDE", 0.0);
        map.put("TABLOURI", 0.0);
        map.put("TIMBRE", 0.0);
        map.put("VINILE", 0.0);
        map.put("CUSTOM", 0.0);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<CollectionWithCount> collections = new ArrayList<>();
            while (rs.next()) {
                CollectionWithCount cwc = new CollectionWithCount();
                cwc.id = rs.getLong("id");
                cwc.typeId = rs.getInt("id_tip");
                collections.add(cwc);
            }

            collections.parallelStream().forEach(cwc -> {
                try {
                    cwc.objectCount = objectRepository.countAllObjectsById(cwc.id);
                } catch (CustomException e) {
                    Logger.warning("Eroare la numărarea obiectelor pentru colecția " + cwc.id + ": " + e.getMessage());
                    cwc.objectCount = 0L;
                }
            });

            for (CollectionWithCount cwc : collections) {
                switch (cwc.typeId) {
                    case 1:
                        map.put("MONEDE", map.get("MONEDE") + cwc.objectCount);
                        break;
                    case 2:
                        map.put("TABLOURI", map.get("TABLOURI") + cwc.objectCount);
                        break;
                    case 3:
                        map.put("TIMBRE", map.get("TIMBRE") + cwc.objectCount);
                        break;
                    case 4:
                        map.put("VINILE", map.get("VINILE") + cwc.objectCount);
                        break;
                    case 5:
                        map.put("CUSTOM", map.get("CUSTOM") + cwc.objectCount);
                        break;
                    default:
                        Logger.warning("Tip de colecție necunoscut: " + cwc.typeId);
                        break;
                }
            }

            Logger.debug("Distribuție calculată cu succes: " + map);
            return map;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la calcularea distribuției: " + e.getMessage(),
                    e
            );
        }
    }

    private Collection mapResultSetToCollection(ResultSet rs) throws SQLException {
        Collection collection = new Collection();
        collection.setId(rs.getLong("id"));
        collection.setIdUser(rs.getLong("id_user"));
        collection.setIdTip(rs.getInt("id_tip"));
        collection.setNume(rs.getString("nume"));
        collection.setVisibility(rs.getBoolean("visibility"));
        collection.setCreatedAt(rs.getTimestamp("created_at"));
        return collection;
    }

    private static class CollectionWithCount {
        Long id;
        int typeId;
        Long objectCount;
    }
}