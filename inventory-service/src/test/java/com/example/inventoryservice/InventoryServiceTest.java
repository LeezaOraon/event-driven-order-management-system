package com.example.inventoryservice;

import com.example.inventoryservice.dto.InventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.dto.StockCheckResponse;
import com.example.inventoryservice.exception.InsufficientStockException;
import com.example.inventoryservice.exception.ResourceNotFoundException;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory sampleInventory;

    @BeforeEach
    void setUp() {
        sampleInventory = Inventory.builder()
                .id(1L)
                .productCode("LAPTOP-001")
                .productName("ThinkPad X1 Carbon")
                .quantity(50)
                .reservedQuantity(5)
                .build();
    }

    @Test
    void checkStock_shouldReturnAvailableWhenSufficientStock() {
        when(inventoryRepository.findByProductCode("LAPTOP-001"))
                .thenReturn(Optional.of(sampleInventory));

        StockCheckResponse response = inventoryService.checkStock("LAPTOP-001", 10);

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getAvailableQuantity()).isEqualTo(45);
    }

    @Test
    void checkStock_shouldReturnUnavailableWhenInsufficientStock() {
        when(inventoryRepository.findByProductCode("LAPTOP-001"))
                .thenReturn(Optional.of(sampleInventory));

        StockCheckResponse response = inventoryService.checkStock("LAPTOP-001", 100);

        assertThat(response.isAvailable()).isFalse();
    }

    @Test
    void reserveStock_shouldReduceAvailableQuantity() {
        when(inventoryRepository.findByProductCode("LAPTOP-001"))
                .thenReturn(Optional.of(sampleInventory));
        when(inventoryRepository.save(any())).thenReturn(sampleInventory);

        inventoryService.reserveStock("LAPTOP-001", 10);

        verify(inventoryRepository).save(argThat(inv -> inv.getReservedQuantity() == 15));
    }

    @Test
    void reserveStock_shouldThrowWhenInsufficientStock() {
        when(inventoryRepository.findByProductCode("LAPTOP-001"))
                .thenReturn(Optional.of(sampleInventory));

        assertThatThrownBy(() -> inventoryService.reserveStock("LAPTOP-001", 100))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("LAPTOP-001");
    }

    @Test
    void addInventory_shouldThrowWhenProductAlreadyExists() {
        when(inventoryRepository.existsByProductCode("LAPTOP-001")).thenReturn(true);

        InventoryRequest request = new InventoryRequest("LAPTOP-001", "ThinkPad", 10);
        assertThatThrownBy(() -> inventoryService.addInventory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getByProductCode_shouldThrowWhenNotFound() {
        when(inventoryRepository.findByProductCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getByProductCode("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}