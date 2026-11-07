package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.InventoryDtos.InventoryItemDto;
import br.com.bora.delivery.api.dto.PageDto;
import br.com.bora.delivery.domain.MenuItem;
import br.com.bora.delivery.repository.MenuItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final MenuItemRepository menuItemRepository;

    public InventoryService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public PageDto<InventoryItemDto> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Use página a partir de zero e tamanho entre 1 e 100.");
        }
        return PageDto.from(menuItemRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))).map(InventoryItemDto::from));
    }

    @Transactional
    public InventoryItemDto updateStock(Long menuItemId, int quantity) {
        if (quantity < 0 || quantity > 100_000) {
            throw new IllegalArgumentException("A quantidade deve estar entre 0 e 100.000 unidades.");
        }
        MenuItem item = menuItemRepository.findByIdForUpdate(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        item.setStockQuantity(quantity);
        return InventoryItemDto.from(item);
    }
}
