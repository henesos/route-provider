package com.aviation.routeprovider.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class OperatingDays {

    private static final Set<Integer> VALID_DAYS = Set.of(1, 2, 3, 4, 5, 6, 7);

    private final Set<Integer> days;

    public OperatingDays() {
        this.days = Collections.emptySet();
    }

    @JsonCreator
    public OperatingDays(Set<Integer> days) {
        if (days == null) {
            this.days = Collections.emptySet();
        } else {
            validate(days);
            this.days = Set.copyOf(days);
        }
    }

    public OperatingDays(int... days) {
        if (days == null) {
            this.days = Collections.emptySet();
        } else {
            validate(days);
            this.days = Arrays.stream(days)
                .boxed()
                .collect(Collectors.toUnmodifiableSet());
        }
    }

    public boolean operatesOn(DayOfWeek day) {
        return days.contains(toDayNumber(day));
    }

    public boolean operatesOn(int dayNumber) {
        return days.contains(dayNumber);
    }

    public boolean isEmpty() {
        return days.isEmpty();
    }

    public boolean isDaily() {
        return days.size() == 7;
    }

    @JsonValue
    public Set<Integer> getDays() {
        return days;
    }

    public int[] toArray() {
        return days.stream()
            .mapToInt(Integer::intValue)
            .toArray();
    }

    public static OperatingDays daily() {
        return new OperatingDays(1, 2, 3, 4, 5, 6, 7);
    }

    public static OperatingDays empty() {
        return new OperatingDays();
    }

    public static int toDayNumber(DayOfWeek dayOfWeek) {
        return dayOfWeek.getValue();
    }

    public static DayOfWeek toDayOfWeek(int dayNumber) {
        if (!VALID_DAYS.contains(dayNumber)) {
            throw new IllegalArgumentException(
                "Invalid day number: " + dayNumber + ". Must be 1-7");
        }
        return DayOfWeek.of(dayNumber);
    }

    private void validate(int... days) {
        for (int day : days) {
            if (!VALID_DAYS.contains(day)) {
                throw new IllegalArgumentException(
                    "Invalid day number: " + day + ". Must be between 1 and 7");
            }
        }
    }

    private void validate(Set<Integer> days) {
        for (int day : days) {
            if (!VALID_DAYS.contains(day)) {
                throw new IllegalArgumentException(
                    "Invalid day number: " + day + ". Must be between 1 and 7");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperatingDays that = (OperatingDays) o;
        return Objects.equals(days, that.days);
    }

    @Override
    public int hashCode() {
        return Objects.hash(days);
    }

    @Override
    public String toString() {
        return "OperatingDays" + days;
    }
}
