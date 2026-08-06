package com.skuli.student.api;

import java.util.List;
import java.util.Optional;

/**
 * Exposed, read-only lookups used to resolve a user's row-level visibility scope (§7): a student's
 * own class, and a parent's children. Consumed by the app layer when populating
 * {@link com.skuli.common.security.UserContext}. Tenant-scoped like all repository access.
 */
public interface StudentDirectory {

    /** The class the given student (id == username) belongs to, if the student exists. */
    Optional<Integer> classIdOf(String studentId);

    /** The children of the given parent (id == username): each child's id and class. */
    List<ChildRef> childrenOf(String parentId);

    record ChildRef(String studentId, Integer classId) {
    }
}
