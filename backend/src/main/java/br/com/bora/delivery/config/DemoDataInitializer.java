package br.com.bora.delivery.config;

import br.com.bora.delivery.domain.Account;
import br.com.bora.delivery.domain.AccountRole;
import br.com.bora.delivery.repository.AccountRepository;
import br.com.bora.delivery.repository.RestaurantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
public class DemoDataInitializer {
    private static final String[] CATEGORIES = {
            "Brasileira", "Hambúrguer", "Pizza", "Japonesa",
            "Cafeteria", "Saudável", "Árabe", "Mexicana"
    };
    private static final int[] PRICE_BASE_CENTS = {2100, 1800, 2400, 1900, 500, 1600, 1000, 1400};
    private static final int[] PRICE_SPREAD_CENTS = {3800, 4200, 5000, 5200, 2300, 4200, 3500, 3900};
    private static final String[] CATEGORY_NAMES = {
            "Pratos", "Lanches", "Pizzas", "Combinados",
            "Cafés", "Bowls", "Esfihas", "Tacos"
    };
    private static final String[][] DISHES = {
            {"Feijoada completa", "Bife acebolado", "Frango grelhado com arroz", "Escondidinho de carne-seca",
                    "Moqueca de peixe", "Virado à paulista", "Strogonoff de frango", "Costela assada"},
            {"Smash burger clássico", "Cheeseburger artesanal", "Burger de frango crocante", "Burger barbecue",
                    "Burger vegetariano de grão-de-bico", "Duplo bacon cheddar", "Burger de costela", "Hot dog completo"},
            {"Margherita", "Calabresa com cebola", "Quatro queijos", "Portuguesa", "Frango com catupiry",
                    "Pepperoni", "Abobrinha com ricota", "Chocolate com morango"},
            {"Combinado de sushi", "Sashimi de salmão", "Temaki de salmão", "Hot roll", "Yakisoba de legumes",
                    "Uramaki de atum", "Gyoza de carne suína", "Poke de salmão"},
            {"Cappuccino", "Latte", "Mocha", "Pão de queijo", "Croissant na manteiga",
                    "Bolo de cenoura com brigadeiro", "Toast de abacate", "Brownie de chocolate"},
            {"Bowl de frango e quinoa", "Salada caesar", "Bowl de salmão", "Wrap de falafel",
                    "Salada mediterrânea", "Bowl de açaí com frutas", "Omelete de espinafre", "Sopa de abóbora"},
            {"Esfiha de carne", "Esfiha de queijo", "Kibe assado", "Kafta no prato", "Homus com pão sírio",
                    "Falafel com tahine", "Shawarma de frango", "Babaganuche"},
            {"Taco de carne", "Burrito de frango", "Quesadilla de queijo", "Nachos com guacamole",
                    "Burrito vegetariano", "Taco de peixe", "Chili com carne", "Enchilada de frango"}
    };
    private static final String[][] DISH_MODIFIERS = {
            {"", " com arroz, feijão e farofa", " com ovo caipira", " em porção família"},
            {"", " com fritas", " com cheddar extra", " em combo com bebida"},
            {"", " brotinho", " grande", " meio a meio"},
            {"", " · 6 peças", " · 12 peças", " com missoshiru"},
            {"", " com leite vegetal", " duplo", " com pão de queijo"},
            {"", " com proteína extra", " com molho à parte", " em tamanho família"},
            {"", " com homus", " em combo com kafta", " para compartilhar"},
            {"", " com guacamole", " em combo com nachos", " para compartilhar"}
    };
    private static final String[] IMAGES = {
            "photo-1547592180-85f173990554", "photo-1568901346375-23c9450c58cd",
            "photo-1628840042765-356cda07504e", "photo-1579871494447-9811cf80d66c",
            "photo-1495474472287-4d71bcdd2085", "photo-1512621776951-a57141f2eefd",
            "photo-1540189549336-e6e99c3679fe", "photo-1555939594-58d7cb561ad1",
            "photo-1565299624946-b28f40a0ae38", "photo-1565958011703-44f9829ba187",
            "photo-1546069901-ba9599a7e63c", "photo-1547592180-85f173990554",
            "photo-1550547660-d9450f859349", "photo-1573080496219-bb080dd4f877",
            "photo-1590301157890-4810ed352733", "photo-1608039829572-78524f79c4c7",
            "photo-1525351484163-7529414344d8", "photo-1600891964599-f61ba0e24092",
            "photo-1547592180-85f173990554", "photo-1509440159596-0249088772ff"
    };

    @Bean
    CommandLineRunner seedDemoData(
            JdbcTemplate jdbcTemplate,
            RestaurantRepository restaurantRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            seedAccounts(accountRepository, passwordEncoder);
            if (restaurantRepository.count() > 0) return;

            List<Object[]> restaurants = new ArrayList<>(400);
            for (int index = 0; index < 400; index++) {
                int category = index % CATEGORIES.length;
                restaurants.add(new Object[]{
                        index + 1,
                        String.format(Locale.ROOT, "Cozinha de demonstração %03d", index + 1),
                        CATEGORIES[category],
                        cuisine(category),
                        "Receitas preparadas na hora com ingredientes selecionados e um toque especial da casa.",
                        BigDecimal.valueOf(42 + ((index * 17) % 9), 1),
                        18 + (index * 13) % 37,
                        new BigDecimal("3.90").add(new BigDecimal("0.50").multiply(BigDecimal.valueOf(index % 11))),
                        new BigDecimal("15.00").add(new BigDecimal("5.00").multiply(BigDecimal.valueOf(index % 8))),
                        imageUrl(index, 900, 560),
                        index % 4 != 3,
                        -23.5614 + (index % 20 - 10) * 0.0032,
                        -46.6560 + (index / 20 - 10) * 0.0036
                });
            }
            jdbcTemplate.batchUpdate(
                    "INSERT INTO restaurants (id, name, category, cuisine, description, rating, delivery_minutes, delivery_fee, minimum_order, image_url, featured, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    restaurants);

            List<Object[]> menuItems = new ArrayList<>(10_000);
            for (int index = 0; index < 10_000; index++) {
                int restaurantIndex = index / 25;
                int dishIndex = index % 25;
                int categoryIndex = restaurantIndex % CATEGORIES.length;
                String category = CATEGORY_NAMES[categoryIndex];
                String name = DISHES[categoryIndex][dishIndex % DISHES[categoryIndex].length]
                        + DISH_MODIFIERS[categoryIndex][dishIndex / DISHES[categoryIndex].length];
                int priceCents = PRICE_BASE_CENTS[categoryIndex]
                        + Math.floorMod(dishIndex * 191 + restaurantIndex * 37, PRICE_SPREAD_CENTS[categoryIndex]);
                BigDecimal price = BigDecimal.valueOf(priceCents, 2);
                menuItems.add(new Object[]{
                        index + 1,
                        name,
                        descriptionFor(categoryIndex),
                        category,
                        price,
                        imageUrl(index + 3, 480, 360),
                        100 + Math.floorMod(index * 17, 20),
                        restaurantIndex + 1
                });
            }
            jdbcTemplate.batchUpdate(
                    "INSERT INTO menu_items (id, name, description, category, price, image_url, stock_quantity, restaurant_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    menuItems);
        };
    }

    private static void seedAccounts(AccountRepository accounts, PasswordEncoder encoder) {
        if (!accounts.existsByEmailIgnoreCase("cliente@bora.demo")) {
            accounts.save(new Account("Cliente Bora", "cliente@bora.demo",
                    encoder.encode("cliente123"), AccountRole.CUSTOMER));
        }
        if (!accounts.existsByEmailIgnoreCase("restaurante@bora.demo")) {
            accounts.save(new Account("Operação Bora", "restaurante@bora.demo",
                    encoder.encode("restaurante123"), AccountRole.RESTAURANT_OWNER));
        }
        if (!accounts.existsByEmailIgnoreCase("admin@bora.demo")) {
            accounts.save(new Account("Admin Bora", "admin@bora.demo",
                    encoder.encode("admin123456"), AccountRole.ADMIN));
        }
    }

    private static String cuisine(int index) {
        return switch (index) {
            case 0 -> "Comida brasileira";
            case 1 -> "Hambúrguer artesanal";
            case 2 -> "Pizzaria artesanal";
            case 3 -> "Culinária japonesa";
            case 4 -> "Café e brunch";
            case 5 -> "Saudável e natural";
            case 6 -> "Comida árabe";
            default -> "Comida mexicana";
        };
    }

    private static String descriptionFor(int category) {
        String[] descriptions = {
                "Comida brasileira feita na hora, com arroz, feijão e acompanhamentos preparados diariamente.",
                "Pão tostado, carne selecionada e ingredientes frescos, montado após o pedido.",
                "Massa aberta e assada na hora, molho de tomate e cobertura generosa.",
                "Preparado com arroz japonês temperado, peixe e vegetais frescos.",
                "Café espresso preparado na hora e ingredientes selecionados para acompanhar.",
                "Ingredientes frescos e uma combinação equilibrada, com molhos servidos à parte.",
                "Receita de inspiração levantina com temperos aromáticos e preparo artesanal.",
                "Tortilha macia, recheio temperado e acompanhamentos frescos."
        };
        return descriptions[category];
    }

    private static String imageUrl(int index, int width, int height) {
        String image = IMAGES[Math.floorMod(index, IMAGES.length)];
        return String.format(Locale.ROOT,
                "https://images.unsplash.com/%s?w=%d&h=%d&fit=crop", image, width, height);
    }
}
