package com.mcs.usage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class UsageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsageServiceApplication.class, args);
    }
}
