package com.dashkevich.javalabs.lab_5_1;

/**
 * Клиент проката
 */
public record Client(int number, boolean pensioner) {

    @Override
    public String toString() {
        return number + (pensioner ? " (пенсионер)" : "");
    }
}
