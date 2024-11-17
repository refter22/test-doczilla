package student.repository;

import student.domain.Student;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteStudentRepository implements StudentRepository {

    private final Connection connection;

    public SqliteStudentRepository(String dbPath) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            this.connection.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite драйвер не найден", e);
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось подключиться к БД: " + dbPath, e);
        }
    }

    @Override
    public Student save(Student student) {
        if (student.getId() == null) {
            try (PreparedStatement insertStmt = connection.prepareStatement("""
                    INSERT INTO students (first_name, last_name, middle_name, birth_date, group_name)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {

                connection.setAutoCommit(false);

                try {
                    insertStmt.setString(1, student.getFirstName());
                    insertStmt.setString(2, student.getLastName());
                    insertStmt.setString(3, student.getMiddleName());
                    insertStmt.setString(4, student.getBirthDate().toString());
                    insertStmt.setString(5, student.getGroup());

                    insertStmt.executeUpdate();

                    try (ResultSet rs = connection.createStatement()
                            .executeQuery("SELECT last_insert_rowid()")) {
                        if (rs.next()) {
                            Student savedStudent = new Student(
                                    rs.getLong(1),
                                    student.getFirstName(),
                                    student.getLastName(),
                                    student.getMiddleName(),
                                    student.getBirthDate(),
                                    student.getGroup());

                            connection.commit();
                            return savedStudent;
                        }
                    }

                    connection.rollback();
                    throw new RuntimeException("Не удалось получить ID сохраненного студента");

                } catch (Exception e) {
                    connection.rollback();
                    if (e instanceof SQLException && e.getMessage().contains("UNIQUE")) {
                        throw new IllegalArgumentException("Студент с такими данными уже существует", e);
                    }
                    throw new RuntimeException("Не удалось сохранить студента", e);
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при работе с БД", e);
            }
        }
        return student;
    }

    @Override
    public Optional<Student> findById(Long id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM students WHERE id = ?")) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
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
            }

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось найти студента", e);
        }
    }

    @Override
    public List<Student> findAll() {
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {

            List<Student> students = new ArrayList<>();
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
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM students WHERE id = ?")) {

            connection.setAutoCommit(false);
            try {
                stmt.setLong(1, id);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    connection.rollback();
                    throw new IllegalArgumentException("Студент с ID " + id + " не найден");
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Не удалось удалить студента", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при работе с БД", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}