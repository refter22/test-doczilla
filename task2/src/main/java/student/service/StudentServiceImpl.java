package student.service;

import student.domain.Student;
import student.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentServiceImpl implements StudentService {
    private final StudentRepository repository;

    public StudentServiceImpl(StudentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Student createStudent(Student student) {
        validateStudent(student);
        return repository.save(student);
    }

    @Override
    public Optional<Student> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id не может быть null");
        }
        return repository.findById(id);
    }

    @Override
    public List<Student> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id не может быть null");
        }
        repository.deleteById(id);
    }

    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Студент не может быть null");
        }

        List<String> errors = new ArrayList<>();

        if (isBlank(student.getFirstName())) {
            errors.add("Имя обязательно для заполнения");
        }
        if (isBlank(student.getLastName())) {
            errors.add("Фамилия обязательна для заполнения");
        }
        if (student.getBirthDate() == null) {
            errors.add("Дата рождения обязательна для заполнения");
        }
        if (isBlank(student.getGroup())) {
            errors.add("Группа обязательна для заполнения");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ошибки валидации: " + String.join(", ", errors));
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}