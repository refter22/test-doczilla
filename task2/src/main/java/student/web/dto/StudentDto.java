package student.web.dto;

import student.domain.Student;
import java.time.LocalDate;

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
        return new Student(
                id,
                firstName,
                lastName,
                middleName,
                LocalDate.parse(birthDate),
                group);
    }
}