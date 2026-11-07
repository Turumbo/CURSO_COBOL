package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.ChatDtos.MessageDto;
import br.com.bora.delivery.api.dto.ReviewDtos.CreateReviewRequest;
import br.com.bora.delivery.api.dto.ReviewDtos.ReviewDto;
import br.com.bora.delivery.domain.AccountRole;
import br.com.bora.delivery.domain.OrderMessage;
import br.com.bora.delivery.domain.OrderReview;
import br.com.bora.delivery.domain.OrderStatus;
import br.com.bora.delivery.repository.OrderMessageRepository;
import br.com.bora.delivery.repository.OrderRepository;
import br.com.bora.delivery.repository.OrderReviewRepository;
import br.com.bora.delivery.security.AppPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommunicationService {
    private final OrderRepository orderRepository;
    private final OrderMessageRepository messageRepository;
    private final OrderReviewRepository reviewRepository;

    public CommunicationService(OrderRepository orderRepository, OrderMessageRepository messageRepository,
                                OrderReviewRepository reviewRepository) {
        this.orderRepository = orderRepository;
        this.messageRepository = messageRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<MessageDto> listMessages(Long orderId, AppPrincipal principal) {
        requireOrderAccess(orderId, principal);
        return messageRepository.findByOrderIdOrderBySentAtAsc(orderId).stream().map(MessageDto::from).toList();
    }

    @Transactional
    public MessageDto sendMessage(Long orderId, String text, AppPrincipal principal) {
        var order = requireOrderAccess(orderId, principal);
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("A conversa foi encerrada porque o pedido foi concluído.");
        }
        String message = text.trim();
        if (message.isEmpty()) throw new IllegalArgumentException("A mensagem não pode estar vazia.");
        if (messageRepository.countByOrderId(orderId) >= 500) {
            throw new IllegalArgumentException("Esta conversa atingiu o limite demonstrativo de mensagens.");
        }
        return MessageDto.from(messageRepository.save(new OrderMessage(
                orderId, principal.id(), principal.name(), principal.role(), message)));
    }

    @Transactional
    public ReviewDto createReview(CreateReviewRequest request, AppPrincipal principal) {
        var order = orderRepository.findDetailedById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (order.getCustomerAccountId() == null || !order.getCustomerAccountId().equals(principal.id())) {
            throw new IllegalArgumentException("Somente quem fez o pedido pode avaliá-lo.");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Você poderá avaliar o pedido depois da entrega.");
        }
        if (reviewRepository.findByOrderId(request.orderId()).isPresent()) {
            throw new IllegalArgumentException("Este pedido já foi avaliado.");
        }
        OrderReview review = new OrderReview(request.orderId(), principal.id(), order.getRestaurant().getId(),
                request.rating(), request.comment() == null ? "" : request.comment().trim());
        return ReviewDto.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public ReviewDto findReview(Long orderId, AppPrincipal principal) {
        var order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (principal.role() == AccountRole.CUSTOMER && !principal.id().equals(order.getCustomerAccountId())) {
            throw new IllegalArgumentException("Este pedido pertence a outra conta.");
        }
        return reviewRepository.findByOrderId(orderId).map(ReviewDto::from).orElse(null);
    }

    private br.com.bora.delivery.domain.CustomerOrder requireOrderAccess(Long orderId, AppPrincipal principal) {
        var order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        boolean customer = principal.role() == AccountRole.CUSTOMER
                && principal.id().equals(order.getCustomerAccountId());
        boolean store = principal.role() == AccountRole.RESTAURANT_OWNER || principal.role() == AccountRole.ADMIN;
        if (!customer && !store) throw new IllegalArgumentException("Você não pode acessar a conversa deste pedido.");
        return order;
    }
}
