package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.MenuItem;
import br.com.bora.delivery.domain.Restaurant;
import br.com.bora.delivery.service.CustomizationCatalog;

import java.math.BigDecimal;
import java.util.List;

public record RestaurantDto(
        Long id,
        String name,
        String category,
        String cuisine,
        String description,
        double rating,
        int deliveryMinutes,
        BigDecimal deliveryFee,
        BigDecimal minimumOrder,
        String imageUrl,
        boolean featured,
        double latitude,
        double longitude,
        List<MenuItemDto> menuItems) {

    public static RestaurantDto from(Restaurant restaurant) {
        return from(restaurant, true);
    }

    public static RestaurantDto from(Restaurant restaurant, boolean includeMenu) {
        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCategory(),
                restaurant.getCuisine(),
                restaurant.getDescription(),
                restaurant.getRating(),
                restaurant.getDeliveryMinutes(),
                restaurant.getDeliveryFee(),
                restaurant.getMinimumOrder(),
                restaurant.getImageUrl(),
                restaurant.isFeatured(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                includeMenu ? restaurant.getMenuItems().stream().map(MenuItemDto::from).toList() : List.of());
    }

    public record MenuItemDto(Long id, String name, String description, String category,
                              BigDecimal price, String imageUrl, boolean available,
                              List<CustomizationOptionDto> customizationOptions) {
        public static MenuItemDto from(MenuItem item) {
            return new MenuItemDto(item.getId(), item.getName(), item.getDescription(),
                    item.getCategory(), item.getPrice(), item.getImageUrl(), item.isAvailable(),
                    CustomizationCatalog.optionsFor(item.getCategory()).stream()
                            .map(option -> new CustomizationOptionDto(option.name(), option.price())).toList());
        }
    }

    public record CustomizationOptionDto(String name, BigDecimal price) {
    }
}
