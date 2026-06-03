package com.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "zadanie")
public class Zadanie {

    @Id
    @GeneratedValue
    @Column(name = "zadanie_id")
    private Integer zadanieId;

    @NotBlank(message = "Pole nazwa nie może być puste!")
    @Column(nullable = false, length = 50)
    private String nazwa;

    @Column(nullable = false)
    private Integer kolejnosc;

    @Column(length = 1000)
    private String opis;

    @CreatedDate
    @Column(name = "dataczas_dodania", nullable = false, updatable = false)
    private LocalDateTime dataczasDodania;

    @ManyToOne
    @JoinColumn(name = "projekt_id")
    private Projekt projekt;
}