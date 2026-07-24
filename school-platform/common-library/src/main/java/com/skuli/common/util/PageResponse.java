package com.skuli.common.util;

import java.util.List;

/**
 * Transport-friendly pagination envelope returned by list endpoints, mirroring Spring Data's
 * {@code Page} without leaking JPA types across the API boundary.
 *
 * @param content       the page of items
 * @param page          zero-based page index
 * @param size          page size
 * @param totalElements total number of matching items
 * @param totalPages     total number of pages
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
