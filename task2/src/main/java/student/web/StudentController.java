package student.web;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import student.service.StudentService;
import student.web.dto.StudentDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import student.domain.Student;

public class StudentController {
    private final StudentService service;
    private final ObjectMapper objectMapper;

    public StudentController(StudentService service) {
        this.service = service;
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public void createStudent(Context ctx) {
        try {
            String body = ctx.body();
            StudentDto dto;
            try {
                dto = objectMapper.readValue(body, StudentDto.class);
            } catch (JsonProcessingException e) {
                ctx.status(400).json(new ErrorResponse("Некорректный JSON"));
                return;
            }

            try {
                Student student = service.createStudent(dto.toDomain());
                ctx.status(201).json(StudentDto.fromDomain(student));
            } catch (IllegalArgumentException e) {
                ctx.status(400).json(new ErrorResponse(e.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResponse("Внутренняя ошибка сервера"));
            e.printStackTrace();
        }
    }

    public void getStudent(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        service.findById(id)
                .map(StudentDto::fromDomain)
                .ifPresentOrElse(
                        dto -> ctx.json(dto),
                        () -> {
                            ctx.status(HttpStatus.NOT_FOUND);
                            ctx.json(new ErrorResponse("Студент не найден"));
                        });
    }

    public void getAllStudents(Context ctx) {
        ctx.json(
                service.findAll().stream()
                        .map(StudentDto::fromDomain)
                        .toList());
    }

    public void deleteStudent(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        service.deleteById(id);
        ctx.status(HttpStatus.NO_CONTENT);
    }

    record ErrorResponse(String message) {
    }
}