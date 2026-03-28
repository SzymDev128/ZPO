package com.validation;
import com.validation.annotation.*;

public class Student {
    @NotNull(message = "Nie podano imienia")
    @Size(min = 3, max = 50,
            message = "Imię musi składać się z przynajmniej {min} znaków i nie przekraczać {max}")
    private String imie;
    @NotNull(message = "Nie podano nazwiska")
    @Size(min = 2, max = 50,
            message = "Nazwisko musi składać się z przynajmniej {min} znaków i nie przekraczać {max}")
    private String nazwisko;
    @NotNull(message = "Nie podano numeru indeksu")
    @NrIndeksu(message = "Niepoprawny numer indeksu") // tylko ośmiocyfrowy łańcuch znaków
    private String nrIndeksu;
    @NotEmpty(message = "Nie podano adresu e-mail")
    @Email(message = "Niepoprawny adres e-mail")
    private String email;
    public Student(){
    }
    public Student(String imie, String nazwisko, String nrIndeksu, String email) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.nrIndeksu = nrIndeksu;
        this.email = email;
    }

    public String getImie() {
        return imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public void setNazwisko(String nazwisko) {
        this.nazwisko = nazwisko;
    }

    public String getNrIndeksu() {
        return nrIndeksu;
    }

    public void setNrIndeksu(String nrIndeksu) {
        this.nrIndeksu = nrIndeksu;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
