package com.example.migrate.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tech_companies")
public class TechCompanies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "ranking", nullable = false)
    private int ranking;

    @Column(name = "company_name", nullable = false ,unique = true)
    private String company;

    @Column(name = "market_cap", nullable = false)
    private String marketCap;

    @Column(name = "stock", nullable = false)
    private String stock;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "sector", nullable = false)
    private String sector;

    @Column(name = "industry", nullable = false)
    private String industry;

}
