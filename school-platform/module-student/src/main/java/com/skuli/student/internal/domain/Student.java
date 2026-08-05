package com.skuli.student.internal.domain;

import com.skuli.common.domain.AuditableEntity;
import com.skuli.common.domain.UserSex;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * An enrolled student. Id equals the Keycloak username. The parent lives in this same module
 * (module-student) but is still referenced by id for uniformity; the class and grade live in
 * module-academics and are held as ids, never as an object relationship across the boundary.
 */
@Entity
@Table(name = "student")
public class Student extends AuditableEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    private String email;

    private String phone;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(length = 500)
    private String img;

    @Column(name = "blood_type", nullable = false)
    private String bloodType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserSex sex;

    @Column(nullable = false)
    private Instant birthday;

    @Column(name = "parent_id", nullable = false)
    private String parentId;

    @Column(name = "class_id", nullable = false)
    private Integer classId;

    @Column(name = "grade_id", nullable = false)
    private Integer gradeId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public UserSex getSex() {
        return sex;
    }

    public void setSex(UserSex sex) {
        this.sex = sex;
    }

    public Instant getBirthday() {
        return birthday;
    }

    public void setBirthday(Instant birthday) {
        this.birthday = birthday;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }
}
