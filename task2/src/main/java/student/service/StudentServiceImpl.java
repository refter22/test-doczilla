package student.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import student.domain.Student;
import student.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentServiceImpl implements StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);
    private final StudentRepository repository;

    public StudentServiceImpl(StudentRepository repository) {
        this.repository = repository;
        logger.debug("StudentService создан");
    }

    @Override
    public Student createStudent(Student student) {
        logger.debug("Запрос на создание студента: {}", student);
        validateStudent(student);
        logger.debug("Валидация пройдена успешно");

        Student savedStudent = repository.save(student);
        logger.info("Создан новый студент: ID={}, имя={}, фамилия={}, группа={}",
                savedStudent.getId(),
                savedStudent.getFirstName(),
                savedStudent.getLastName(),
                savedStudent.getGroup());

        return savedStudent;
    }

    @Override
    public Optional<Student> findById(Long id) {
        logger.debug("Поиск студента по ID: {}", id);
        if (id == null) {
            logger.warn("Попытка найти студента с null ID");
            throw new IllegalArgumentException("Id не может быть null");
        }

        Optional<Student> student = repository.findById(id);
        if (student.isPresent()) {
            logger.debug("Найден студент: {}", student.get());
        } else {
            logger.debug("Студент с ID {} не найден", id);
        }

        return student;
    }

    @Override
    public List<Student> findAll() {
        logger.debug("Запрос на получение всех студентов");
        List<Student> students = repository.findAll();
        logger.debug("Найдено {} студентов", students.size());
        return students;
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Запрос на удаление студента с ID: {}", id);
        if (id == null) {
            logger.warn("Попытка удалить студента с null ID");
            throw new IllegalArgumentException("Id не может быть null");
        }

        repository.deleteById(id);
        logger.info("Удален студент с ID: {}", id);
    }

    private void validateStudent(Student student) {
        logger.debug("Валидация студента: {}", student);
        if (student == null) {
            logger.warn("Попытка создать null студента");
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
            String errorMessage = "Ошибки валидации: " + String.join(", ", errors);
            logger.warn("Ошибка валидации студента: {}", errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}