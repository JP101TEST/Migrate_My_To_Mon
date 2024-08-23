package com.example.migrate.api;

import com.example.migrate.service.MigrateService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/migrate")
public class Migrate {
    private final MigrateService migrateService;

    public Migrate(MigrateService migrateService) {
        this.migrateService = migrateService;
    }

    @PostMapping("")
    public Object migrate(@RequestParam("table") String tableName,@RequestParam("limit") int limit) {
        return migrateService.migrate(tableName,limit);
    }
}
