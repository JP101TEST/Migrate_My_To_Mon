package com.example.migrate.service.ole;

import com.example.migrate.dto.response.ResponseGeneral;
import com.example.migrate.entity.Car;
import com.example.migrate.entity.TechCompanies;
import com.example.migrate.entity.Test;
import com.example.migrate.exception.CustomException;
import com.example.migrate.service.ServiceAbstract;
import com.example.migrate.service.utility.readFile.csv.ReadCsvFile;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class UploadService extends ServiceAbstract {
    private String fileName;

    public Object upload(MultipartFile file, String tableName) {
        try {
            // Read file
            List<JsonNode> data = ReadCsvFile.readCsvToListJsonNode(file);

            fileName = file.getOriginalFilename();
            //System.out.println(data.size());
            insertToMySql(data, tableName);

            log.info("Upload |{}| successful.", file.getOriginalFilename());
            return new ResponseGeneral("200", "Upload " + file.getOriginalFilename() + " successful.");
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseGeneral("400", e.getMessage());
        }
    }

    public void insertToMySql(List<JsonNode> data, String tableName) throws CustomException {
        for (JsonNode dataInsert : data) {
            switch (tableName) {
                case "car":
                    try {
                        handleCarInsertion(dataInsert);
                    } catch (Exception e) {
                        log.error("Upload | Error processing data for table | " + tableName + " | from | " + fileName + "|.");
                        throw new CustomException("Upload | Error processing data for table | " + tableName + " | from | " + fileName + "|.");
                    }
                    break;
                case "tech_companies":
                    try {
                        handleTechCompaniesInsertion(dataInsert);
                    } catch (Exception e) {
                        log.error("Upload | Error processing data for table | " + tableName + " | from | " + fileName + "|.");
                        throw new CustomException("Upload | Error processing data for table | " + tableName + " | from | " + fileName + "|.");
                    }
                    break;
                case "test":
                    try {
                        handleTestInsertion(dataInsert);
                    } catch (Exception e) {
                        log.error("Upload | Error processing data for table | " + tableName + " | from | " + fileName + "|.");
                        throw new CustomException("Upload | Error processing data for table | " + tableName + " | from | " + fileName + "|.");
                    }
                    break;
                default:
                    log.error("Upload | Unknown table name: " + tableName);
                    throw new CustomException("Upload | Unknown table name: " + tableName);
            }
        }
    }

    private void handleCarInsertion(JsonNode dataInsert) throws Exception {
        Car car = objectMapper.readValue(dataInsert.toString(), Car.class);
        Optional<Car> existingCar = carRepository.findByModelName(car.getModelName());
        if (existingCar.isEmpty()) {
            carRepository.save(car);
        }
    }

    private void handleTechCompaniesInsertion(JsonNode dataInsert) throws Exception {
        TechCompanies techCompanies = objectMapper.readValue(dataInsert.toString(), TechCompanies.class);
        Optional<TechCompanies> existingTechCompanies = techCompaniesRepository.findByCompany(techCompanies.getCompany());
        if (existingTechCompanies.isEmpty()) {
            techCompaniesRepository.save(techCompanies);
        }
    }

    private void handleTestInsertion(JsonNode dataInsert) throws Exception {
        //System.out.println("testRepository.findAll().size() : " + testRepository.findAll().size());
        while (true) {
            //System.out.println(testRepository.findAll().size());
            if (testRepository.findAll().size() == 10000) {
                throw new CustomException("Max is 10000");
            }
            Test test = objectMapper.readValue(dataInsert.toString(), Test.class);
            testRepository.save(test);
        }
    }
}
