package com.example.inventoryservice.config;

import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    @Bean
    CommandLineRunner seedInventory(InventoryRepository repo) {
        return args -> {
            if (repo.count() == 0) {

                repo.save(Inventory.builder()
                        .productCode("LAPTOP-001")
                        .productName("ThinkPad X1 Carbon")
                        .quantity(50)
                        .reservedQuantity(0)
                        .build());

                repo.save(Inventory.builder()
                        .productCode("PHONE-001")
                        .productName("Pixel 8 Pro")
                        .quantity(120)
                        .reservedQuantity(5)
                        .build());

                repo.save(Inventory.builder()
                        .productCode("TABLET-001")
                        .productName("iPad Air")
                        .quantity(0)
                        .reservedQuantity(0)
                        .build());

                log.info("Inventory seeded successfully with default products");
            }
        };
    }
}