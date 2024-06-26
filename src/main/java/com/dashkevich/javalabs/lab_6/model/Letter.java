package com.dashkevich.javalabs.lab_6.model;

import java.time.LocalDateTime;

public record Letter(
        Integer id,
        Integer sender,
        Integer recipient,
        String subject,
        String body,
        LocalDateTime dateOfSending
) {}
