package com.skuli.common.validation;

/**
 * Bean Validation group for constraints that apply only when creating a resource (not updating).
 * Used e.g. so a new user's password is required on {@code POST} but optional on {@code PUT},
 * where a null password means "leave the existing credential unchanged".
 */
public interface OnCreate {
}
