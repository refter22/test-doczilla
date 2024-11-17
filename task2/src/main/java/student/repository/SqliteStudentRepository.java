package student.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import student.domain.Student;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteStudentRepository implements StudentRepository {
    private static final Logger logger = LoggerFactory.getLogger(SqliteStudentRepository.class);
    private final Connection connection;

    public SqliteStudentRepository(String dbPath) {
        logger.debug("Инициализация SQLite репозитория. Путь к БД: {}", dbPath);
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            this.connection.setAutoCommit(true);
            logger.info("Подключение к БД успешно установлено");
        } catch (ClassNotFoundException e) {
            logger.error("SQLite драйвер не найден", e);
            throw new RuntimeException("SQLite драйвер не найден", e);
        } catch (SQLException e) {
            logger.error("Не удалось подключиться к БД: {}", dbPath, e);
            throw new RuntimeException("Не удалось подключиться к БД: " + dbPath, e);
        }
    }

    @Override
    public Student save(Student student) {
        logger.debug("Попытка сохранения студента: {}", student);
        if (student.getId() == null) {
            try (PreparedStatement insertStmt = connection.prepareStatement("""
                    INSERT INTO students (first_name, last_name, middle_name, birth_date, group_name)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {

                connection.setAutoCommit(false);
                logger.trace("Начата транзакция");

                try {
                    insertStmt.setString(1, student.getFirstName());
                    insertStmt.setString(2, student.getLastName());
                    insertStmt.setString(3, student.getMiddleName());
                    insertStmt.setString(4, student.getBirthDate().toString());
                    insertStmt.setString(5, student.getGroup());

                    insertStmt.executeUpdate();
                    logger.debug("SQL запрос выполнен успешно");

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
                            logger.info("Студент успешно сохранен с ID: {}", savedStudent.getId());
                            return savedStudent;
                        }
                    }

                    connection.rollback();
                    logger.error("Не удалось получить ID сохраненного студента");
                    throw new RuntimeException("Не удалось получить ID сохраненного студента");

                } catch (Exception e) {
                    connection.rollback();
                    logger.error("Ошибка при сохранении студента", e);
                    if (e instanceof SQLException && e.getMessage().contains("UNIQUE")) {
                        throw new IllegalArgumentException("Студент с такими данными уже существует", e);
                    }
                    throw new RuntimeException("Не удалось сохранить студента", e);
                } finally {
                    connection.setAutoCommit(true);
                    logger.trace("Транзакция завершена");
                }
            } catch (SQLException e) {
                logger.error("Ошибка при работе с БД", e);
                throw new RuntimeException("Ошибка при работе с БД", e);
            }
        }
        return student;
    }

    @Override
    public Optional<Student> findById(Long id) {
        logger.debug("Поиск студента по ID: {}", id);
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM students WHERE id = ?")) {

            stmt.setLong(1, id);
            logger.trace("Выполнение SQL запроса: {}", stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student(
                            rs.getLong("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("middle_name"),
                            LocalDate.parse(rs.getString("birth_date")),
                            rs.getString("group_name"));
                    logger.debug("Найден студент: {}", student);
                    return Optional.of(student);
                }
                logger.debug("Студент с ID {} не найден", id);
                return Optional.empty();
            }

        } catch (SQLException e) {
            logger.error("Ошибка при поиске студента с ID: {}", id, e);
            throw new RuntimeException("Не удалось найти студента", e);
        }
    }

    @Override
    public List<Student> findAll() {
        logger.debug("Запрос всех студентов");
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
            logger.debug("Найдено {} студентов", students.size());
            return students;

        } catch (SQLException e) {
            logger.error("Ошибка при получении списка студентов", e);
            throw new RuntimeException("Не удалось получить список студентов", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Попытка удаления студента с ID: {}", id);
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM students WHERE id = ?")) {

            connection.setAutoCommit(false);
            logger.trace("Начата транзакция удаления");

            try {
                stmt.setLong(1, id);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    connection.rollback();
                    logger.warn("Попытка удаления несуществующего студента с ID: {}", id);
                    throw new IllegalArgumentException("Студент с ID " + id + " не найден");
                }

                connection.commit();
                logger.info("Студент с ID {} успешно удален", id);
            } catch (Exception e) {
                connection.rollback();
                logger.error("Ошибка при удалении студента с ID: {}", id, e);
                throw new RuntimeException("Не удалось удалить студента", e);
            } finally {
                connection.setAutoCommit(true);
                logger.trace("Транзакция удаления завершена");
            }

        } catch (SQLException e) {
            logger.error("Ошибка при работе с БД во время удаления", e);
            throw new RuntimeException("Ошибка при работе с БД", e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Соединение с БД закрыто");
            }
        } catch (SQLException e) {
            logger.error("Ошибка при закрытии соединения с БД", e);
            e.printStackTrace();
        }
    }
}