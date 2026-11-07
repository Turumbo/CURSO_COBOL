package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.PaymentTransaction;
import br.com.bora.delivery.domain.DemoPaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class PaymentDtos {
    private PaymentDtos() {
    }

    public record PaymentTransactionDto(
            Long orderId,
            String method,
            String status,
            String providerReference,
            BigDecimal amount,
            Instant createdAt,
            String notice) {
        public static PaymentTransactionDto from(PaymentTransaction transaction) {
            return new PaymentTransactionDto(transaction.getOrderId(), transaction.getMethod().name(),
                    transaction.getStatus(), transaction.getProviderReference(), transaction.getAmount(),
                    transaction.getCreatedAt(), transaction.getNotice());
        }
    }

    public record FiscalDocumentDto(
            Long orderId,
            String reference,
            String documentType,
            BigDecimal amount,
            Instant createdAt,
            boolean legallyValid,
            String notice) {
    }

    public record AddDemoCardRequest(
            @NotBlank @Pattern(regexp = "VISA|MASTERCARD|ELO|AMEX") String brand,
            @NotBlank @Pattern(regexp = "[0-9]{4}") String lastFour) {
    }

    public record DemoCardDto(Long id, String brand, String lastFour, String reference, String notice) {
        public static DemoCardDto from(DemoPaymentMethod method) {
            return new DemoCardDto(method.getId(), method.getBrand(), method.getLastFour(), method.getReference(),
                    "Cartão demonstrativo: não está tokenizado e não pode ser usado para cobrança.");
        }
    }
}
