package br.com.bora.delivery.api.dto;

import br.com.bora.delivery.domain.OrderReview;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ReviewDtos {
    private ReviewDtos() {
    }

    public record CreateReviewRequest(@NotNull Long orderId, @Min(1) @Max(5) int rating,
                                      @Size(max = 1200) String comment) {
    }

    public record ReviewDto(Long id, Long orderId, Long restaurantId, int rating,
                            String comment, Instant createdAt) {
        public static ReviewDto from(OrderReview review) {
            return new ReviewDto(review.getId(), review.getOrderId(), review.getRestaurantId(),
                    review.getRating(), review.getComment(), review.getCreatedAt());
        }
    }
}
