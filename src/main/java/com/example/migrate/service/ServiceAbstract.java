package com.example.migrate.service;

import com.example.migrate.repository.CarRepository;
import com.example.migrate.repository.TechCompaniesRepository;
import com.example.migrate.repository.TestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class ServiceAbstract {
    protected final String LOCAL_STORE = "local";
    @Value("${mongodb_url}")
    protected String MONGODB_URL;
    @Value("${mongodb_database}")
    protected String MONGODB_DATABASE;
    @Value("${mongodb.collection.test_year}")
    protected String MONGODB_COLLECTION_TEST_YEAR;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected TechCompaniesRepository techCompaniesRepository;
    @Autowired
    protected CarRepository carRepository;
    @Autowired
    protected TestRepository testRepository;
}
