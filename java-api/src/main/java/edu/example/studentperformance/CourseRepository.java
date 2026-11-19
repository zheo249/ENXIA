package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y recupera cursos academicos en MySQL mediante JDBC.
 *
 * Tabla esperada: courses(id, academic_year, start_date, end_date)
 * y enrollments.course_id como clave foranea hacia courses.id.
 */
public class CourseRepository {

    private final Database database;

    public CourseRepository(Database database) {
        this.database = database;
    }

    public List<Course> findAll() {
        String sql = "SELECT id, academic_year, start_date, end_date FROM courses ORDER BY start_date DESC, id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            List<Course> courses = new ArrayList<>();
            while (rows.next()) {
                courses.add(toCourse(rows));
            }
            return courses;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list courses", exception);
        }
    }

    public Course findById(int id) {
        String sql = "SELECT id, academic_year, start_date, end_date FROM courses WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? toCourse(rows) : null;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not read course " + id, exception);
        }
    }

    public Course create(Course course) {
        String sql = "INSERT INTO courses (academic_year, start_date, end_date) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, course.getAcademicYear());
            statement.setString(2, course.getStartDate());
            statement.setString(3, course.getEndDate());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    course.setId(keys.getInt(1));
                }
            }
            return course;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not create course", exception);
        }
    }

    public boolean update(int id, Course course) {
        String sql = "UPDATE courses SET academic_year = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.getAcademicYear());
            statement.setString(2, course.getStartDate());
            statement.setString(3, course.getEndDate());
            statement.setInt(4, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update course " + id, exception);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not delete course " + id, exception);
        }
    }

    /** Indica si ya hay otro curso con ese nombre (excluyendo excludeId al editar). */
    public boolean existsByAcademicYear(String academicYear, int excludeId) {
        String sql = "SELECT COUNT(*) FROM courses WHERE academic_year = ? AND id <> ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, academicYear);
            statement.setInt(2, excludeId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not check course name", exception);
        }
    }

    /** Indica si el curso tiene alguna matricula asociada. */
    public boolean hasEnrollments(int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, courseId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() && rows.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not check enrollments of course " + courseId, exception);
        }
    }

    private Course toCourse(ResultSet row) throws SQLException {
        return new Course(
                row.getInt("id"),
                row.getString("academic_year"),
                row.getString("start_date"),
                row.getString("end_date")
        );
    }
}
