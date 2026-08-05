package com.skuli.common.domain;

/**
 * Weekday a lesson is scheduled on. Ported from the Prisma {@code Day} enum (school week is
 * Monday–Friday); persisted as its {@code name()} via {@code @Enumerated(EnumType.STRING)}.
 */
public enum Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY
}
