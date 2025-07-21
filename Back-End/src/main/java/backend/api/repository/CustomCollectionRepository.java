package backend.api.repository;

import backend.api.config.databaseConfig.DatabaseManager;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.CustomCollection;

import java.sql.*;
import java.util.Optional;


public class CustomCollectionRepository {

    public void save(CustomCollection customCollection) throws CustomException {
        if (customCollection == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "NullCustomCollectionError",
                    "Nu se poate salva o configurație personalizată null"
            );
        }

        String sql = "INSERT INTO custom_collections (id_colectie, material, valoare, greutate, " +
                "nume_artist, tematica, gen, casa_discuri, tara, an, stare, raritate, pret_achizitie) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            prepareCustomCollectionStatement(pstmt, customCollection);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new Exception500.InternalServerErrorException(
                        "DatabaseError",
                        "InsertFailedError",
                        "Salvarea configurației personalizate a eșuat: nicio linie afectată"
                );
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    Logger.success("Configurație personalizată salvată cu succes, ID: " + id);
                } else {
                    throw new Exception500.InternalServerErrorException(
                            "DatabaseError",
                            "InsertFailedError",
                            "Nu s-a putut obține ID-ul configurației personalizate create"
                    );
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la salvarea configurației personalizate: " + e.getMessage(),
                    e
            );
        }
    }

    public void update(CustomCollection customCollection) throws CustomException {
        if (customCollection == null || customCollection.getId() == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidCustomCollectionError",
                    "Nu se poate actualiza o configurație personalizată invalidă"
            );
        }

        String sql = "UPDATE custom_collections SET " +
                "material = ?, valoare = ?, greutate = ?, nume_artist = ?, " +
                "tematica = ?, gen = ?, casa_discuri = ?, tara = ?, an = ?, " +
                "stare = ?, raritate = ?, pret_achizitie = ? WHERE id = ?";


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int i = 1;
            pstmt.setBoolean(i++, customCollection.getMaterial());
            pstmt.setBoolean(i++, customCollection.getValoare());
            pstmt.setBoolean(i++, customCollection.getGreutate());
            pstmt.setBoolean(i++, customCollection.getNumeArtist());
            pstmt.setBoolean(i++, customCollection.getTematica());
            pstmt.setBoolean(i++, customCollection.getGen());
            pstmt.setBoolean(i++, customCollection.getCasaDiscuri());
            pstmt.setBoolean(i++, customCollection.getTara());
            pstmt.setBoolean(i++, customCollection.getAn());
            pstmt.setBoolean(i++, customCollection.getStare());
            pstmt.setBoolean(i++, customCollection.getRaritate());
            pstmt.setBoolean(i++, customCollection.getPretAchizitie());
            pstmt.setLong(i, customCollection.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                Logger.warning("Actualizarea configurației personalizate cu ID " +
                        customCollection.getId() + " nu a modificat nicio linie");
            } else {
                Logger.success("Configurație personalizată actualizată cu succes, ID: " + customCollection.getId());
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la actualizarea configurației personalizate: " + e.getMessage(),
                    e
            );
        }
    }

    public void delete(Long id) throws CustomException {
        if (id == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidIdError",
                    "Nu se poate șterge o configurație personalizată cu ID null"
            );
        }

        String sql = "DELETE FROM custom_collections WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                Logger.warning("Ștergerea configurației personalizate cu ID " + id + " nu a modificat nicio linie");
            } else {
                Logger.success("Configurație personalizată ștearsă cu succes, ID: " + id);
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la ștergerea configurației personalizate: " + e.getMessage(),
                    e
            );
        }
    }

    public void deleteByCollectionId(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidCollectionIdError",
                    "Nu se pot șterge configurații personalizate pentru o colecție cu ID null"
            );
        }

        String sql = "DELETE FROM custom_collections WHERE id_colectie = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, collectionId);
            int affectedRows = pstmt.executeUpdate();

            Logger.debug("S-au șters " + affectedRows + " configurații personalizate pentru colecția cu ID: " + collectionId);
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la ștergerea configurațiilor personalizate: " + e.getMessage(),
                    e
            );
        }
    }

    public Optional<CustomCollection> findByCollectionId(Long collectionId) throws CustomException {
        if (collectionId == null) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "InvalidCollectionIdError",
                    "Nu se poate găsi configurație personalizată pentru o colecție cu ID null"
            );
        }

        String sql;
        sql = "SELECT * FROM custom_collections WHERE id_colectie = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, collectionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CustomCollection customCollection = mapResultSetToCustomCollection(rs);
                    Logger.debug("Configurație personalizată găsită pentru colecția cu ID: " + collectionId);
                    return Optional.of(customCollection);
                } else {
                    Logger.warning("Nu s-a găsit configurație personalizată pentru colecția cu ID: " + collectionId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la căutarea configurației personalizate: " + e.getMessage(),
                    e
            );
        }
    }

    private void prepareCustomCollectionStatement(PreparedStatement pstmt, CustomCollection customCollection) throws SQLException {
        pstmt.setLong(1, customCollection.getIdColectie());
        pstmt.setBoolean(2, customCollection.getMaterial());
        pstmt.setBoolean(3, customCollection.getValoare());
        pstmt.setBoolean(4, customCollection.getGreutate());
        pstmt.setBoolean(5, customCollection.getNumeArtist());
        pstmt.setBoolean(6, customCollection.getTematica());
        pstmt.setBoolean(7, customCollection.getGen());
        pstmt.setBoolean(8, customCollection.getCasaDiscuri());
        pstmt.setBoolean(9, customCollection.getTara());
        pstmt.setBoolean(10, customCollection.getAn());
        pstmt.setBoolean(11, customCollection.getStare());
        pstmt.setBoolean(12, customCollection.getRaritate());
        pstmt.setBoolean(13, customCollection.getPretAchizitie());
    }

    private CustomCollection mapResultSetToCustomCollection(ResultSet rs) throws SQLException {
        CustomCollection customCollection = new CustomCollection();
        customCollection.setId(rs.getLong("id"));
        customCollection.setIdColectie(rs.getLong("id_colectie"));
        customCollection.setMaterial(rs.getBoolean("material"));
        customCollection.setValoare(rs.getBoolean("valoare"));
        customCollection.setGreutate(rs.getBoolean("greutate"));
        customCollection.setNumeArtist(rs.getBoolean("nume_artist"));
        customCollection.setTematica(rs.getBoolean("tematica"));
        customCollection.setGen(rs.getBoolean("gen"));
        customCollection.setCasaDiscuri(rs.getBoolean("casa_discuri"));
        customCollection.setTara(rs.getBoolean("tara"));
        customCollection.setAn(rs.getBoolean("an"));
        customCollection.setStare(rs.getBoolean("stare"));
        customCollection.setRaritate(rs.getBoolean("raritate"));
        customCollection.setPretAchizitie(rs.getBoolean("pret_achizitie"));
        return customCollection;
    }
}