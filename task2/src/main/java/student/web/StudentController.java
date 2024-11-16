package student.web;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import student.service.StudentService;
import student.web.dto.StudentDto;

public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    public void createStudent(Context ctx) {
        StudentDto dto = ctx.bodyAsClass(StudentDto.class);
        StudentDto created = StudentDto.fromDomain(
                service.createStudent(dto.toDomain()));
        ctx.status(HttpStatus.CREATED);
        ctx.json(created);
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