package student.repository.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseMigration {
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
    }

    public void migrate() {
        try {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(MIGRATIONS.get(0));
            }

            int currentVersion = getCurrentVersion();

            for (int i = currentVersion + 1; i < MIGRATIONS.size(); i++) {
                try (Statement stmt = connection.createStatement()) {
                    connection.setAutoCommit(false);

                    stmt.execute(MIGRATIONS.get(i));

                    stmt.execute("INSERT INTO migrations (version) VALUES (" + i + ")");

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при выполнении миграций", e);
        }
    }

    private int getCurrentVersion() throws SQLException {
        try (var stmt = connection.createStatement()) {
            var rs = stmt.executeQuery(
                    "SELECT MAX(version) as version FROM migrations");
            return rs.next() ? rs.getInt("version") : 0;
        }
    }
}