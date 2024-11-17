package student.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import student.domain.Student;
import student.repository.StudentRepository;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;

class StudentServiceTest {
    private StudentService service;
    private StudentRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(StudentRepository.class);
        service = new StudentServiceImpl(repository);
    }

    @Test
    void shouldCreateValidStudent() {
        // given
        Student newStudent = new Student(
                null,
                "Иван",
                "Иванов",
                "Иванович",
                LocalDate.of(2000, 1, 1),
                "Группа 1");

        when(repository.save(any())).thenReturn(
                new Student(1L, "Иван", "Иванов", "Иванович",
                        LocalDate.of(2000, 1, 1), "Группа 1"));

        // when
        Student created = service.createStudent(newStudent);

        // then
        assertNotNull(created.getId());
        verify(repository).save(newStudent);
    }

    @Test
    void shouldThrowExceptionWhenCreatingInvalidStudent() {
        // given
        Student invalidStudent = new Student(
                null, "", "", null, null, "");

        // when + then
        assertThrows(IllegalArgumentException.class, () -> service.createStudent(invalidStudent));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFindById() {
        Student student = new Student(1L, "Иван", "Иванов", "Иванович",
                LocalDate.of(2000, 1, 1), "Группа 1");
        when(repository.findById(1L)).thenReturn(Optional.of(student));

        Optional<Student> found = service.findById(1L);
        assertTrue(found.isPresent());
        assertEquals(student.getId(), found.get().getId());
    }

    @Test
    void shouldFindAll() {
        List<Student> students = List.of(
                new Student(1L, "Иван", "Иванов", null,
                        LocalDate.of(2000, 1, 1), "Группа 1"));
        when(repository.findAll()).thenReturn(students);

        List<Student> found = service.findAll();
        assertEquals(1, found.size());
    }
}