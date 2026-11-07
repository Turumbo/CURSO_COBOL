package br.com.bora.delivery.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private Long customerAccountId;
    private String deliveryAddress;
    private double latitude;
    private double longitude;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal discount;
    private BigDecimal total;
    private String promoCode;
    private Instant createdAt;
    private Instant statusUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderLine> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(Long customerAccountId, String customerName, String deliveryAddress, double latitude, double longitude,
                         PaymentMethod paymentMethod, Restaurant restaurant, BigDecimal subtotal,
                         BigDecimal deliveryFee, BigDecimal discount, BigDecimal total, String promoCode) {
        this.customerName = customerName;
        this.customerAccountId = customerAccountId;
        this.deliveryAddress = deliveryAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.paymentMethod = paymentMethod;
        this.restaurant = restaurant;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.discount = discount;
        this.total = total;
        this.promoCode = promoCode;
        this.status = OrderStatus.RECEIVED;
        this.createdAt = Instant.now();
        this.statusUpdatedAt = this.createdAt;
    }

    public void addItem(OrderLine item) {
        items.add(item);
        item.setOrder(this);
    }

    public void applyTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public Long getCustomerAccountId() { return customerAccountId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getTotal() { return total; }
    public String getPromoCode() { return promoCode; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStatusUpdatedAt() { return statusUpdatedAt; }
    public Restaurant getRestaurant() { return restaurant; }
    public List<OrderLine> getItems() { return items; }
    public void setStatus(OrderStatus status) {
        this.status = status;
        this.statusUpdatedAt = Instant.now();
    }
}
