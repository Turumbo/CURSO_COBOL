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
@Table(name = "order_lines")
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemName;
    private Long menuItemId;
    private BigDecimal unitPrice;
    private int quantity;
    private String customizations;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    protected OrderLine() {
    }

    public OrderLine(Long menuItemId, String itemName, BigDecimal unitPrice, int quantity, String customizations) {
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.customizations = customizations;
    }

    void setOrder(CustomerOrder order) { this.order = order; }
    public Long getId() { return id; }
    public String getItemName() { return itemName; }
    public Long getMenuItemId() { return menuItemId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public String getCustomizations() { return customizations; }
}
