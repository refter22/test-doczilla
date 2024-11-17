package student.web.dto;

import student.domain.Student;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public record StudentDto(
        Long id,
        String firstName,
        String lastName,
        String middleName,
        String birthDate,
        String group) {
    public static StudentDto fromDomain(Student student) {
        return new StudentDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getMiddleName(),
                student.getBirthDate().toString(),
                student.getGroup());
    }

    public Student toDomain() {
        if (firstName == null || firstName.isBlank() ||
                lastName == null || lastName.isBlank() ||
                birthDate == null || birthDate.isBlank() ||
                group == null || group.isBlank()) {
            throw new IllegalArgumentException("Отсутствуют обязательные поля");
        }

        try {
            return new Student(
                    id,
                    firstName,
                    lastName,
                    middleName,
                    LocalDate.parse(birthDate),
                    group);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Некорректный формат даты");
        }
    }
}