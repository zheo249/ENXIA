package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y recupera estudiantes en MySQL mediante JDBC.
 * Es la unica clase que conoce SQL: el resto de capas solo ven objetos Student.
 */
public class StudentRepository {
    private final Database database;

    public StudentRepository(Database database) {
        this.database = database;
    }

    public List<Student> findAll() {
        String sql = "SELECT id, name, study_hours FROM students ORDER BY id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<Student> students = new ArrayList<Student>();
            while (rows.next()) {
                students.add(toStudent(rows));
            }
            return students;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list students", exception);
        }
    }

    /**
     * Devuelve el estudiante o null si no existe.
     */
    public Student findById(int id) {
        String sql = "SELECT id, name, study_hours FROM students WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return toStudent(rows);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not read student " + id, exception);
        }
    }

    public Student create(Student student) {
        // Ahora el identificador lo genera MySQL (AUTO_INCREMENT) y lo recuperamos.
        String sql = "INSERT INTO students (name, study_hours) VALUES (?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, student.getName());
            statement.setDouble(2, student.getStudyHours());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setId(keys.getInt(1));
                }
            }
            return student;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not create student", exception);
        }
    }

    public Student update(int id, Student student) {
        // El identificador de la URL prevalece sobre cualquier valor recibido en el JSON.
        String sql = "UPDATE students SET name = ?, study_hours = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.getName());
            statement.setDouble(2, student.getStudyHours());
            statement.setInt(3, id);
            statement.executeUpdate();
            student.setId(id);
            return student;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update student " + id, exception);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not delete student " + id, exception);
        }
    }

    private Student toStudent(ResultSet row) throws SQLException {
        return new Student(
                row.getInt("id"),
                row.getString("name"),
                row.getDouble("study_hours"));
    }
}
