package student.web;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import student.service.StudentService;
import student.web.dto.StudentDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import student.domain.Student;

public class StudentController {
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    private final StudentService service;
    private final ObjectMapper objectMapper;

    public StudentController(StudentService service) {
        this.service = service;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        logger.debug("StudentController создан");
    }

    public void createStudent(Context ctx) {
        try {
            logger.info("Получен запрос на создание студента");
            String body = ctx.body();
            logger.debug("Тело запроса: {}", body);

            StudentDto dto;
            try {
                dto = objectMapper.readValue(body, StudentDto.class);
                logger.debug("Десериализован DTO: {}", dto);
            } catch (JsonProcessingException e) {
                logger.warn("Ошибка парсинга JSON: {}", e.getMessage());
                ctx.status(400).json(new ErrorResponse("Некорректный JSON"));
                return;
            }

            try {
                Student student = service.createStudent(dto.toDomain());
                logger.info("Создан новый студент с ID: {}", student.getId());
                ctx.status(201).json(StudentDto.fromDomain(student));
            } catch (IllegalArgumentException e) {
                logger.warn("Некорректные данные студента: {}", e.getMessage());
                ctx.status(400).json(new ErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при создании студента", e);
            ctx.status(500).json(new ErrorResponse("Внутренняя ошибка сервера"));
        }
    }

    public void getStudent(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        logger.info("Получен запрос на поиск студента с ID: {}", id);

        service.findById(id)
                .map(StudentDto::fromDomain)
                .ifPresentOrElse(
                        dto -> {
                            logger.debug("Найден студент: {}", dto);
                            ctx.json(dto);
                        },
                        () -> {
                            logger.warn("Студент с ID {} не найден", id);
                            ctx.status(HttpStatus.NOT_FOUND);
                            ctx.json(new ErrorResponse("Студент не найден"));
                        });
    }

    public void getAllStudents(Context ctx) {
        logger.info("Получен запрос на список всех студентов");
        var students = service.findAll().stream()
                .map(StudentDto::fromDomain)
                .toList();
        logger.debug("Найдено {} студентов", students.size());
        ctx.json(students);
    }

    public void deleteStudent(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        logger.info("Получен запрос на удаление студента с ID: {}", id);

        try {
            service.deleteById(id);
            logger.info("Студент с ID {} успешно удален", id);
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Ошибка при удалении студента с ID: {}", id, e);
            throw e;
        }
    }

    record ErrorResponse(String message) {
    }
}