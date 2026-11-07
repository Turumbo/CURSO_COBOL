package br.com.bora.delivery.api;

import br.com.bora.delivery.service.RestaurantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final RestaurantService restaurantService;

    public CatalogController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/summary")
    public RestaurantService.CatalogSummary summary() {
        return restaurantService.summary();
    }
}
