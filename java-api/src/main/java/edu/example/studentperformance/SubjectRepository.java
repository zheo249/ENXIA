package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y recupera asignaturas en MySQL mediante JDBC.
 */
public class SubjectRepository {

    private final Database database;

    public SubjectRepository(Database database) {
        this.database = database;
    }

    public List<Subject> findAll() {
        String sql = "SELECT id, name, difficulty FROM subjects ORDER BY id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {

            List<Subject> subjects = new ArrayList<>();
            while (rows.next()) {
                subjects.add(toSubject(rows));
            }
            return subjects;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list subjects", exception);
        }
    }

    public Subject findById(int id) {
        String sql = "SELECT id, name, difficulty FROM subjects WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return toSubject(rows);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not read subject " + id, exception);
        }
    }

    public Subject create(Subject subject) {
        String sql = "INSERT INTO subjects (name, difficulty) VALUES (?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, subject.getName());
            statement.setString(2, toDbDifficulty(subject.getDifficulty()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    subject.setId(keys.getInt(1));
                }
            }
            return subject;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not create subject", exception);
        }
    }

    private Subject toSubject(ResultSet row) throws SQLException {
        return new Subject(
                row.getInt("id"),
                row.getString("name"),
                fromDbDifficulty(row.getString("difficulty"))
        );
    }

    private String toDbDifficulty(String difficulty) {
        if ("EASY".equalsIgnoreCase(difficulty) || "facil".equalsIgnoreCase(difficulty)) return "facil";
        if ("MEDIUM".equalsIgnoreCase(difficulty) || "media".equalsIgnoreCase(difficulty)) return "media";
        if ("HARD".equalsIgnoreCase(difficulty) || "dificil".equalsIgnoreCase(difficulty)) return "dificil";
        return difficulty;
    }

    private String fromDbDifficulty(String dbDifficulty) {
        if ("facil".equalsIgnoreCase(dbDifficulty)) return "EASY";
        if ("media".equalsIgnoreCase(dbDifficulty)) return "MEDIUM";
        if ("dificil".equalsIgnoreCase(dbDifficulty)) return "HARD";
        return dbDifficulty;
    }
}
