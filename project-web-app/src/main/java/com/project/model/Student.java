package com.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student {

    private Integer studentId;

    @NotBlank(message = "Pole imię nie może być puste!")
    private String imie;

    @NotBlank(message = "Pole nazwisko nie może być puste!")
    private String nazwisko;

    @NotBlank(message = "Pole nr indeksu nie może być puste!")
    private String nrIndeksu;

    @NotBlank(message = "Pole email nie może być puste!")
    @Email(message = "Niepoprawny format adresu email!")
    private String email;

    private Boolean stacjonarny;
}
