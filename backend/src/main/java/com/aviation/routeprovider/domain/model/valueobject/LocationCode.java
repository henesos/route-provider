package com.aviation.routeprovider.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.Objects;

public final class LocationCode {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 10;
    private static final int IATA_CODE_LENGTH = 3;

    private final String code;

    @JsonCreator
    public LocationCode(String code) {
        validate(code);
        this.code = code.toUpperCase(Locale.ENGLISH).trim();
    }

    @JsonValue
    public String getValue() {
        return code;
    }

    public boolean isIataCode() {
        return code.length() == IATA_CODE_LENGTH && isAlpha(code);
    }

    public boolean isCustomCode() {
        return code.length() > IATA_CODE_LENGTH;
    }

    private void validate(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Location code cannot be null or empty");
        }
        if (code.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Location code must be at least " + MIN_LENGTH + " characters");
        }
        if (code.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Location code cannot exceed " + MAX_LENGTH + " characters");
        }
        if (!isAlphanumeric(code)) {
            throw new IllegalArgumentException(
                    "Location code must contain only alphanumeric characters");
        }
    }

    private boolean isAlpha(String str) {
        return str.chars().allMatch(Character::isLetter);
    }

    private boolean isAlphanumeric(String str) {
        return str.chars().allMatch(Character::isLetterOrDigit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationCode that = (LocationCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
