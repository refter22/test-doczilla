package student;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.bundled.CorsPluginConfig;
import student.exception.NotFoundException;
import student.repository.StudentRepository;
import student.repository.SqliteStudentRepository;
import student.service.StudentService;
import student.service.StudentServiceImpl;
import student.web.StudentController;

public class Application {
    public static void main(String[] args) {
        StudentRepository repository = new SqliteStudentRepository("students.db");
        StudentService service = new StudentServiceImpl(repository);
        StudentController controller = new StudentController(service);

        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(CorsPluginConfig::anyHost);
            });
        }).start(7000);

        app.post("/api/students", controller::createStudent);
        app.get("/api/students/{id}", controller::getStudent);
        app.get("/api/students", controller::getAllStudents);
        app.delete("/api/students/{id}", controller::deleteStudent);

        app.exception(NotFoundException.class, (e, ctx) -> {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(new ErrorResponse(e.getMessage()));
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResponse(e.getMessage()));
        });
    }

    record ErrorResponse(String message) {
    }
}