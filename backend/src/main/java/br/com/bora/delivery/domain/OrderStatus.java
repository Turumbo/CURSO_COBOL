package br.com.bora.delivery.domain;

public enum OrderStatus {
    RECEIVED,
    PREPARING,
    READY_FOR_PICKUP,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}
