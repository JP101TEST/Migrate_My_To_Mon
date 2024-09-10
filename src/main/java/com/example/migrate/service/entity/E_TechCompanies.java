package com.example.migrate.service.entity;

import com.example.migrate.entity.Car;
import com.example.migrate.entity.TechCompanies;
import com.example.migrate.repository.TechCompaniesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class E_TechCompanies extends EntityAbstract {

    private final TechCompaniesRepository techCompaniesRepository;

    public E_TechCompanies(TechCompaniesRepository techCompaniesRepository) {
        this.techCompaniesRepository = techCompaniesRepository;
    }

    @Override
    public void upload(List<JsonNode> data) throws Exception {
        try {
            for (JsonNode dataInsert : data) {
                TechCompanies techCompanies = objectMapper.readValue(dataInsert.toString(), TechCompanies.class);
                Optional<TechCompanies> existingTechCompanies = techCompaniesRepository.findByCompany(techCompanies.getCompany());
                if (existingTechCompanies.isEmpty()) {
                    techCompaniesRepository.save(techCompanies);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<JsonNode> getListJson() {
        List<TechCompanies> techCompanies = techCompaniesRepository.findAll();
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (TechCompanies row : techCompanies) {
            jsonNodes.add(objectMapper.convertValue(row, JsonNode.class));

        }
        return jsonNodes;
    }

    @Override
    public List<JsonNode> getListJsonLimit(Pageable pageable) {
        Page<TechCompanies> techCompanies  = techCompaniesRepository.findAll(pageable);
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (TechCompanies row : techCompanies) {
            jsonNodes.add(objectMapper.convertValue(row, JsonNode.class));
        }
        return jsonNodes;
    }

    @Override
    public long getSize() {
        return techCompaniesRepository.count();
    }

}
