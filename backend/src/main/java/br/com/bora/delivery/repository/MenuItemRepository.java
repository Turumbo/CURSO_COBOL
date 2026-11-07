package br.com.bora.delivery.repository;

import br.com.bora.delivery.domain.MenuItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from MenuItem item join fetch item.restaurant where item.id = :id")
    java.util.Optional<MenuItem> findByIdForUpdate(@Param("id") Long id);

    Page<MenuItem> findByRestaurantId(Long restaurantId, Pageable pageable);
    Page<MenuItem> findByRestaurantIdAndCategoryIgnoreCase(Long restaurantId, String category, Pageable pageable);

    @Query("""
            select item from MenuItem item
            where item.restaurant.id = :restaurantId
              and (:category is null or lower(item.category) = lower(:category))
              and (:term is null or lower(item.name) like lower(concat('%', :term, '%'))
                   or lower(item.description) like lower(concat('%', :term, '%')))
            """)
    Page<MenuItem> searchMenu(@Param("restaurantId") Long restaurantId,
                              @Param("category") String category,
                              @Param("term") String term,
                              Pageable pageable);
}
