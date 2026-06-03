package com.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student",
        indexes = {
                @Index(name = "idx_nazwisko", columnList = "nazwisko", unique = false),
                @Index(name = "idx_nr_indeksu", columnList = "nr_indeksu", unique = true)
        })
public class Student {

    @Id
    @GeneratedValue
    @Column(name = "student_id")
    private Integer studentId;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String imie;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nazwisko;

    @Column(name = "nr_indeksu", nullable = false, unique = true, length = 20)
    private String nrIndeksu;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    private Boolean stacjonarny;

    @ManyToMany(mappedBy = "studenci")
    private Set<Projekt> projekty;
}