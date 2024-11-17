package student;

import io.javalin.Javalin;
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
    public static void main(String[] args) {
        String dbPath = "students.db";
        StudentRepository repository = null;
        Javalin app = null;

        try {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                DatabaseMigration migration = new DatabaseMigration(connection);
                migration.migrate();
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка при инициализации БД", e);
            }

            repository = new SqliteStudentRepository(dbPath);
            StudentService service = new StudentServiceImpl(repository);
            StudentController controller = new StudentController(service);

            app = Javalin.create(config -> {
                config.plugins.enableCors(cors -> {
                    cors.add(CorsPluginConfig::anyHost);
                });
                config.http.maxRequestSize = 10 * 1024 * 1024;
                config.http.asyncTimeout = 10_000L;
            }).start(7000);

            app.post("/api/students", controller::createStudent);
            app.get("/api/students/{id}", controller::getStudent);
            app.get("/api/students", controller::getAllStudents);
            app.delete("/api/students/{id}", controller::deleteStudent);

            configureErrorHandlers(app);

            final Javalin finalApp = app;
            final StudentRepository finalRepository = repository;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы приложения...");
                finalApp.stop();
                try {
                    finalRepository.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Приложение остановлено");
            }));

        } catch (Exception e) {
            System.err.println("Критическая ошибка при запуске приложения:");
            e.printStackTrace();
            if (app != null) {
                app.stop();
            }
            if (repository != null) {
                try {
                    repository.close();
                } catch (Exception closeError) {
                    closeError.printStackTrace();
                }
            }
            System.exit(1);
        }
    }

    private static void configureErrorHandlers(Javalin app) {
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