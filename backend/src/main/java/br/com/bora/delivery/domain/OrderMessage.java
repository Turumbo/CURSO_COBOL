package br.com.bora.delivery.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "order_messages")
public class OrderMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private Long senderAccountId;
    private String senderName;
    @Enumerated(EnumType.STRING)
    private AccountRole senderRole;
    private String message;
    private Instant sentAt;

    protected OrderMessage() {
    }

    public OrderMessage(Long orderId, Long senderAccountId, String senderName, AccountRole senderRole, String message) {
        this.orderId = orderId;
        this.senderAccountId = senderAccountId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.message = message;
        this.sentAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getSenderAccountId() { return senderAccountId; }
    public String getSenderName() { return senderName; }
    public AccountRole getSenderRole() { return senderRole; }
    public String getMessage() { return message; }
    public Instant getSentAt() { return sentAt; }
}
