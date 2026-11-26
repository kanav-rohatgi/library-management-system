package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "students")
@Check(constraints = "academic_year >= 1")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String rollNumber;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 32)
    private String phone;

    @Column(nullable = false, length = 120)
    private String department;

    @Column(nullable = false)
    private int academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StudentStatus status;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean archived;

    protected Student() {
    }

    public Student(String rollNumber, String name, String email, String phone,
                   String department, int academicYear, StudentStatus status) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.academicYear = academicYear;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public boolean isArchived() {
        return archived;
    }

    public void archive() {
        archived = true;
    }

    public void revise(Student details) {
        rollNumber = details.rollNumber;
        name = details.name;
        email = details.email;
        phone = details.phone;
        department = details.department;
        academicYear = details.academicYear;
        status = details.status;
    }
}
