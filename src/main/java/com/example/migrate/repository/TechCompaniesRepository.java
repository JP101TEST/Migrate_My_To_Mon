package com.example.migrate.repository;

import com.example.migrate.entity.TechCompanies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechCompaniesRepository extends JpaRepository<TechCompanies, Integer> {
    Optional<TechCompanies> findByCompany(String company);
}
