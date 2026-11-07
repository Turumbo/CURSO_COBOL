package br.com.bora.delivery.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "order_reviews", uniqueConstraints = @UniqueConstraint(columnNames = "order_id"))
public class OrderReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    private Long customerAccountId;
    private Long restaurantId;
    private int rating;
    private String comment;
    private Instant createdAt;

    protected OrderReview() {
    }

    public OrderReview(Long orderId, Long customerAccountId, Long restaurantId, int rating, String comment) {
        this.orderId = orderId;
        this.customerAccountId = customerAccountId;
        this.restaurantId = restaurantId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getCustomerAccountId() { return customerAccountId; }
    public Long getRestaurantId() { return restaurantId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
