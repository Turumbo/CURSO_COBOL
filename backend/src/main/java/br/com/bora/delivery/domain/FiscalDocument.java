package br.com.bora.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "demo_fiscal_documents",
        uniqueConstraints = @UniqueConstraint(name = "uk_demo_fiscal_order", columnNames = "order_id"))
public class FiscalDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 40)
    private String reference;

    @Column(nullable = false, length = 60)
    private String documentType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean legallyValid;

    protected FiscalDocument() {
    }

    public FiscalDocument(Long orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.reference = String.format(java.util.Locale.ROOT, "BORA-DEMO-%08d", orderId);
        this.documentType = "COMPROVANTE_DEMO";
        this.amount = amount;
        this.createdAt = Instant.now();
        this.legallyValid = false;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getReference() { return reference; }
    public String getDocumentType() { return documentType; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isLegallyValid() { return legallyValid; }
}
