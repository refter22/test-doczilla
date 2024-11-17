package student;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.bundled.CorsPluginConfig;
import student.exception.NotFoundException;
import student.repository.StudentRepository;
import student.repository.SqliteStudentRepository;
import student.repository.migration.DatabaseMigration;
import student.service.StudentService;
import student.service.StudentServiceImpl;
import student.web.StudentController;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {
    private static final int PORT = 7000;
    private static final String DB_PATH = "students.db";
    private final StudentRepository repository;
    private final Javalin app;

    private Application() {
        this.repository = initializeDatabase();
        StudentService service = new StudentServiceImpl(repository);
        StudentController controller = new StudentController(service);
        this.app = initializeJavalin(controller);
    }

    public static void main(String[] args) {
        try {
            Application application = new Application();
            application.start();
        } catch (Exception e) {
            System.err.println("Критическая ошибка при запуске приложения:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void start() {
        registerShutdownHook();
    }

    private StudentRepository initializeDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            DatabaseMigration migration = new DatabaseMigration(connection);
            migration.migrate();
            return new SqliteStudentRepository(DB_PATH);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при инициализации БД", e);
        }
    }

    private Javalin initializeJavalin(StudentController controller) {
        Javalin app = Javalin.create(config -> {
            configureCors(config);
            configureStaticFiles(config);
            configureHttp(config);
        }).start(PORT);

        registerRoutes(app, controller);
        configureErrorHandlers(app);

        return app;
    }

    private void configureCors(JavalinConfig config) {
        config.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
    }

    private void configureStaticFiles(JavalinConfig config) {
        config.staticFiles.add("/static");
    }

    private void configureHttp(JavalinConfig config) {
        config.http.maxRequestSize = 10 * 1024 * 1024;
        config.http.asyncTimeout = 10_000L;
    }

    private void registerRoutes(Javalin app, StudentController controller) {
        app.post("/api/students", controller::createStudent);
        app.get("/api/students/{id}", controller::getStudent);
        app.get("/api/students", controller::getAllStudents);
        app.delete("/api/students/{id}", controller::deleteStudent);
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Завершение работы приложения...");
            shutdown();
            System.out.println("Приложение остановлено");
        }));
    }

    private void shutdown() {
        try {
            if (app != null) {
                app.stop();
            }
            if (repository != null) {
                repository.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureErrorHandlers(Javalin app) {
        app.exception(NotFoundException.class, (e, ctx) -> {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new ErrorResponse(e.getMessage()));
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse(e.getMessage()));
        });

        app.exception(JsonParseException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse("Некорректный JSON"));
        });

        app.exception(JsonMappingException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse("Ошибка маппинга JSON: " + e.getMessage()));
        });

        app.exception(SQLException.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(new ErrorResponse("Ошибка базы данных"));
            e.printStackTrace();
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(new ErrorResponse("Внутренняя ошибка сервера"));
            e.printStackTrace();
        });
    }

    record ErrorResponse(String message) {
    }
}