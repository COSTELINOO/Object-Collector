package backend.api.repository;

import backend.api.config.databaseConfig.DatabaseManager;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.Obiect;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.stream.Collectors;


public class ObjectRepository {

    public Long save(Obiect obiect) throws CustomException {
        if (obiect == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "NullObjectError",
                    "Nu se poate salva un obiect null"
            );
        }

        String sql = "INSERT INTO objects (id_colectie, nume_colectie, descriere, material, valoare, greutate, " +
                "nume_artist, tematica, gen, casa_discuri, tara, an, stare, raritate, pret_achizitie, " +
                "image, visibility) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setPreparedStatementParameters(pstmt, obiect);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception500.InternalServerErrorException(
                        "DatabaseError",
                        "InsertFailedError",
                        "Salvarea obiectului a eșuat: nicio linie afectată"
                );
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    Logger.success("Obiect salvat cu succes, ID: " + id);
                    return id;
                } else {
                    throw new Exception500.InternalServerErrorException(
                            "DatabaseError",
                            "InsertFailedError",
                            "Nu s-a putut obține ID-ul obiectului creat"
                    );
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la salvarea obiectului: " + e.getMessage(),
                    e
            );
        }
    }

    public void update(Obiect obiect) throws CustomException {
        if (obiect == null || obiect.getId() == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidObjectError",
                    "Nu se poate actualiza un obiect invalid"
            );
        }

        String sql = "UPDATE objects SET nume_colectie = ?, descriere = ?, material = ?, valoare = ?, " +
                "greutate = ?, nume_artist = ?, tematica = ?, gen = ?, casa_discuri = ?, tara = ?, " +
                "an = ?, stare = ?, raritate = ?, pret_achizitie = ?, image = ?, visibility = ?, " +
                "updated_at = ? WHERE id = ?";


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, obiect.getName());
            pstmt.setString(2, obiect.getDescriere());
            pstmt.setString(3, obiect.getMaterial());
            pstmt.setFloat(4, obiect.getValoare());
            pstmt.setInt(5, obiect.getGreutate());
            pstmt.setString(6, obiect.getNumeArtist());
            pstmt.setString(7, obiect.getTematica());
            pstmt.setString(8, obiect.getGen());
            pstmt.setString(9, obiect.getCasaDiscuri());
            pstmt.setString(10, obiect.getTara());
            pstmt.setDate(11, java.sql.Date.valueOf(obiect.getAn()));
            pstmt.setString(12, obiect.getStare());
            pstmt.setString(13, obiect.getRaritate());
            pstmt.setFloat(14, obiect.getPretAchizitie().floatValue());
            pstmt.setString(15, obiect.getImage());
            pstmt.setBoolean(16, obiect.getVisibility());
            pstmt.setTimestamp(17, obiect.getUpdatedAt() != null ?
                    obiect.getUpdatedAt() : new Timestamp(System.currentTimeMillis()));
            pstmt.setLong(18, obiect.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                Logger.warning("Actualizarea obiectului cu ID " + obiect.getId() + " nu a modificat nicio linie");
            } else {
                Logger.success("Obiect actualizat cu succes, ID: " + obiect.getId());
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la actualizarea obiectului: " + e.getMessage(),
                    e
            );
        }
    }

    public void delete(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate șterge un obiect cu ID null"
            );
        }

        String sql = "DELETE FROM objects WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                Logger.warning("Ștergerea obiectului cu ID " + id + " nu a modificat nicio linie");
            } else {
                Logger.success("Obiect șters cu succes, ID: " + id);
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la ștergerea obiectului: " + e.getMessage(),
                    e
            );
        }
    }

    public Optional<Obiect> findById(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate găsi un obiect cu ID null"
            );
        }

        String sql;
        sql = "SELECT * FROM objects WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Obiect obiect = mapResultSetToObiect(rs);
                    Logger.debug("Obiect găsit cu ID: " + id);
                    return Optional.of(obiect);
                } else {
                    Logger.warning("Nu s-a găsit niciun obiect cu ID: " + id);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la căutarea obiectului: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Obiect> findAllPublic() throws CustomException {
        String sql;
        sql = "SELECT * FROM objects WHERE visibility = true ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<Obiect> tempList = new ArrayList<>();
            while (rs.next()) {
                tempList.add(mapResultSetToObiect(rs));
            }

            List<Obiect> obiecte = tempList.parallelStream()
                    .collect(Collectors.toList());

            Logger.debug("S-au găsit " + obiecte.size() + " obiecte publice");
            return obiecte;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea obiectelor publice: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Obiect> findAllByCollectionId(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidCollectionIdError",
                    "Nu se pot găsi obiecte pentru o colecție cu ID null"
            );
        }

        String sql;
        sql = "SELECT * FROM objects WHERE id_colectie = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, collectionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Obiect> tempList = new ArrayList<>();
                while (rs.next()) {
                    tempList.add(mapResultSetToObiect(rs));
                }

                List<Obiect> obiecte = tempList.parallelStream()
                        .collect(Collectors.toList());

                Logger.debug("S-au găsit " + obiecte.size() + " obiecte în colecția cu ID: " + collectionId);
                return obiecte;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea obiectelor din colecție: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Obiect> findAllPublicByCollectionId(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidCollectionIdError",
                    "Nu se pot găsi obiecte publice pentru o colecție cu ID null"
            );
        }

        String sql;
        sql = "SELECT * FROM objects WHERE id_colectie = ? AND visibility = true ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, collectionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<Obiect> tempList = new ArrayList<>();
                while (rs.next()) {
                    tempList.add(mapResultSetToObiect(rs));
                }

                List<Obiect> obiecte = tempList.parallelStream()
                        .collect(Collectors.toList());

                Logger.debug("S-au găsit " + obiecte.size() + " obiecte publice în colecția cu ID: " + collectionId);
                return obiecte;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea obiectelor publice din colecție: " + e.getMessage(),
                    e
            );
        }
    }

    public void deleteAllByCollectionId(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidCollectionIdError",
                    "Nu se pot șterge obiecte pentru o colecție cu ID null"
            );
        }

        String sql = "DELETE FROM objects WHERE id_colectie = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, collectionId);
            int affectedRows = pstmt.executeUpdate();

            Logger.debug("S-au șters " + affectedRows + " obiecte din colecția cu ID: " + collectionId);
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la ștergerea obiectelor din colecție: " + e.getMessage(),
                    e
            );
        }
    }

    public List<Obiect> findAllByUserId(Long userId) throws CustomException {
        if (userId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidUserIdError",
                    "Nu se pot găsi obiecte pentru un utilizator cu ID null"
            );
        }

        String sql = "SELECT o.* FROM objects o " +
                "JOIN collections c ON o.id_colectie = c.id " +
                "WHERE c.id_user = ? ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                CopyOnWriteArrayList<Obiect> obiecteSafe = new CopyOnWriteArrayList<>();

                while (rs.next()) {
                    obiecteSafe.add(mapResultSetToObiect(rs));
                }

                List<Obiect> obiecte = obiecteSafe.parallelStream()
                        .collect(Collectors.toList());

                Logger.debug("S-au găsit " + obiecte.size() + " obiecte pentru utilizatorul cu ID: " + userId);
                return obiecte;
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la obținerea obiectelor utilizatorului: " + e.getMessage(),
                    e
            );
        }
    }

    public Long countAllObjectsById(Long id) throws CustomException {
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
            }
            return 0L;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la numărarea obiectelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Long countAllObjects() throws CustomException {
        String sql = "SELECT COUNT(*) FROM objects";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                Long count = rs.getLong(1);
                Logger.debug("Număr total obiecte în sistem: " + count);
                return count;
            }
            return 0L;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la numărarea tuturor obiectelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Double getObjectsValue() throws CustomException {
        String sql;
        sql = "SELECT * FROM objects";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            List<Double> valori = new ArrayList<>();
            while (rs.next()) {
                double pret = rs.getDouble("pret_achizitie");
                if (!rs.wasNull() && pret > 0) {
                    valori.add(pret);
                }
            }

            double value = valori.parallelStream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            Logger.debug("Valoare totală a tuturor obiectelor: " + value);
            return value;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la calcularea valorii totale a obiectelor: " + e.getMessage(),
                    e
            );
        }
    }

    private void setPreparedStatementParameters(PreparedStatement pstmt, Obiect obiect) throws SQLException {
        pstmt.setInt(1, obiect.getIdColectie());
        pstmt.setString(2, obiect.getName());
        pstmt.setString(3, obiect.getDescriere());
        pstmt.setString(4, obiect.getMaterial());
        pstmt.setFloat(5, obiect.getValoare());
        pstmt.setInt(6, obiect.getGreutate());
        pstmt.setString(7, obiect.getNumeArtist());
        pstmt.setString(8, obiect.getTematica());
        pstmt.setString(9, obiect.getGen());
        pstmt.setString(10, obiect.getCasaDiscuri());
        pstmt.setString(11, obiect.getTara());
        pstmt.setDate(12, java.sql.Date.valueOf(obiect.getAn()));
        pstmt.setString(13, obiect.getStare());
        pstmt.setString(14, obiect.getRaritate());
        pstmt.setFloat(15, obiect.getPretAchizitie().floatValue());
        pstmt.setString(16, obiect.getImage());
        pstmt.setBoolean(17, obiect.getVisibility());
    }

    private Obiect mapResultSetToObiect(ResultSet rs) throws SQLException {
        Obiect obiect = new Obiect();
        obiect.setId(rs.getLong("id"));
        obiect.setIdColectie(rs.getInt("id_colectie"));
        obiect.setName(rs.getString("nume_colectie"));
        obiect.setDescriere(rs.getString("descriere"));
        obiect.setMaterial(rs.getString("material"));
        obiect.setValoare(rs.getFloat("valoare"));
        obiect.setGreutate(rs.getInt("greutate"));
        obiect.setNumeArtist(rs.getString("nume_artist"));
        obiect.setTematica(rs.getString("tematica"));
        obiect.setGen(rs.getString("gen"));
        obiect.setCasaDiscuri(rs.getString("casa_discuri"));
        obiect.setTara(rs.getString("tara"));

        Date anDate = rs.getDate("an");
        if (anDate != null) {
            obiect.setAn(anDate.toLocalDate());
        }

        obiect.setStare(rs.getString("stare"));
        obiect.setRaritate(rs.getString("raritate"));
        obiect.setPretAchizitie(rs.getFloat("pret_achizitie"));
        obiect.setImage(rs.getString("image"));
        obiect.setVisibility(rs.getBoolean("visibility"));
        obiect.setCreatedAt(rs.getTimestamp("created_at"));
        obiect.setUpdatedAt(rs.getTimestamp("updated_at"));
        return obiect;
    }
}