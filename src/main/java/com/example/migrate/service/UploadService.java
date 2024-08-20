package com.example.migrate.service;

import com.example.migrate.controller.ReadFile;
import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.entity.TechCompanies;
import com.example.migrate.repository.TechCompaniesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Log4j2
public class UploadService {
    private final ObjectMapper objectMapper;
    private final TechCompaniesRepository techCompaniesRepository;

    public UploadService(ObjectMapper objectMapper, TechCompaniesRepository techCompaniesRepository) {
        this.objectMapper = objectMapper;
        this.techCompaniesRepository = techCompaniesRepository;
    }

    public Object upload(MultipartFile file) {
        try {
        List<JsonNode> data = ReadFile.readCsv(file);
        for (JsonNode dataInsert : data){
            TechCompanies techCompanies = objectMapper.readValue(dataInsert.toString(),TechCompanies.class);
            if (techCompaniesRepository.findByCompany(techCompanies.getCompany()).isPresent()){
                continue;
            }
            techCompaniesRepository.save(techCompanies);
        }
            log.info("Upload |{}| successful.", file.getOriginalFilename());
        return new ResponseGeneral("200","Upload "+file.getOriginalFilename()+" successful.");
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseGeneral("400",e.getMessage());
        }
    }
}
