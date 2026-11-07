package br.com.bora.delivery;

import br.com.bora.delivery.api.dto.AuthDtos.LoginRequest;
import br.com.bora.delivery.api.dto.AuthDtos.SessionDto;
import br.com.bora.delivery.api.dto.AuthDtos.AccountDto;
import br.com.bora.delivery.api.dto.CreateOrderRequest;
import br.com.bora.delivery.api.dto.OrderDto;
import br.com.bora.delivery.api.dto.PageDto;
import br.com.bora.delivery.api.dto.RestaurantDto;
import br.com.bora.delivery.api.dto.AuthDtos.UpdateProfileRequest;
import br.com.bora.delivery.api.dto.InventoryDtos.InventoryItemDto;
import br.com.bora.delivery.api.dto.InventoryDtos.UpdateStockRequest;
import br.com.bora.delivery.api.dto.PaymentDtos.FiscalDocumentDto;
import br.com.bora.delivery.api.dto.PaymentDtos.PaymentTransactionDto;
import br.com.bora.delivery.api.dto.PaymentDtos.DemoCardDto;
import br.com.bora.delivery.api.dto.PaymentDtos.AddDemoCardRequest;
import br.com.bora.delivery.api.dto.UpdateOrderStatusRequest;
import br.com.bora.delivery.domain.OrderStatus;
import br.com.bora.delivery.domain.PaymentMethod;
import br.com.bora.delivery.service.RestaurantService.CatalogSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DeliveryApiIntegrationTest {
    private static final ParameterizedTypeReference<PageDto<RestaurantDto>> RESTAURANT_PAGE =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PageDto<RestaurantDto.MenuItemDto>> MENU_PAGE =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PageDto<InventoryItemDto>> INVENTORY_PAGE =
            new ParameterizedTypeReference<>() { };

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void seedsFourHundredRestaurantsAndTenThousandMenuItemsWithPagination() {
        CatalogSummary summary = restTemplate.getForObject(url("/api/catalog/summary"), CatalogSummary.class);
        ResponseEntity<PageDto<RestaurantDto>> restaurants = restTemplate.exchange(
                url("/api/restaurants?page=0&size=10"), HttpMethod.GET, null, RESTAURANT_PAGE);

        assertThat(summary.restaurants()).isEqualTo(400);
        assertThat(summary.menuItems()).isEqualTo(10_000);
        assertThat(restaurants.getBody().items()).hasSize(10);
        assertThat(restaurants.getBody().totalItems()).isEqualTo(400);
        assertThat(restaurants.getBody().items().get(0).menuItems()).isEmpty();

        Long restaurantId = restaurants.getBody().items().get(0).id();
        ResponseEntity<PageDto<RestaurantDto.MenuItemDto>> menu = restTemplate.exchange(
                url("/api/restaurants/" + restaurantId + "/menu?page=0&size=25"),
                HttpMethod.GET, null, MENU_PAGE);
        assertThat(menu.getBody().items()).hasSize(25);
        assertThat(menu.getBody().items().get(0).customizationOptions()).isNotEmpty();
    }

    @Test
    void authenticatesCustomerAndCreatesServerPricedCustomizedOrder() {
        SessionDto customer = login("cliente@bora.demo", "cliente123");
        RestaurantDto restaurant = firstRestaurant();
        RestaurantDto.MenuItemDto item = firstMenuItem(restaurant.id());
        String option = item.customizationOptions().get(0).name();
        var request = new CreateOrderRequest(
                restaurant.id(), "Pessoa Teste", "Rua de Teste, 123", -23.55, -46.63,
                PaymentMethod.PIX, "BEMVINDO10",
                List.of(new CreateOrderRequest.OrderItemRequest(item.id(), 20, List.of(option))));
        ResponseEntity<OrderDto> orderResponse = restTemplate.exchange(
                url("/api/orders"),
                HttpMethod.POST,
                new HttpEntity<>(request, bearer(customer.token())),
                OrderDto.class);

        assertThat(orderResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orderResponse.getBody()).isNotNull();
        assertThat(orderResponse.getBody().status()).isEqualTo(OrderStatus.RECEIVED);
        assertThat(orderResponse.getBody().items()).hasSize(1);
        assertThat(orderResponse.getBody().items().get(0).customizations()).contains(option);
        assertThat(orderResponse.getBody().discount()).isPositive();
        assertThat(orderResponse.getBody().total()).isPositive();

        PaymentTransactionDto payment = restTemplate.exchange(
                url("/api/orders/" + orderResponse.getBody().id() + "/payment"),
                HttpMethod.GET, new HttpEntity<>(bearer(customer.token())), PaymentTransactionDto.class).getBody();
        assertThat(payment.method()).isEqualTo(PaymentMethod.PIX.name());
        assertThat(payment.status()).isEqualTo("SIMULATED");
        assertThat(payment.providerReference()).startsWith("MOCK-");

        FiscalDocumentDto document = restTemplate.exchange(
                url("/api/orders/" + orderResponse.getBody().id() + "/fiscal-document"),
                HttpMethod.POST, new HttpEntity<>(bearer(customer.token())), FiscalDocumentDto.class).getBody();
        assertThat(document.reference()).startsWith("BORA-DEMO-");
        assertThat(document.legallyValid()).isFalse();
        assertThat(document.notice()).contains("sem validade fiscal");
        FiscalDocumentDto repeatedDocument = restTemplate.exchange(
                url("/api/orders/" + orderResponse.getBody().id() + "/fiscal-document"),
                HttpMethod.POST, new HttpEntity<>(bearer(customer.token())), FiscalDocumentDto.class).getBody();
        assertThat(repeatedDocument.reference()).isEqualTo(document.reference());
    }

    @Test
    void recordsMockTransactionsForNamedWalletMethodsWithoutRealCharges() {
        SessionDto customer = login("cliente@bora.demo", "cliente123");
        RestaurantDto restaurant = firstRestaurant();
        RestaurantDto.MenuItemDto item = firstMenuItem(restaurant.id());

        for (PaymentMethod method : List.of(PaymentMethod.BB_PAY, PaymentMethod.NUPAY, PaymentMethod.ALELO)) {
            var request = new CreateOrderRequest(
                    restaurant.id(), "Pessoa Teste", "Rua de Teste, 123", -23.55, -46.63,
                    method, "", List.of(new CreateOrderRequest.OrderItemRequest(item.id(), 2)));
            OrderDto order = restTemplate.postForObject(
                    url("/api/orders"), new HttpEntity<>(request, bearer(customer.token())), OrderDto.class);
            PaymentTransactionDto transaction = restTemplate.exchange(
                    url("/api/orders/" + order.id() + "/payment"),
                    HttpMethod.GET, new HttpEntity<>(bearer(customer.token())), PaymentTransactionDto.class).getBody();

            assertThat(transaction.method()).isEqualTo(method.name());
            assertThat(transaction.status()).isEqualTo("SIMULATED");
            assertThat(transaction.notice()).contains("nenhuma cobrança");
        }
    }

    @Test
    void restaurantCanManageStockAndOrdersRespectAvailability() {
        SessionDto owner = login("restaurante@bora.demo", "restaurante123");
        ResponseEntity<PageDto<InventoryItemDto>> inventoryPage = restTemplate.exchange(
                url("/api/admin/inventory?page=0&size=100"), HttpMethod.GET,
                new HttpEntity<>(bearer(owner.token())), INVENTORY_PAGE);
        assertThat(inventoryPage.getBody().items()).isNotEmpty();
        RestaurantDto restaurant = firstRestaurant();
        RestaurantDto.MenuItemDto item = firstMenuItem(restaurant.id());
        int originalQuantity = 100;
        restTemplate.exchange(url("/api/admin/inventory/" + item.id()), HttpMethod.PATCH,
                new HttpEntity<>(new UpdateStockRequest(originalQuantity), bearer(owner.token())), InventoryItemDto.class);

        ResponseEntity<InventoryItemDto> depleted = restTemplate.exchange(
                url("/api/admin/inventory/" + item.id()), HttpMethod.PATCH,
                new HttpEntity<>(new UpdateStockRequest(0), bearer(owner.token())), InventoryItemDto.class);
        assertThat(depleted.getBody().stockQuantity()).isZero();
        RestaurantDto.MenuItemDto unavailable = firstMenuItem(restaurant.id());
        assertThat(unavailable.available()).isFalse();

        SessionDto customer = login("cliente@bora.demo", "cliente123");
        var request = new CreateOrderRequest(
                restaurant.id(), "Pessoa Teste", "Rua de Teste, 123", -23.55, -46.63,
                PaymentMethod.PIX, "", List.of(new CreateOrderRequest.OrderItemRequest(item.id(), 20)));
        ResponseEntity<String> rejectedOrder = restTemplate.exchange(
                url("/api/orders"), HttpMethod.POST,
                new HttpEntity<>(request, bearer(customer.token())), String.class);
        assertThat(rejectedOrder.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rejectedOrder.getBody()).contains("Estoque insuficiente");

        restTemplate.exchange(url("/api/admin/inventory/" + item.id()), HttpMethod.PATCH,
                new HttpEntity<>(new UpdateStockRequest(originalQuantity), bearer(owner.token())), InventoryItemDto.class);
        OrderDto order = restTemplate.postForObject(
                url("/api/orders"), new HttpEntity<>(request, bearer(customer.token())), OrderDto.class);
        ResponseEntity<PageDto<InventoryItemDto>> reservedStock = restTemplate.exchange(
                url("/api/admin/inventory?page=0&size=100"), HttpMethod.GET,
                new HttpEntity<>(bearer(owner.token())), INVENTORY_PAGE);
        InventoryItemDto reserved = reservedStock.getBody().items().stream()
                .filter(entry -> entry.menuItemId().equals(item.id())).findFirst().orElseThrow();
        assertThat(reserved.stockQuantity()).isEqualTo(originalQuantity - 20);
        restTemplate.exchange(url("/api/admin/orders/" + order.id() + "/status"), HttpMethod.PATCH,
                new HttpEntity<>(new UpdateOrderStatusRequest(OrderStatus.CANCELLED), bearer(owner.token())),
                OrderDto.class);
        ResponseEntity<PageDto<InventoryItemDto>> restoredStock = restTemplate.exchange(
                url("/api/admin/inventory?page=0&size=100"), HttpMethod.GET,
                new HttpEntity<>(bearer(owner.token())), INVENTORY_PAGE);
        InventoryItemDto restored = restoredStock.getBody().items().stream()
                .filter(entry -> entry.menuItemId().equals(item.id())).findFirst().orElseThrow();
        assertThat(restored.stockQuantity()).isEqualTo(originalQuantity);
    }

    @Test
    void protectsOrderCreationAndAdminOperations() {
        RestaurantDto restaurant = firstRestaurant();
        RestaurantDto.MenuItemDto item = firstMenuItem(restaurant.id());
        var request = new CreateOrderRequest(
                restaurant.id(), "Pessoa Teste", "Rua de Teste, 123", -23.55, -46.63,
                PaymentMethod.PIX, "",
                List.of(new CreateOrderRequest.OrderItemRequest(item.id(), 20)));

        ResponseEntity<String> anonymousOrder = restTemplate.postForEntity(url("/api/orders"), request, String.class);
        assertThat(anonymousOrder.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        ResponseEntity<String> anonymousProfile = restTemplate.exchange(
                url("/api/auth/me"), HttpMethod.PUT,
                new HttpEntity<>(new UpdateProfileRequest("Intruso", "intruso@example.com", null, null)),
                String.class);
        assertThat(anonymousProfile.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        SessionDto customer = login("cliente@bora.demo", "cliente123");
        OrderDto order = restTemplate.postForObject(
                url("/api/orders"),
                new HttpEntity<>(request, bearer(customer.token())),
                OrderDto.class);
        SessionDto owner = login("restaurante@bora.demo", "restaurante123");
        var update = new HttpEntity<>(new UpdateOrderStatusRequest(OrderStatus.DELIVERED), bearer(owner.token()));

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/admin/orders/" + order.id() + "/status"),
                HttpMethod.PATCH,
                update,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Não é possível alterar");
    }

    @Test
    void customerCanManageOwnProfileWithoutChangingAccountRole() {
        SessionDto customer = login("cliente@bora.demo", "cliente123");
        var update = new HttpEntity<>(
                new UpdateProfileRequest("Cliente Atualizado", "cliente@bora.demo",
                        "(11) 99999-1234", "Rua das Flores, 42, São Paulo"),
                bearer(customer.token()));

        ResponseEntity<AccountDto> response =
                restTemplate.exchange(url("/api/auth/me"), HttpMethod.PUT, update,
                        AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Cliente Atualizado");
        assertThat(response.getBody().phone()).isEqualTo("(11) 99999-1234");
        assertThat(response.getBody().deliveryAddress()).isEqualTo("Rua das Flores, 42, São Paulo");
        assertThat(response.getBody().role()).isEqualTo(br.com.bora.delivery.domain.AccountRole.CUSTOMER);
        ResponseEntity<AccountDto> refreshed = restTemplate.exchange(
                url("/api/auth/me"), HttpMethod.GET,
                new HttpEntity<>(bearer(customer.token())), AccountDto.class);
        assertThat(refreshed.getBody().email()).isEqualTo("cliente@bora.demo");

        ResponseEntity<String> duplicateEmail = restTemplate.exchange(
                url("/api/auth/me"), HttpMethod.PUT,
                new HttpEntity<>(new UpdateProfileRequest("Cliente Atualizado", "admin@bora.demo", null, null),
                        bearer(customer.token())),
                String.class);
        assertThat(duplicateEmail.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void customerCanManageOnlyDemonstrationCardMetadata() {
        SessionDto customer = login("cliente@bora.demo", "cliente123");
        var cardRequest = new HttpEntity<>(
                new AddDemoCardRequest("VISA", "4242"),
                bearer(customer.token()));
        ResponseEntity<DemoCardDto> created = restTemplate.exchange(
                url("/api/auth/me/payment-methods"), HttpMethod.POST, cardRequest, DemoCardDto.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().brand()).isEqualTo("VISA");
        assertThat(created.getBody().lastFour()).isEqualTo("4242");
        assertThat(created.getBody().reference()).startsWith("MOCK-CARD-");
        assertThat(created.getBody().notice()).contains("não pode ser usado para cobrança");

        ResponseEntity<List<DemoCardDto>> listed = restTemplate.exchange(
                url("/api/auth/me/payment-methods"), HttpMethod.GET,
                new HttpEntity<>(bearer(customer.token())),
                new ParameterizedTypeReference<>() { });
        assertThat(listed.getBody()).hasSize(1);

        SessionDto owner = login("restaurante@bora.demo", "restaurante123");
        ResponseEntity<String> forbidden = restTemplate.exchange(
                url("/api/auth/me/payment-methods"), HttpMethod.POST,
                new HttpEntity<>(new AddDemoCardRequest("VISA", "4242"),
                        bearer(owner.token())),
                String.class);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/api/auth/me/payment-methods/" + created.getBody().id()), HttpMethod.DELETE,
                new HttpEntity<>(bearer(customer.token())), Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private RestaurantDto firstRestaurant() {
        ResponseEntity<PageDto<RestaurantDto>> response = restTemplate.exchange(
                url("/api/restaurants?page=0&size=1"), HttpMethod.GET, null, RESTAURANT_PAGE);
        return response.getBody().items().get(0);
    }

    private RestaurantDto.MenuItemDto firstMenuItem(Long restaurantId) {
        ResponseEntity<PageDto<RestaurantDto.MenuItemDto>> response = restTemplate.exchange(
                url("/api/restaurants/" + restaurantId + "/menu?page=0&size=1"),
                HttpMethod.GET, null, MENU_PAGE);
        return response.getBody().items().get(0);
    }

    private SessionDto login(String email, String password) {
        return restTemplate.postForObject(
                url("/api/auth/login"), new LoginRequest(email, password), SessionDto.class);
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
