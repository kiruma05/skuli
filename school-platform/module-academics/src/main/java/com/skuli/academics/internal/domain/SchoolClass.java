package com.skuli.academics.internal.domain;

import com.skuli.common.domain.TenantAware;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A class group (the Prisma {@code Class} model; renamed to avoid the {@code java.lang.Class}
 * clash and the SQL reserved word — the table is {@code classes}). The supervisor is a teacher in
 * module-staff, referenced by id (nullable). {@code name} is unique per tenant.
 */
@Entity
@Table(name = "classes")
public class SchoolClass extends TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "supervisor_id")
    private String supervisorId;

    @Column(name = "grade_id", nullable = false)
    private Integer gradeId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }
}
