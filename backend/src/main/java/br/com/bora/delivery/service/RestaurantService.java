package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.PageDto;
import br.com.bora.delivery.api.dto.RestaurantDto;
import br.com.bora.delivery.repository.MenuItemRepository;
import br.com.bora.delivery.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public PageDto<RestaurantDto> listRestaurants(int page, int size, String category, String query, String sort) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(60, Math.max(1, size)),
                switch (sort == null ? "recommended" : sort) {
                    case "rating" -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by("name"));
                    case "delivery" -> Sort.by("deliveryMinutes").and(Sort.by("name"));
                    default -> Sort.by(Sort.Direction.DESC, "featured")
                            .and(Sort.by(Sort.Direction.DESC, "rating"))
                            .and(Sort.by("name"));
                });
        String term = query == null ? "" : query.trim();
        boolean hasCategory = category != null && !category.isBlank() && !"Todos".equalsIgnoreCase(category);
        Page<br.com.bora.delivery.domain.Restaurant> results;
        if (hasCategory && !term.isEmpty()) {
            results = restaurantRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCase(term, category.trim(), pageable);
        } else if (hasCategory) {
            results = restaurantRepository.findByCategoryIgnoreCase(category.trim(), pageable);
        } else if (!term.isEmpty()) {
            results = restaurantRepository.findByNameContainingIgnoreCase(term, pageable);
        } else {
            results = restaurantRepository.findAll(pageable);
        }
        return PageDto.from(results.map(restaurant -> RestaurantDto.from(restaurant, false)));
    }

    @Transactional(readOnly = true)
    public PageDto<RestaurantDto.MenuItemDto> listMenu(Long restaurantId, int page, int size,
                                                        String category, String query) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new IllegalArgumentException("Restaurante não encontrado.");
        }
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(100, Math.max(1, size)),
                Sort.by("id"));
        String categoryFilter = category == null || category.isBlank() || "Todos".equalsIgnoreCase(category)
                ? null : category.trim();
        String term = query == null || query.isBlank() ? null : query.trim();
        return PageDto.from(menuItemRepository.searchMenu(restaurantId, categoryFilter, term, pageable)
                .map(RestaurantDto.MenuItemDto::from));
    }

    @Transactional(readOnly = true)
    public CatalogSummary summary() {
        return new CatalogSummary(restaurantRepository.count(), menuItemRepository.count());
    }

    public record CatalogSummary(long restaurants, long menuItems) {
    }
}
