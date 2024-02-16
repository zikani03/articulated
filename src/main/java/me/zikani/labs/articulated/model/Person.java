package me.zikani.labs.articulated.model;

import java.time.Duration;
import java.time.LocalDate;

public class Person {
    private LocalDate dateOfBirth;

    public Person(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int age(LocalDate asOf) {
        var cmp = asOf;
        if (asOf == null) {
            cmp = LocalDate.now();
        }
        return cmp.getYear() - dateOfBirth.getYear();
    }
}
