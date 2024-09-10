package com.example.migrate.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/migrate")
public class Migrate extends ApiAbstract {

    @PostMapping("")
    public Object migrate(@RequestParam("table") String tableName) {
        return migrate.migrate(tableName);
    }


}
