package student.repository;

import student.domain.Student;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteStudentRepository implements StudentRepository, AutoCloseable {

    private final Connection connection;

    public SqliteStudentRepository(String dbPath) {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTableIfNeeded();
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось подключиться к БД", e);
        }
    }

    private void createTableIfNeeded() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS students (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            first_name TEXT NOT NULL,
                            last_name TEXT NOT NULL,
                            middle_name TEXT,
                            birth_date TEXT NOT NULL,
                            group_name TEXT NOT NULL
                        )
                    """);
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось создать таблицу", e);
        }
    }

    @Override
    public Student save(Student student) {
        try {
            if (student.getId() == null) {
                PreparedStatement insertStmt = connection.prepareStatement("""
                            INSERT INTO students (first_name, last_name, middle_name, birth_date, group_name)
                            VALUES (?, ?, ?, ?, ?)
                        """);

                insertStmt.setString(1, student.getFirstName());
                insertStmt.setString(2, student.getLastName());
                insertStmt.setString(3, student.getMiddleName());
                insertStmt.setString(4, student.getBirthDate().toString());
                insertStmt.setString(5, student.getGroup());

                insertStmt.executeUpdate();

                ResultSet rs = connection.createStatement().executeQuery(
                        "SELECT last_insert_rowid()");

                if (rs.next()) {
                    return new Student(
                            rs.getLong(1),
                            student.getFirstName(),
                            student.getLastName(),
                            student.getMiddleName(),
                            student.getBirthDate(),
                            student.getGroup());
                }
            }
            return student;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить студента", e);
        }
    }

    @Override
    public Optional<Student> findById(Long id) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM students WHERE id = ?");
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Student(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("middle_name"),
                        LocalDate.parse(rs.getString("birth_date")),
                        rs.getString("group_name")));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось найти студента", e);
        }
    }

    @Override
    public List<Student> findAll() {
        try {
            List<Student> students = new ArrayList<>();
            ResultSet rs = connection.createStatement().executeQuery(
                    "SELECT * FROM students");

            while (rs.next()) {
                students.add(new Student(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("middle_name"),
                        LocalDate.parse(rs.getString("birth_date")),
                        rs.getString("group_name")));
            }
            return students;

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить список студентов", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM students WHERE id = ?");
            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить студента", e);
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}