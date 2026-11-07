package br.com.bora.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_payment_order", columnNames = "order_id"))
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String providerReference;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false, length = 200)
    private String notice;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(Long orderId, PaymentMethod method, String providerReference,
                              BigDecimal amount, String notice) {
        this.orderId = orderId;
        this.method = method;
        this.status = "SIMULATED";
        this.providerReference = providerReference;
        this.amount = amount;
        this.createdAt = Instant.now();
        this.notice = notice;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public PaymentMethod getMethod() { return method; }
    public String getStatus() { return status; }
    public String getProviderReference() { return providerReference; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
    public String getNotice() { return notice; }
}
