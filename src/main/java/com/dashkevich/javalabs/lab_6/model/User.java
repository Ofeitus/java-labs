package com.dashkevich.javalabs.lab_6.model;

import java.time.LocalDate;
import java.util.Objects;

public record User(Integer id, String fio, LocalDate dateOfBirth) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(fio, user.fio) && Objects.equals(dateOfBirth, user.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fio, dateOfBirth);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fio='" + fio + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
