package com.project.controller;

import com.project.model.Student;
import com.project.service.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api")
@Tag(name = "Student")
public class StudentRestController {

    private StudentService studentService;

    @Autowired
    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/studenci/{studentId}")
    ResponseEntity<Student> getStudent(@PathVariable("studentId") Integer studentId) {
        return ResponseEntity.of(studentService.getStudent(studentId));
    }

    @GetMapping("/studenci")
    public Page<Student> getStudenci(@ParameterObject Pageable pageable) {
        return studentService.getStudenci(pageable);
    }

    @GetMapping(value = "/studenci", params = "nazwisko")
    public Page<Student> getStudenciByNazwisko(@RequestParam(name = "nazwisko") String nazwisko,
                                               @ParameterObject Pageable pageable) {
        return studentService.searchByNazwisko(nazwisko, pageable);
    }

    @GetMapping(value = "/studenci", params = "nrIndeksu")
    public Page<Student> getStudenciByNrIndeksu(@RequestParam(name = "nrIndeksu") String nrIndeksu,
                                                @ParameterObject Pageable pageable) {
        return studentService.searchByNrIndeksu(nrIndeksu, pageable);
    }

    @PostMapping("/studenci")
    ResponseEntity<Void> createStudent(@Valid @RequestBody Student student) {
        Student created = studentService.setStudent(student);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{studentId}").buildAndExpand(created.getStudentId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/studenci/{studentId}")
    public ResponseEntity<Void> updateStudent(@Valid @RequestBody Student student,
                                              @PathVariable("studentId") Integer studentId) {
        return studentService.getStudent(studentId)
                .map(s -> {
                    studentService.setStudent(student);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/studenci/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("studentId") Integer studentId) {
        return studentService.getStudent(studentId)
                .map(s -> {
                    studentService.deleteStudent(studentId);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}