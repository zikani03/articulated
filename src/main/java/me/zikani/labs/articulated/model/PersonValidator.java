package me.zikani.labs.articulated.model;

import java.time.Clock;
import java.time.LocalDate;

public class PersonValidator {
    private final Clock clock;

    public PersonValidator(Clock clock) {
        this.clock = clock;
    }

    public boolean isValid(Person person) {
        return this.isAllowedToWritePHP(person);
    }

    private boolean isAllowedToWritePHP(Person person) {
        return person.age(LocalDate.now(clock)) >= 18;
    }
}
