package br.com.bora.delivery.domain;

import br.com.bora.delivery.security.SensitiveDataConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "demo_payment_methods")
public class DemoPaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 20)
    private String brand;

    @Convert(converter = SensitiveDataConverter.class)
    @Column(nullable = false, length = 100)
    private String lastFour;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false)
    private Instant createdAt;

    protected DemoPaymentMethod() {
    }

    public DemoPaymentMethod(Long accountId, String brand, String lastFour) {
        this.accountId = accountId;
        this.brand = brand;
        this.lastFour = lastFour;
        this.reference = "MOCK-CARD-" + UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getAccountId() { return accountId; }
    public String getBrand() { return brand; }
    public String getLastFour() { return lastFour; }
    public String getReference() { return reference; }
    public Instant getCreatedAt() { return createdAt; }
}
