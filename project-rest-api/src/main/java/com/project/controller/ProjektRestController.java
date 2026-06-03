package com.project.controller;

import com.project.model.Projekt;
import com.project.service.ProjektService;
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
@Tag(name = "Projekt")
public class ProjektRestController {

    private ProjektService projektService;

    @Autowired
    public ProjektRestController(ProjektService projektService) {
        this.projektService = projektService;
    }

    @GetMapping("/projekty/{projektId}")
    ResponseEntity<Projekt> getProjekt(@PathVariable("projektId") Integer projektId) {
        return ResponseEntity.of(projektService.getProjekt(projektId));
    }

    @PostMapping("/projekty")
    ResponseEntity<Void> createProjekt(@Valid @RequestBody Projekt projekt) {
        Projekt created = projektService.setProjekt(projekt);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{projektId}").buildAndExpand(created.getProjektId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/projekty/{projektId}")
    public ResponseEntity<Void> updateProjekt(@Valid @RequestBody Projekt projekt,
                                              @PathVariable("projektId") Integer projektId) {
        return projektService.getProjekt(projektId)
                .map(p -> {
                    projektService.setProjekt(projekt);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/projekty/{projektId}")
    public ResponseEntity<Void> deleteProjekt(@PathVariable("projektId") Integer projektId) {
        return projektService.getProjekt(projektId)
                .map(p -> {
                    projektService.deleteProjekt(projektId);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/projekty")
    public Page<Projekt> getProjekty(@ParameterObject Pageable pageable) {
        return projektService.getProjekty(pageable);
    }

    @GetMapping(value = "/projekty-nazwa", params = "nazwa")
    Page<Projekt> getProjektyByNazwa(@RequestParam(name = "nazwa") String nazwa,
                                     @ParameterObject Pageable pageable) {
        return projektService.searchByNazwa(nazwa, pageable);
    }
}