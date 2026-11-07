package br.com.bora.delivery.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private String cuisine;
    private String description;
    private double rating;
    private int deliveryMinutes;
    private BigDecimal deliveryFee;
    private BigDecimal minimumOrder;
    private String imageUrl;
    private boolean featured;
    private double latitude;
    private double longitude;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<MenuItem> menuItems = new ArrayList<>();

    protected Restaurant() {
    }

    public Restaurant(String name, String category, String cuisine, String description, double rating,
                      int deliveryMinutes, BigDecimal deliveryFee, BigDecimal minimumOrder,
                      String imageUrl, boolean featured, double latitude, double longitude) {
        this.name = name;
        this.category = category;
        this.cuisine = cuisine;
        this.description = description;
        this.rating = rating;
        this.deliveryMinutes = deliveryMinutes;
        this.deliveryFee = deliveryFee;
        this.minimumOrder = minimumOrder;
        this.imageUrl = imageUrl;
        this.featured = featured;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void addMenuItem(MenuItem item) {
        menuItems.add(item);
        item.setRestaurant(this);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getCuisine() { return cuisine; }
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    public int getDeliveryMinutes() { return deliveryMinutes; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public BigDecimal getMinimumOrder() { return minimumOrder; }
    public String getImageUrl() { return imageUrl; }
    public boolean isFeatured() { return featured; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public List<MenuItem> getMenuItems() { return menuItems; }
}
