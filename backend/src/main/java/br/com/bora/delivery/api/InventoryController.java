package br.com.bora.delivery.api;

import br.com.bora.delivery.api.dto.InventoryDtos.InventoryItemDto;
import br.com.bora.delivery.api.dto.InventoryDtos.UpdateStockRequest;
import br.com.bora.delivery.api.dto.PageDto;
import br.com.bora.delivery.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public PageDto<InventoryItemDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return inventoryService.list(page, size);
    }

    @PatchMapping("/{menuItemId}")
    public InventoryItemDto update(
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateStockRequest request) {
        return inventoryService.updateStock(menuItemId, request.quantity());
    }
}
