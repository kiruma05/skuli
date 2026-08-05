package com.skuli.academics.api;

import java.util.Optional;

/**
 * Exposed, read-only view of class groups for other modules. This is the sanctioned cross-module
 * seam: module-student needs a class's capacity to enforce the enrolment rule, but must not reach
 * into academics' internals — it depends on this interface instead. Lookups are scoped to the
 * current request's tenant.
 */
public interface ClassCatalog {

    /**
     * The capacity of the given class within the current tenant, or empty if no such class exists.
     */
    Optional<Integer> capacityOf(Integer classId);
}
