package student.web;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;
import student.service.StudentService;
import student.web.dto.StudentDto;
import student.domain.Student;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

class StudentControllerTest {
    private final StudentService service = mock(StudentService.class);
    private final StudentController controller = new StudentController(service);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Javalin app = Javalin.create()
        .post("/api/students", controller::createStudent)
        .get("/api/students/{id}", controller::getStudent)
        .get("/api/students", controller::getAllStudents)
        .delete("/api/students/{id}", controller::deleteStudent);

    @Test
    void shouldCreateStudent() {
        StudentDto newStudent = new StudentDto(
            null, "Иван", "Иванов", "Иванович",
            "2000-01-01", "Группа 1"
        );

        Student savedStudent = new Student(
            1L, "Иван", "Иванов", "Иванович",
            LocalDate.of(2000, 1, 1), "Группа 1"
        );

        when(service.createStudent(any())).thenReturn(savedStudent);

        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/api/students", newStudent);
            assertEquals(201, response.code());

            StudentDto created = objectMapper.readValue(
                response.body().string(),
                StudentDto.class
            );
            assertEquals(1L, created.id());
            assertEquals("Иван", created.firstName());
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
                LocalDate.of(2000, 2, 2), "Группа 2")
        );

        when(service.findAll()).thenReturn(students);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/api/students");
            assertEquals(200, response.code());

            List<StudentDto> result = objectMapper.readValue(
                response.body().string(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, StudentDto.class)
            );
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
}