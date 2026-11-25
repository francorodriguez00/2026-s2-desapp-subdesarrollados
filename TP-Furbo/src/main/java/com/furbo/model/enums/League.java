package com.furbo.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum League {
    PL, PD, SA, BL1, FL1;

    public static Optional<League> parse(String value) {
        if (value == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(l -> l.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
