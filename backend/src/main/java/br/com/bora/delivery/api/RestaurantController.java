package br.com.bora.delivery.api;

import br.com.bora.delivery.api.dto.RestaurantDto;
import br.com.bora.delivery.api.dto.PageDto;
import br.com.bora.delivery.service.RestaurantService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public PageDto<RestaurantDto> listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "18") int size,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "recommended") String sort) {
        return restaurantService.listRestaurants(page, size, category, q, sort);
    }

    @GetMapping("/{id}/menu")
    public PageDto<RestaurantDto.MenuItemDto> listMenu(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String q) {
        return restaurantService.listMenu(id, page, size, category, q);
    }

}
