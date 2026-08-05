package com.skuli.staff.internal.domain;

import com.skuli.common.domain.AuditableEntity;
import com.skuli.common.domain.UserSex;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * A member of teaching staff. The primary key equals the Keycloak username (id == username
 * invariant), so it is assigned, not generated. The set of subjects a teacher can teach is the
 * former Prisma {@code _SubjectToTeacher} join, owned here in module-staff; the referenced
 * subjects live in module-academics and are held as ids, never as an object relationship.
 */
@Entity
@Table(name = "teacher")
public class Teacher extends AuditableEntity {

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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "subject_teachers", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject_id")
    private Set<Integer> subjectIds = new HashSet<>();

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

    public Set<Integer> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(Set<Integer> subjectIds) {
        this.subjectIds = subjectIds;
    }
}
