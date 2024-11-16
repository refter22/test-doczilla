package student.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import student.domain.Student;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

abstract class StudentRepositoryTest {

    private StudentRepository repository;

    @BeforeEach
    void setUp() {
        repository = createRepository();
    }

    abstract StudentRepository createRepository();

    @Test
    void shouldSaveAndGenerateId() {
        // given
        Student newStudent = new Student(
            null,
            "Иван",
            "Иванов",
            "Иванович",
            LocalDate.of(2000, 1, 1),
            "Группа 1"
        );

        // when
        Student saved = repository.save(newStudent);

        // then
        assertNotNull(saved.getId());
        assertEquals(newStudent.getFirstName(), saved.getFirstName());
        assertEquals(newStudent.getLastName(), saved.getLastName());
    }

    @Test
    void shouldFindSavedStudent() {
        // given
        Student saved = repository.save(new Student(
            null,
            "Иван",
            "Иванов",
            "Иванович",
            LocalDate.of(2000, 1, 1),
            "Группа 1"
        ));

        // when
        var found = repository.findById(saved.getId());

        // then
        assertTrue(found.isPresent());
        assertEquals(saved.getFirstName(), found.get().getFirstName());
    }

    @Test
    void shouldReturnEmptyWhenStudentNotFound() {
        // given
        // when
        var found = repository.findById(1L);

        // then
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAllStudents() {
        // given
        Student student1 = repository.save(new Student(
            null, "Иван", "Иванов", "Иванович",
            LocalDate.of(2000, 1, 1), "Группа 1"
        ));
        Student student2 = repository.save(new Student(
            null, "Петр", "Петров", "Петрович",
            LocalDate.of(2000, 2, 2), "Группа 1"
        ));

        // when
        List<Student> students = repository.findAll();

        // then
        assertEquals(2, students.size());
        assertTrue(students.stream()
            .map(Student::getId)
            .toList()
            .containsAll(List.of(student1.getId(), student2.getId()))
        );
    }

    @Test
    void shouldDeleteStudent() {
        // given
        Student saved = repository.save(new Student(
            null, "Иван", "Иванов", "Иванович",
            LocalDate.of(2000, 1, 1), "Группа 1"
        ));

        // when
        repository.deleteById(saved.getId());

        // then
        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}