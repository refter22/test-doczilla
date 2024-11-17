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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
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
            logger.info("Запуск приложения...");
            Application application = new Application();
            application.start();
            logger.info("Приложение успешно запущено на порту {}", PORT);
        } catch (Exception e) {
            logger.error("Критическая ошибка при запуске приложения", e);
            System.exit(1);
        }
    }

    private void start() {
        registerShutdownHook();
    }

    private StudentRepository initializeDatabase() {
        logger.info("Инициализация базы данных: {}", DB_PATH);
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            DatabaseMigration migration = new DatabaseMigration(connection);
            migration.migrate();
            logger.info("Миграция базы данных успешно выполнена");
            return new SqliteStudentRepository(DB_PATH);
        } catch (SQLException e) {
            logger.error("Ошибка при инициализации БД", e);
            throw new RuntimeException("Ошибка при инициализации БД", e);
        }
    }

    private Javalin initializeJavalin(StudentController controller) {
        logger.info("Инициализация веб-сервера");
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
        logger.debug("Настройка CORS");
        config.plugins.enableCors(cors -> cors.add(CorsPluginConfig::anyHost));
    }

    private void configureStaticFiles(JavalinConfig config) {
        logger.debug("Настройка раздачи статических файлов");
        config.staticFiles.add("/static");
    }

    private void configureHttp(JavalinConfig config) {
        logger.debug("Настройка HTTP параметров");
        config.http.maxRequestSize = 10 * 1024 * 1024;
        config.http.asyncTimeout = 10_000L;
    }

    private void registerRoutes(Javalin app, StudentController controller) {
        logger.debug("Регистрация маршрутов API");
        app.post("/api/students", controller::createStudent);
        app.get("/api/students/{id}", controller::getStudent);
        app.get("/api/students", controller::getAllStudents);
        app.delete("/api/students/{id}", controller::deleteStudent);
    }

    private void registerShutdownHook() {
        logger.debug("Регистрация хука завершения работы");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Завершение работы приложения...");
            shutdown();
            logger.info("Приложение остановлено");
        }));
    }

    private void shutdown() {
        try {
            if (app != null) {
                logger.debug("Остановка веб-сервера");
                app.stop();
            }
            if (repository != null) {
                logger.debug("Закрытие соединения с БД");
                repository.close();
            }
        } catch (Exception e) {
            logger.error("Ошибка при завершении работы", e);
        }
    }

    private void configureErrorHandlers(Javalin app) {
        logger.debug("Настройка обработчиков ошибок");

        app.exception(NotFoundException.class, (e, ctx) -> {
            logger.warn("Ресурс не найден: {}", e.getMessage());
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new ErrorResponse(e.getMessage()));
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            logger.warn("Некорректный запрос: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse(e.getMessage()));
        });

        app.exception(JsonParseException.class, (e, ctx) -> {
            logger.warn("Ошибка парсинга JSON: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse("Некорректный JSON"));
        });

        app.exception(JsonMappingException.class, (e, ctx) -> {
            logger.warn("Ошибка маппинга JSON: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse("Ошибка маппинга JSON: " + e.getMessage()));
        });

        app.exception(SQLException.class, (e, ctx) -> {
            logger.error("Ошибка базы данных", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(new ErrorResponse("Ошибка базы данных"));
        });

        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Внутренняя ошибка сервера", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(new ErrorResponse("Внутренняя ошибка сервера"));
        });
    }

    record ErrorResponse(String message) {
    }
}