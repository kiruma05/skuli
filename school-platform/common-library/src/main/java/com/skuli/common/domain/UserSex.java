package com.skuli.common.domain;

/**
 * Biological sex as captured on staff/student records. Ported from the Prisma {@code UserSex}
 * enum; persisted as its {@code name()} via {@code @Enumerated(EnumType.STRING)}.
 */
public enum UserSex {
    MALE,
    FEMALE
}
