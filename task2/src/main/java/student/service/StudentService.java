package student.service;

import student.domain.Student;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    Student createStudent(Student student);

    Optional<Student> findById(Long id);

    List<Student> findAll();

    void deleteById(Long id);
}