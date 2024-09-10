package com.example.migrate.service.entity;

import com.example.migrate.entity.Car;
import com.example.migrate.repository.CarRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class E_Car extends EntityAbstract {

    private final CarRepository carRepository;

    public E_Car(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    public void upload(List<JsonNode> data) throws Exception {
        try {
            for (JsonNode dataInsert : data) {
                Car car = objectMapper.readValue(dataInsert.toString(), Car.class);
                Optional<Car> existingCar = carRepository.findByModelName(car.getModelName());
                if (existingCar.isEmpty()) {
                    carRepository.save(car);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<JsonNode> getListJson() {
        List<Car> carList = carRepository.findAll();
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (Car row : carList) {
            jsonNodes.add(objectMapper.convertValue(row, JsonNode.class));
        }
        return jsonNodes;
    }

    @Override
    public List<JsonNode> getListJsonLimit(Pageable pageable) {
        Page<Car> carList = carRepository.findAll(pageable);
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (Car row : carList) {
            Map<String,Object> objectMap = objectMapper.convertValue(row, Map.class);
            objectMap.replace("release",row.getRelease().toString());
            jsonNodes.add(objectMapper.convertValue(objectMap, JsonNode.class));
        }
        return jsonNodes;
    }

    @Override
    public long getSize() {
        return carRepository.count();
    }


}

