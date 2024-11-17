package student.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import student.repository.migration.DatabaseMigration;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class SqliteStudentRepositoryTest extends StudentRepositoryTest {
    private static final String TEST_DB = "test.db";
    private Connection connection;
    private File dbFile;
    private SqliteStudentRepository repository;

    @Override
    @BeforeEach
    void setUp() throws SQLException {
        dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + TEST_DB);
        DatabaseMigration migration = new DatabaseMigration(connection);
        migration.migrate();

        repository = new SqliteStudentRepository(TEST_DB);
        super.setUp();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (repository != null) {
            repository.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Override
    StudentRepository createRepository() {
        return repository;
    }
}