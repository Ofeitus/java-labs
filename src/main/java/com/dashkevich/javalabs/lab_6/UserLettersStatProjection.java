package com.dashkevich.javalabs.lab_6;

public record UserLettersStatProjection(User user, Integer lettersSent, Integer lettersReceived) {
    @Override
    public String toString() {
        return "UserLettersStatProjection{" +
                "user=" + user +
                ", lettersSent=" + lettersSent +
                ", lettersReceived=" + lettersReceived +
                '}';
    }
}
