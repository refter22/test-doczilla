package student.repository;

import student.domain.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends AutoCloseable {
    Student save(Student student);

    void deleteById(Long id);

    List<Student> findAll();

    Optional<Student> findById(Long id);

    @Override
    void close() throws Exception;
}