
package edu.example.studentperformance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y recupera estudiantes y sus matriculas (HU-04) en MySQL mediante JDBC.
 */
public class StudentRepository {

    /** Curso que se asigna a matriculas que llegan sin curso (clientes anteriores a HU-02). */
    public static final String DEFAULT_ACADEMIC_YEAR = "2025/2026";

    private final Database database;

    public StudentRepository(Database database) {
        this.database = database;
    }

    public List<Student> findAll() {
        String sql = "SELECT id, name, study_hours FROM students ORDER BY id";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {

            List<Student> students = new ArrayList<>();

            while (rows.next()) {
                Student student = toStudent(rows);
                student.setEnrollments(findEnrollmentsByStudentId(connection, student.getId()));
                students.add(student);
            }

            return students;

        } catch (SQLException exception) {
            throw new DataAccessException("Could not list students", exception);
        }
    }

    public Student findById(int id) {
        String sql = "SELECT id, name, study_hours FROM students WHERE id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    Student student = toStudent(rows);
                    student.setEnrollments(findEnrollmentsByStudentId(connection, student.getId()));
                    return student;
                }

                return null;
            }

        } catch (SQLException exception) {
            throw new DataAccessException("Could not read student " + id, exception);
        }
    }

    public Student create(Student student) {
        String sql = "INSERT INTO students (name, study_hours) VALUES (?, ?)";

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                double totalHours = resolveStudyHours(student);

                statement.setString(1, student.getName());
                statement.setDouble(2, totalHours);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        student.setId(keys.getInt(1));
                    }
                }

                student.setStudyHours(totalHours);
                saveEnrollments(connection, student.getId(), student.getEnrollments());

                connection.commit();

                student.setEnrollments(findEnrollmentsByStudentId(connection, student.getId()));
                return student;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            throw new DataAccessException("Could not create student", exception);
        }
    }

    public Student update(int id, Student student) {
        String sql = "UPDATE students SET name = ?, study_hours = ? WHERE id = ?";

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                double totalHours = resolveStudyHours(student);

                statement.setString(1, student.getName());
                statement.setDouble(2, totalHours);
                statement.setInt(3, id);
                statement.executeUpdate();

                saveEnrollments(connection, id, student.getEnrollments());

                connection.commit();

                student.setId(id);
                student.setStudyHours(totalHours);
                student.setEnrollments(findEnrollmentsByStudentId(connection, id));

                return student;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

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

    public List<Enrollment> findByStudentId(int studentId) {
        try (Connection connection = database.getConnection()) {
            return findEnrollmentsByStudentId(connection, studentId);

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Could not list enrollments of student " + studentId, exception);
        }
    }

    public Enrollment createEnrollment(int studentId, Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, subject_id, academic_year, study_hours, grade) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String year = academicYearOf(enrollment);

            statement.setInt(1, studentId);
            statement.setInt(2, enrollment.getSubjectId());
            statement.setString(3, year);
            statement.setDouble(4, enrollment.getStudyHours());
            setNullableGrade(statement, 5, enrollment.getGrade());

            statement.executeUpdate();

            enrollment.setAcademicYear(year);
            return enrollment;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Could not create enrollment of student " + studentId, exception);
        }
    }

    public boolean updateGrade(int studentId, int subjectId, String academicYear, Double grade) {
        String sql = "UPDATE enrollments SET grade = ? " +
                     "WHERE student_id = ? AND subject_id = ? AND academic_year = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setNullableGrade(statement, 1, grade);
            statement.setInt(2, studentId);
            statement.setInt(3, subjectId);
            statement.setString(4, academicYear);

            return statement.executeUpdate() > 0;

        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Could not update grade of student " + studentId, exception);
        }
    }

    private void saveEnrollments(Connection connection, int studentId, List<Enrollment> enrollments)
            throws SQLException {

        if (enrollments == null || enrollments.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO enrollments (student_id, subject_id, academic_year, study_hours, grade) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE study_hours = VALUES(study_hours), " +
                     "grade = COALESCE(VALUES(grade), grade)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Enrollment enrollment : enrollments) {
                statement.setInt(1, studentId);
                statement.setInt(2, enrollment.getSubjectId());
                statement.setString(3, academicYearOf(enrollment));
                statement.setDouble(4, enrollment.getStudyHours());
                setNullableGrade(statement, 5, enrollment.getGrade());

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private List<Enrollment> findEnrollmentsByStudentId(Connection connection, int studentId)
            throws SQLException {

        String sql = "SELECT e.subject_id, s.name AS subject_name, s.difficulty, e.study_hours, " +
                     "e.academic_year, e.grade " +
                     "FROM enrollments e " +
                     "JOIN subjects s ON e.subject_id = s.id " +
                     "WHERE e.student_id = ? " +
                     "ORDER BY e.academic_year DESC, s.id";

        List<Enrollment> enrollments = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {

                    double gradeValue = rows.getDouble("grade");
                    Double grade = rows.wasNull() ? null : gradeValue;

                    enrollments.add(new Enrollment(
                            rows.getInt("subject_id"),
                            rows.getString("subject_name"),
                            fromDbDifficulty(rows.getString("difficulty")),
                            rows.getDouble("study_hours"),
                            rows.getString("academic_year"),
                            grade
                    ));
                }
            }
        }

        return enrollments;
    }

    private String academicYearOf(Enrollment enrollment) {
        String year = enrollment.getAcademicYear();

        return (year == null || year.isBlank())
                ? DEFAULT_ACADEMIC_YEAR
                : year;
    }

    private void setNullableGrade(PreparedStatement statement, int index, Double grade)
            throws SQLException {

        if (grade == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setDouble(index, grade);
        }
    }

    private double resolveStudyHours(Student student) {
        if (student.getEnrollments() != null && !student.getEnrollments().isEmpty()) {
            double sum = 0;

            for (Enrollment e : student.getEnrollments()) {
                sum += e.getStudyHours();
            }

            return sum;
        }

        return student.getStudyHours();
    }

    private String fromDbDifficulty(String dbDifficulty) {
        if ("facil".equalsIgnoreCase(dbDifficulty)) {
            return "EASY";
        }

        if ("media".equalsIgnoreCase(dbDifficulty)) {
            return "MEDIUM";
        }

        if ("dificil".equalsIgnoreCase(dbDifficulty)) {
            return "HARD";
        }

        return dbDifficulty;
    }

    private Student toStudent(ResultSet row) throws SQLException {
        return new Student(
                row.getInt("id"),
                row.getString("name"),
                row.getDouble("study_hours")
        );
    }
}
