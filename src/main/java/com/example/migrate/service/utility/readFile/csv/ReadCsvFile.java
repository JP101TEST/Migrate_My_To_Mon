package com.example.migrate.service.utility.readFile.csv;

import com.example.migrate.exception.CustomException;
import com.example.migrate.service.utility.readFile.ReadFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class ReadCsvFile extends ReadFile {

    public static List<JsonNode> readCsvToListJsonNode(MultipartFile file) throws CustomException {

        String massage;
        if (file == null || file.isEmpty()) {
            massage = "File input is empty please select file";
            log.error(massage);
            throw new CustomException(massage);
        }

        if (!file.getOriginalFilename().split("\\.")[file.getOriginalFilename().split("\\.").length - 1].equals("csv")) {
            massage = "This file | " + file.getOriginalFilename() + " | is not csv";
            log.error(massage);
            throw new CustomException(massage);
        }

        // Read file
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            CSVReader csvReader = new CSVReaderBuilder(bufferedReader).withCSVParser(new CSVParserBuilder().withSeparator(',').build()).build();
            // Get header
            String[] header = csvReader.readNext();
            if (header == null || header.length == 0) {
                massage = "This file | " + file.getOriginalFilename() + " | is empty.";
                log.error(massage);
                throw new CustomException(massage);
            }

            List<JsonNode> result = new ArrayList<>();
            String[] row;

            while ((row = csvReader.readNext()) != null) {
                Map<String, Object> data = new HashMap<>();
                for (int i = 0; i < header.length; i++) {
                    if (i < row.length && !row[i].isEmpty()) {
                        String key = header[i];
                        Object value = parseValue(row[i]);
                        if (value != null) {
                            // Place value in correct nested object
                            putValue(data, key, value);
                        }
                    }
                }
                result.add(objectMapper.convertValue(data, JsonNode.class));
            }
            return result;
        } catch (Exception e) {
            log.error("Csv file is empty.");
            throw new CustomException("Csv file is empty.");
        }

    }

    private static Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        // แปลงเป็นชนิดข้อมูลปกติ
        try {
            // พยายามแปลงเป็น Integer
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // ถ้าไม่ใช่ Integer, ลองแปลงเป็น Double
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                // ถ้าไม่ใช่ Double, ลองแปลงเป็น Boolean
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    return Boolean.parseBoolean(value);
                }
                return value;
            }
        }

    }

    private static void putValue(Map<String, Object> map, String key, Object value) {
        // แยกคีย์ออกเป็นส่วน ๆ โดยใช้จุด (.) เป็นตัวแยก
        String[] parts = key.split("\\.");
        Map<String, Object> current = map;
        // วนลูปผ่านแต่ละส่วนของคีย์ ยกเว้นส่วนสุดท้าย
        for (int i = 0; i < parts.length - 1; i++) {
            // ตรวจสอบว่าคีย์ส่วนปัจจุบันมีอยู่ใน Map หรือไม่
            // ถ้าไม่มี หรือไม่ใช่ Map ก็สร้าง Map ใหม่ใส่เข้าไป
            if (!current.containsKey(parts[i]) || !(current.get(parts[i]) instanceof Map)) {
                current.put(parts[i], new HashMap<String, Object>());
            }
            // เปลี่ยน current ให้ชี้ไปที่ Map ที่สร้างใหม่ หรือที่มีอยู่แล้ว
            current = (Map<String, Object>) current.get(parts[i]);
        }
        // ใส่ค่า (value) ลงในคีย์ส่วนสุดท้าย
        current.put(parts[parts.length - 1], value);
    }
}
