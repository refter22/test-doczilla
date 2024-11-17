package student.web.dto;

import org.junit.jupiter.api.Test;
import student.domain.Student;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class StudentDtoTest {
    @Test
    void shouldConvertFromDomain() {
        // given
        Student student = new Student(
                1L,
                "Иван",
                "Иванов",
                "Иванович",
                LocalDate.of(2000, 1, 1),
                "Группа 1");

        // when
        StudentDto dto = StudentDto.fromDomain(student);

        // then
        assertEquals(student.getId(), dto.id());
        assertEquals(student.getFirstName(), dto.firstName());
        assertEquals(student.getLastName(), dto.lastName());
        assertEquals(student.getMiddleName(), dto.middleName());
        assertEquals(student.getBirthDate().toString(), dto.birthDate());
        assertEquals(student.getGroup(), dto.group());
    }

    @Test
    void shouldConvertToDomain() {
        // given
        StudentDto dto = new StudentDto(
                1L,
                "Иван",
                "Иванов",
                "Иванович",
                "2000-01-01",
                "Группа 1");

        // when
        Student student = dto.toDomain();

        // then
        assertEquals(1L, student.getId());
        assertEquals("Иван", student.getFirstName());
        assertEquals(LocalDate.of(2000, 1, 1), student.getBirthDate());
    }
}