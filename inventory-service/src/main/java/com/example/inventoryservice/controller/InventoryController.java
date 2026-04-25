package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.*;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponse> addInventory(@RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.addInventory(request));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<InventoryResponse> getByProductCode(
            @PathVariable("productCode") String productCode) {
        return ResponseEntity.ok(inventoryService.getByProductCode(productCode));
    }

    @GetMapping("/check/{productCode}")
    public ResponseEntity<StockCheckResponse> checkStock(
            @PathVariable("productCode") String productCode,
            @RequestParam("quantity") Integer quantity) {

        log.info("Checking stock for {} qty {}", productCode, quantity);
        return ResponseEntity.ok(inventoryService.checkStock(productCode, quantity));
    }

    @PatchMapping("/{productCode}/stock")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable("productCode") String productCode,
            @RequestParam("quantity") Integer quantity) {

        return ResponseEntity.ok(inventoryService.updateStock(productCode, quantity));
    }

    @PostMapping("/{productCode}/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable("productCode") String productCode,
            @RequestParam("quantity") Integer quantity) {

        return ResponseEntity.ok(inventoryService.reserveStock(productCode, quantity));
    }

    @PostMapping("/{productCode}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable("productCode") String productCode,
            @RequestParam("quantity") Integer quantity) {

        return ResponseEntity.ok(inventoryService.releaseStock(productCode, quantity));
    }
}