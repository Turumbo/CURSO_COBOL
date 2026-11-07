package br.com.bora.delivery.service;

import br.com.bora.delivery.api.dto.CreateOrderRequest;
import br.com.bora.delivery.api.dto.OrderDto;
import br.com.bora.delivery.domain.CustomerOrder;
import br.com.bora.delivery.domain.MenuItem;
import br.com.bora.delivery.domain.OrderLine;
import br.com.bora.delivery.domain.OrderStatus;
import br.com.bora.delivery.domain.Restaurant;
import br.com.bora.delivery.repository.MenuItemRepository;
import br.com.bora.delivery.repository.OrderRepository;
import br.com.bora.delivery.repository.RestaurantRepository;
import br.com.bora.delivery.domain.AccountRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private static final BigDecimal MAX_PROMO_DISCOUNT = new BigDecimal("10.00");
    private static final BigDecimal PROMO_RATE = new BigDecimal("0.10");

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final MockPaymentService paymentService;

    public OrderService(OrderRepository orderRepository, RestaurantRepository restaurantRepository,
                        MenuItemRepository menuItemRepository, MockPaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public OrderDto create(CreateOrderRequest request, Long accountId, String accountName) {
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado."));
        BigDecimal subtotal = BigDecimal.ZERO;
        CustomerOrder order = new CustomerOrder(
                accountId,
                accountName,
                request.deliveryAddress().trim(),
                request.latitude(),
                request.longitude(),
                request.paymentMethod(),
                restaurant,
                BigDecimal.ZERO,
                restaurant.getDeliveryFee(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                normalizePromo(request.promoCode()));

        for (CreateOrderRequest.OrderItemRequest requestedItem : request.items()) {
            MenuItem menuItem = menuItemRepository.findByIdForUpdate(requestedItem.menuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Um item do pedido não existe mais no cardápio."));
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException("Todos os itens do pedido devem pertencer ao mesmo restaurante.");
            }
            List<String> selectedOptions = requestedItem.customizations() == null
                    ? List.of() : requestedItem.customizations().stream().distinct().toList();
            menuItem.reserveStock(requestedItem.quantity());
            BigDecimal optionPrice = selectedOptions.stream()
                    .map(option -> CustomizationCatalog.priceFor(menuItem.getCategory(), option))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal unitPrice = menuItem.getPrice().add(optionPrice);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(requestedItem.quantity()));
            subtotal = subtotal.add(lineTotal);
            order.addItem(new OrderLine(menuItem.getId(), menuItem.getName(), unitPrice, requestedItem.quantity(),
                    String.join(", ", selectedOptions)));
        }

        if (subtotal.compareTo(restaurant.getMinimumOrder()) < 0) {
            throw new IllegalArgumentException("O pedido não atingiu o valor mínimo do restaurante.");
        }
        BigDecimal discount = calculateDiscount(order.getPromoCode(), subtotal);
        BigDecimal total = subtotal.add(restaurant.getDeliveryFee()).subtract(discount).max(BigDecimal.ZERO);
        setOrderTotals(order, subtotal, discount, total);
        CustomerOrder savedOrder = orderRepository.save(order);
        paymentService.recordSimulation(savedOrder);
        return OrderDto.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id, Long accountId, AccountRole role) {
        CustomerOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (role == AccountRole.CUSTOMER && !order.getCustomerAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Este pedido pertence a outra conta.");
        }
        return OrderDto.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> listAll(AccountRole role, Long accountId) {
        List<CustomerOrder> orders = role == AccountRole.CUSTOMER
                ? orderRepository.findAllByCustomerAccountIdOrderByCreatedAtDesc(accountId)
                : orderRepository.findAllByOrderByCreatedAtDesc();
        return orders
                .stream().map(OrderDto::from).toList();
    }

    @Transactional
    public OrderDto updateStatus(Long id, OrderStatus nextStatus) {
        CustomerOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
        if (order.getStatus() == nextStatus) return OrderDto.from(order);
        if (!allowedTransitions().get(order.getStatus()).contains(nextStatus)) {
            throw new IllegalArgumentException("Não é possível alterar " + order.getStatus() + " para " + nextStatus + ".");
        }
        order.setStatus(nextStatus);
        if (nextStatus == OrderStatus.CANCELLED) {
            for (OrderLine line : order.getItems()) {
                MenuItem item = menuItemRepository.findByIdForUpdate(line.getMenuItemId())
                        .orElseThrow(() -> new IllegalStateException("Item do cardápio do pedido não encontrado."));
                item.restoreStock(line.getQuantity());
            }
        }
        return OrderDto.from(orderRepository.save(order));
    }

    private static String normalizePromo(String promoCode) {
        return promoCode == null ? "" : promoCode.trim().toUpperCase();
    }

    private static BigDecimal calculateDiscount(String promoCode, BigDecimal subtotal) {
        return switch (promoCode) {
            case "BEMVINDO10" -> subtotal.multiply(PROMO_RATE).setScale(2, RoundingMode.HALF_UP).min(MAX_PROMO_DISCOUNT);
            case "BORAALMOCO" -> new BigDecimal("5.00").min(subtotal);
            case "SURPRESA15" -> subtotal.multiply(new BigDecimal("0.15"))
                    .setScale(2, RoundingMode.HALF_UP).min(new BigDecimal("25.00"));
            case "" -> BigDecimal.ZERO;
            default -> throw new IllegalArgumentException("Cupom expirado ou inválido.");
        };
    }

    private static void setOrderTotals(CustomerOrder order, BigDecimal subtotal, BigDecimal discount, BigDecimal total) {
        order.applyTotals(subtotal, discount, total);
    }

    private static Map<OrderStatus, List<OrderStatus>> allowedTransitions() {
        Map<OrderStatus, List<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(OrderStatus.RECEIVED, List.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PREPARING, List.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.READY_FOR_PICKUP, List.of(OrderStatus.ON_THE_WAY, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.ON_THE_WAY, List.of(OrderStatus.DELIVERED));
        transitions.put(OrderStatus.DELIVERED, List.of());
        transitions.put(OrderStatus.CANCELLED, List.of());
        return transitions;
    }
}
