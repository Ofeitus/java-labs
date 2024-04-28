package com.dashkevich.javalabs.lab_6;

import java.time.LocalDate;

public record User(Integer id, String fio, LocalDate dateOfBirth) {
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fio='" + fio + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
