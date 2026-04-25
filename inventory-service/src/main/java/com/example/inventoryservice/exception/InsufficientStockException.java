package com.example.inventoryservice.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productCode, int available, int requested) {
        super(String.format(
                "Insufficient stock for %s: requested %d, available %d",
                productCode, requested, available
        ));
    }
}