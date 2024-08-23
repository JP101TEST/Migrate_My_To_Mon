package com.example.migrate.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;


@Entity
@Data
@Table(name = "car")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "model_name", nullable = false, unique = true)
    private String modelName;

    @Column(name = "company_name", nullable = false)
    private String company;

    @Column(name = "`release`", nullable = false)
    private Date release;
}
