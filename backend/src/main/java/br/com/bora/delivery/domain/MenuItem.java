package br.com.bora.delivery.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String imageUrl;
    private int stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    protected MenuItem() {
    }

    public MenuItem(String name, String description, String category, BigDecimal price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getStockQuantity() { return stockQuantity; }
    public boolean isAvailable() { return stockQuantity > 0; }
    public void reserveStock(int quantity) {
        if (quantity > stockQuantity) {
            throw new IllegalArgumentException("Estoque insuficiente para " + name + ".");
        }
        stockQuantity -= quantity;
    }
    public void restoreStock(int quantity) { stockQuantity += quantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public Restaurant getRestaurant() { return restaurant; }
    void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
}
