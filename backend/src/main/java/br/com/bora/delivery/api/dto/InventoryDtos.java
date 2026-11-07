package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.MenuItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class InventoryDtos {
    private InventoryDtos() {
    }

    public record InventoryItemDto(
            Long menuItemId,
            String name,
            String category,
            Long restaurantId,
            String restaurantName,
            BigDecimal price,
            int stockQuantity,
            boolean lowStock) {
        public static InventoryItemDto from(MenuItem item) {
            return new InventoryItemDto(item.getId(), item.getName(), item.getCategory(),
                    item.getRestaurant().getId(), item.getRestaurant().getName(), item.getPrice(),
                    item.getStockQuantity(), item.getStockQuantity() <= 5);
        }
    }

    public record UpdateStockRequest(@NotNull @Min(0) @Max(100_000) Integer quantity) {
    }
}
