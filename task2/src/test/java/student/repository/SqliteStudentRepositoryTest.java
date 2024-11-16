package student.repository;

class SqliteStudentRepositoryTest extends StudentRepositoryTest {
    @Override
    StudentRepository createRepository() {
        SqliteStudentRepository repository = new SqliteStudentRepository(":memory:");
        closeAfterTest(repository);
        return repository;
    }

    private void closeAfterTest(AutoCloseable closeable) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}