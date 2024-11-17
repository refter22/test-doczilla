package student.repository.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseMigration {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);
    private final Connection connection;
    private static final List<String> MIGRATIONS = List.of(
            """
                    CREATE TABLE IF NOT EXISTS migrations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        version INTEGER NOT NULL,
                        applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """,
            """
                    CREATE TABLE IF NOT EXISTS students (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        first_name TEXT NOT NULL,
                        last_name TEXT NOT NULL,
                        middle_name TEXT,
                        birth_date TEXT NOT NULL,
                        group_name TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """,
            """
                    CREATE INDEX IF NOT EXISTS idx_students_last_name
                    ON students(last_name)
                    """,
            """
                    CREATE INDEX IF NOT EXISTS idx_students_group
                    ON students(group_name)
                    """);

    public DatabaseMigration(Connection connection) {
        this.connection = connection;
        logger.debug("Создан объект миграции БД");
    }

    public void migrate() {
        logger.info("Начало процесса миграции БД");
        try {
            logger.debug("Создание таблицы миграций");
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(MIGRATIONS.get(0));
                logger.trace("Таблица миграций создана или уже существует");
            }

            int currentVersion = getCurrentVersion();
            logger.info("Текущая версия БД: {}", currentVersion);

            if (currentVersion + 1 >= MIGRATIONS.size()) {
                logger.info("БД уже в актуальном состоянии");
                return;
            }

            logger.info("Необходимо применить {} миграций", MIGRATIONS.size() - (currentVersion + 1));
            for (int i = currentVersion + 1; i < MIGRATIONS.size(); i++) {
                try (Statement stmt = connection.createStatement()) {
                    connection.setAutoCommit(false);
                    logger.debug("Начало применения миграции #{}", i);
                    logger.trace("SQL миграции #{}: {}", i, MIGRATIONS.get(i));

                    stmt.execute(MIGRATIONS.get(i));
                    logger.debug("SQL миграции #{} выполнен успешно", i);

                    stmt.execute("INSERT INTO migrations (version) VALUES (" + i + ")");
                    logger.debug("Версия миграции #{} сохранена", i);

                    connection.commit();
                    logger.info("Миграция #{} успешно применена", i);
                } catch (SQLException e) {
                    connection.rollback();
                    logger.error("Ошибка при выполнении миграции #{}", i, e);
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                    logger.trace("Транзакция миграции #{} завершена", i);
                }
            }
            logger.info("Процесс миграции БД успешно завершен");
        } catch (SQLException e) {
            logger.error("Критическая ошибка при выполнении миграций", e);
            throw new RuntimeException("Ошибка при выполнении миграций", e);
        }
    }

    private int getCurrentVersion() throws SQLException {
        logger.debug("Получение текущей версии БД");
        try (var stmt = connection.createStatement()) {
            var rs = stmt.executeQuery(
                    "SELECT MAX(version) as version FROM migrations");
            int version = rs.next() ? rs.getInt("version") : 0;
            logger.debug("Получена текущая версия БД: {}", version);
            return version;
        } catch (SQLException e) {
            logger.error("Ошибка при получении версии БД", e);
            throw e;
        }
    }
}