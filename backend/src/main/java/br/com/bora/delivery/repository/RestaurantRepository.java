package br.com.bora.delivery.repository;

import br.com.bora.delivery.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Restaurant> findByCategoryIgnoreCase(String category, Pageable pageable);
    Page<Restaurant> findByNameContainingIgnoreCaseAndCategoryIgnoreCase(String name, String category, Pageable pageable);
}
