package student.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class StudentTest {
    @Test
    void validStudentShouldBeValid() {
        Student student = new Student(
            1L,
            "Иван",
            "Иванов",
            "Иванович",
            LocalDate.of(2000, 1, 1),
            "Группа 1"
        );

        assertTrue(student.isValid());
    }

    @Test
    void studentWithoutRequiredFieldsShouldBeInvalid() {
        Student studentWithoutFirstName = new Student(
            1L,
            "",  // пустое имя
            "Иванов",
            "Иванович",
            LocalDate.of(2000, 1, 1),
            "Группа 1"
        );

        assertFalse(studentWithoutFirstName.isValid());

        Student studentWithoutBirthDate = new Student(
            1L,
            "Иван",
            "Иванов",
            "Иванович",
            null,  // нет даты рождения
            "Группа 1"
        );

        assertFalse(studentWithoutBirthDate.isValid());
    }

    @Test
    void middleNameShouldBeOptional() {
        Student student = new Student(
            1L,
            "Иван",
            "Иванов",
            null,  // отчество может быть null
            LocalDate.of(2000, 1, 1),
            "Группа 1"
        );

        assertTrue(student.isValid());
    }
}