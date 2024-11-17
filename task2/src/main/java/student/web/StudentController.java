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
        this.objectMapper = createObjectMapper();
        logger.debug("StudentController создан");
    }

    public void createStudent(Context ctx) {
        logger.info("Получен запрос на создание студента");
        try {
            StudentDto dto = parseRequestBody(ctx);
            Student createdStudent = createAndValidateStudent(dto);
            sendSuccessResponse(ctx, createdStudent);
        } catch (ValidationException e) {
            handleValidationError(ctx, e);
        } catch (Exception e) {
            handleInternalError(ctx, "Ошибка при создании студента", e);
        }
    }

    public void getStudent(Context ctx) {
        Long id = getIdFromPath(ctx);
        logger.info("Получен запрос на поиск студента с ID: {}", id);

        service.findById(id)
                .map(StudentDto::fromDomain)
                .ifPresentOrElse(
                        dto -> ctx.json(dto),
                        () -> handleNotFound(ctx, id));
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
        Long id = getIdFromPath(ctx);
        logger.info("Получен запрос на удаление студента с ID: {}", id);

        try {
            service.deleteById(id);
            logger.info("Студент с ID {} успешно удален", id);
            ctx.status(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            handleInternalError(ctx, "Ошибка при удалении студента", e);
        }
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    private StudentDto parseRequestBody(Context ctx) throws ValidationException {
        try {
            String body = ctx.body();
            logger.debug("Тело запроса: {}", body);
            StudentDto dto = objectMapper.readValue(body, StudentDto.class);
            logger.debug("Десериализован DTO: {}", dto);
            return dto;
        } catch (JsonProcessingException e) {
            logger.warn("Ошибка парсинга JSON: {}", e.getMessage());
            throw new ValidationException("Некорректный JSON");
        }
    }

    private Student createAndValidateStudent(StudentDto dto) throws ValidationException {
        try {
            return service.createStudent(dto.toDomain());
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректные данные студента: {}", e.getMessage());
            throw new ValidationException(e.getMessage());
        }
    }

    private Long getIdFromPath(Context ctx) {
        try {
            String idParam = ctx.pathParam("id");
            logger.debug("Получен параметр ID: {}", idParam);
            return Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            logger.warn("Получен некорректный ID: {}", ctx.pathParam("id"));
            throw new ValidationException("Некорректный ID");
        }
    }

    private void sendSuccessResponse(Context ctx, Student student) {
        logger.info("Создан новый студент с ID: {}", student.getId());
        ctx.status(HttpStatus.CREATED)
                .json(StudentDto.fromDomain(student));
    }

    private void handleValidationError(Context ctx, ValidationException e) {
        ctx.status(HttpStatus.BAD_REQUEST)
                .json(new ErrorResponse(e.getMessage()));
    }

    private void handleNotFound(Context ctx, Long id) {
        logger.warn("Студент с ID {} не найден", id);
        ctx.status(HttpStatus.NOT_FOUND)
                .json(new ErrorResponse("Студент не найден"));
    }

    private void handleInternalError(Context ctx, String message, Exception e) {
        logger.error(message, e);
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .json(new ErrorResponse("Внутренняя ошибка сервера"));
    }

    private static class ValidationException extends RuntimeException {
        ValidationException(String message) {
            super(message);
        }
    }

    record ErrorResponse(String message) {
    }
}