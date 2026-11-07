package br.com.bora.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import br.com.bora.delivery.security.SensitiveDataConverter;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountRole role;
    @Convert(converter = SensitiveDataConverter.class)
    @Column(length = 220)
    private String phone;
    @Convert(converter = SensitiveDataConverter.class)
    @Column(length = 1400)
    private String deliveryAddress;

    protected Account() {
    }

    public Account(String name, String email, String passwordHash, AccountRole role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public void updateProfile(String name, String email, String phone, String deliveryAddress) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.deliveryAddress = deliveryAddress;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public AccountRole getRole() { return role; }
    public String getPhone() { return phone; }
    public String getDeliveryAddress() { return deliveryAddress; }
}
