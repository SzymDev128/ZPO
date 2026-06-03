package com.project.controller;

import com.project.model.Zadanie;
import com.project.service.ZadanieService;
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
@Tag(name = "Zadanie")
public class ZadanieRestController {

    private ZadanieService zadanieService;

    @Autowired
    public ZadanieRestController(ZadanieService zadanieService) {
        this.zadanieService = zadanieService;
    }

    @GetMapping("/zadania/{zadanieId}")
    ResponseEntity<Zadanie> getZadanie(@PathVariable("zadanieId") Integer zadanieId) {
        return ResponseEntity.of(zadanieService.getZadanie(zadanieId));
    }

    @GetMapping("/zadania")
    public Page<Zadanie> getZadania(@ParameterObject Pageable pageable) {
        return zadanieService.getZadania(pageable);
    }

    @GetMapping(value = "/zadania", params = "projektId")
    public Page<Zadanie> getZadaniaProjektu(@RequestParam(name = "projektId") Integer projektId,
                                            @ParameterObject Pageable pageable) {
        return zadanieService.getZadaniaProjektu(projektId, pageable);
    }

    @PostMapping("/zadania")
    ResponseEntity<Void> createZadanie(@Valid @RequestBody Zadanie zadanie) {
        Zadanie created = zadanieService.setZadanie(zadanie);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{zadanieId}").buildAndExpand(created.getZadanieId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/zadania/{zadanieId}")
    public ResponseEntity<Void> updateZadanie(@Valid @RequestBody Zadanie zadanie,
                                              @PathVariable("zadanieId") Integer zadanieId) {
        return zadanieService.getZadanie(zadanieId)
                .map(z -> {
                    zadanieService.setZadanie(zadanie);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/zadania/{zadanieId}")
    public ResponseEntity<Void> deleteZadanie(@PathVariable("zadanieId") Integer zadanieId) {
        return zadanieService.getZadanie(zadanieId)
                .map(z -> {
                    zadanieService.deleteZadanie(zadanieId);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}