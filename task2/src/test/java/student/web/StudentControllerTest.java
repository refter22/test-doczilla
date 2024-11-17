package student.web;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;
import student.service.StudentService;
import student.web.StudentController.ErrorResponse;
import student.web.dto.StudentDto;
import student.domain.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.json.JavalinJackson;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

class StudentControllerTest {
    private final StudentService service = mock(StudentService.class);
    private final StudentController controller = new StudentController(service);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final Javalin app = Javalin.create(config -> {
        config.jsonMapper(new JavalinJackson(objectMapper));
    })
            .post("/api/students", controller::createStudent)
            .get("/api/students/{id}", controller::getStudent)
            .get("/api/students", controller::getAllStudents)
            .delete("/api/students/{id}", controller::deleteStudent);

    @Test
    void shouldCreateStudent() {
        StudentDto newStudent = new StudentDto(
                null, "Иван", "Иванов", "Иванович",
                "2000-01-01", "Группа 1");

        Student savedStudent = new Student(
                1L, "Иван", "Иванов", "Иванович",
                LocalDate.of(2000, 1, 1), "Группа 1");

        when(service.createStudent(any())).thenReturn(savedStudent);

        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post("/api/students", newStudent)) {
                assertEquals(201, response.code());
                StudentDto created = objectMapper.readValue(
                        response.body().string(),
                        StudentDto.class);
                assertEquals(1L, created.id());
            }
        });
    }

    @Test
    void shouldReturn404WhenStudentNotFound() {
        when(service.findById(1L)).thenReturn(Optional.empty());

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/api/students/1");
            assertEquals(404, response.code());
        });
    }

    @Test
    void shouldReturnAllStudents() {
        List<Student> students = List.of(
                new Student(1L, "Иван", "Иванов", "Иванович",
                        LocalDate.of(2000, 1, 1), "Группа 1"),
                new Student(2L, "Петр", "Петров", "Петрович",
                        LocalDate.of(2000, 2, 2), "Группа 2"));

        when(service.findAll()).thenReturn(students);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/api/students");
            assertEquals(200, response.code());

            List<StudentDto> result = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, StudentDto.class));
            assertEquals(2, result.size());
            assertEquals("Иван", result.get(0).firstName());
            assertEquals("Петр", result.get(1).firstName());
        });
    }

    @Test
    void shouldDeleteStudent() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.delete("/api/students/1");
            assertEquals(204, response.code());
            verify(service).deleteById(1L);
        });
    }

    @Test
    void shouldReturn400WhenJsonIsInvalid() {
        String invalidJson = "{некорректный json}";

        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/students", invalidJson);
            assertEquals(400, response.code());
        });
    }

    @Test
    void shouldReturn400WhenRequiredFieldsAreMissing() {
        String incompleteJson = """
                {
                    "firstName": "Иван"
                }
                """;

        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/students", incompleteJson);
            assertEquals(400, response.code());

            ErrorResponse error = objectMapper.readValue(
                    response.body().string(),
                    ErrorResponse.class);
            assertNotNull(error.message());
        });
    }

    @Test
    void shouldReturn400WhenDateFormatIsInvalid() {
        String jsonWithInvalidDate = """
                {
                    "firstName": "Иван",
                    "lastName": "Иванов",
                    "middleName": "Иванович",
                    "birthDate": "некорректная дата",
                    "group": "Группа 1"
                }
                """;

        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/students", jsonWithInvalidDate);
            assertEquals(400, response.code());
        });
    }

    @Test
    void shouldReturn500WhenInternalError() {
        when(service.createStudent(any())).thenThrow(new RuntimeException("Внутренняя ошибка"));

        StudentDto newStudent = new StudentDto(
                null, "Иван", "Иванов", "Иванович",
                "2000-01-01", "Группа 1");

        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/students", newStudent);
            assertEquals(500, response.code());

            ErrorResponse error = objectMapper.readValue(
                    response.body().string(),
                    ErrorResponse.class);
            assertEquals("Внутренняя ошибка сервера", error.message());
        });
    }
}