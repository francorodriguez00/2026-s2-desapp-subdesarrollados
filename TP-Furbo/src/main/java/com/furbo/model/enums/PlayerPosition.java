package com.furbo.model.enums;

import java.util.Arrays;
import java.util.Optional;

public enum PlayerPosition {
    GOALKEEPER("Goalkeeper"), DEFENCE("Defence"), MIDFIELD("Midfield"), OFFENCE("Offence");

    private final String apiValue;

    PlayerPosition(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public static Optional<PlayerPosition> parse(String value) {
        if (value == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(p -> p.apiValue.equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
