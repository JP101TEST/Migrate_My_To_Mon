package com.example.migrate.service.entity;

import com.example.migrate.exception.CustomException;
import com.example.migrate.service.ServiceAbstract;
import org.springframework.stereotype.Component;

@Component
public class EntityGenerate extends ServiceAbstract {
    public EntityAbstract getEntity(String tableName) throws Exception {
        return switch (tableName) {
            case "car" -> new E_Car(carRepository);
            case "tech_companies" -> new E_TechCompanies(techCompaniesRepository);
            default -> throw new CustomException("Not found table " + tableName + " in mysql.");
        };
    }
}
