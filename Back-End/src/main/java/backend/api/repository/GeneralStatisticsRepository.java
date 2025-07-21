package backend.api.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import backend.api.config.databaseConfig.DatabaseManager;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.GlobalStatistics.Clasament;


public class GeneralStatisticsRepository {

    public List<Clasament> getAllTopCollectionsByLikes() throws CustomException {
        String sql = """
            SELECT c.id, c.nume, c.visibility, COALESCE(SUM(like_counts.like_count), 0) as total_likes
            FROM collections c
            LEFT JOIN (
                SELECT o.id_colectie, COUNT(l.id) as like_count
                FROM objects o
                LEFT JOIN likes l ON l.id_obiect = o.id
                GROUP BY o.id_colectie
            ) like_counts ON c.id = like_counts.id_colectie
            GROUP BY c.id, c.nume, c.visibility
            ORDER BY total_likes DESC, c.id
            LIMIT 3
        """;
        return executeCollectionClassamentQuery(sql);
    }

    public List<Clasament> getAllTopCollectionsByViews() throws CustomException {
        String sql = """
            SELECT c.id, c.nume, c.visibility, COALESCE(SUM(view_counts.view_count), 0) as total_views
            FROM collections c
            LEFT JOIN (
                SELECT o.id_colectie, COUNT(v.id) as view_count
                FROM objects o
                LEFT JOIN views v ON v.id_obiect = o.id
                GROUP BY o.id_colectie
            ) view_counts ON c.id = view_counts.id_colectie
            GROUP BY c.id, c.nume, c.visibility
            ORDER BY total_views DESC, c.id
            LIMIT 3
        """;
        return executeCollectionClassamentQuery(sql);
    }

    public List<Clasament> getAllTopCollectionsByValue() throws CustomException {
        String sql = """
            SELECT c.id, c.nume, c.visibility, COALESCE(SUM(o.pret_achizitie), 0) as total_value
            FROM collections c
            LEFT JOIN objects o ON c.id = o.id_colectie
            GROUP BY c.id, c.nume, c.visibility
            ORDER BY total_value DESC, c.id
            LIMIT 3
        """;
        return executeCollectionClassamentQuery(sql);
    }

    public List<Clasament> getAllTopObjectsByLikes() throws CustomException {
        String sql = """
            SELECT o.id, o.nume_colectie as name, o.visibility, COUNT(l.id) as like_count
            FROM objects o
            LEFT JOIN likes l ON o.id = l.id_obiect
            GROUP BY o.id, o.nume_colectie, o.visibility
            ORDER BY like_count DESC, o.id
            LIMIT 3
        """;
        return executeObjectClassamentQuery(sql);
    }

    public List<Clasament> getAllTopObjectsByViews() throws CustomException {
        String sql = """
            SELECT o.id, o.nume_colectie as name, o.visibility, COUNT(v.id) as view_count
            FROM objects o
            LEFT JOIN views v ON o.id = v.id_obiect
            GROUP BY o.id, o.nume_colectie, o.visibility
            ORDER BY view_count DESC, o.id
            LIMIT 3
        """;
        return executeObjectClassamentQuery(sql);
    }

    public List<Clasament> getAllTopObjectsByValue() throws CustomException {
        String sql = """
            SELECT o.id, o.nume_colectie as name, o.visibility, o.pret_achizitie as value
            FROM objects o
            WHERE o.pret_achizitie IS NOT NULL AND o.pret_achizitie > 0
            ORDER BY o.pret_achizitie DESC, o.id
            LIMIT 3
        """;
        return executeObjectClassamentQuery(sql);
    }

    private List<Clasament> executeCollectionClassamentQuery(String sql) throws CustomException {
        List<Clasament> result = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Clasament item = new Clasament();
                item.setId(rs.getLong("id"));
                item.setName(rs.getString("nume"));
                item.setVisible(rs.getBoolean("visibility"));

                double criteria = rs.getDouble(4);
                item.setCriteria(criteria);

                result.add(item);
            }

            return result;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la executarea interogării pentru clasamentul colecțiilor: " + e.getMessage(),
                    e
            );
        }
    }

    private List<Clasament> executeObjectClassamentQuery(String sql) throws CustomException {
        List<Clasament> result = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Clasament item = new Clasament();
                item.setId(rs.getLong("id"));
                item.setName(rs.getString("name"));
                item.setVisible(rs.getBoolean("visibility"));

                double criteria = rs.getDouble(4);
                item.setCriteria(criteria);

                result.add(item);
            }
            return result;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la executarea interogării pentru clasamentul obiectelor: " + e.getMessage(),
                    e
            );
        }
    }

    public Double getAllPercentObjectsCreatedLastMonth() throws CustomException {
        String sql = """
            SELECT
                COUNT(*) as total_objects,
                SUM(CASE WHEN created_at >= ? THEN 1 ELSE 0 END) as recent_objects
            FROM objects
        """;


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            stmt.setTimestamp(1, Timestamp.valueOf(oneMonthAgo));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long totalObjects = rs.getLong("total_objects");
                    long recentObjects = rs.getLong("recent_objects");

                    if (totalObjects == 0) {
                        Logger.debug("Nu există obiecte în sistem, procentul este 0%");
                        return 0.0;
                    }

                    return (double) recentObjects / totalObjects * 100.0;

                }
            }

            Logger.warning("Nu s-au putut obține date pentru calculul procentului de obiecte recente");
            return 0.0;
        } catch (SQLException e) {
            throw new Exception500.InternalServerErrorException(
                    "DatabaseError",
                    "SqlException",
                    "Eroare la calcularea procentului de obiecte recente: " + e.getMessage(),
                    e
            );
        }
    }
}