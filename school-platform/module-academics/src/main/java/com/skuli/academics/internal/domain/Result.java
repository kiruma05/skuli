package com.skuli.academics.internal.domain;

import com.skuli.common.domain.TenantAware;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A score a student earned on either an exam or an assignment (exactly one of the two is set;
 * both are nullable, mirroring the Prisma model). The student lives in module-student, held by id.
 */
@Entity
@Table(name = "result")
public class Result extends TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "exam_id")
    private Integer examId;

    @Column(name = "assignment_id")
    private Integer assignmentId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
