package br.com.bora.delivery.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class CustomizationCatalog {
    private static final Map<String, List<Option>> OPTIONS = Map.of(
            "Lanches", List.of(new Option("Bacon crocante", "4.00"), new Option("Queijo extra", "3.50"), new Option("Ovo", "2.50")),
            "Pizzas", List.of(new Option("Borda recheada", "9.00"), new Option("Muçarela extra", "6.00"), new Option("Azeitona extra", "2.00")),
            "Pratos", List.of(new Option("Porção extra de arroz", "5.00"), new Option("Farofa da casa", "4.00"), new Option("Salada extra", "6.00")),
            "Combinados", List.of(new Option("Gengibre extra", "2.00"), new Option("Wasabi extra", "2.00"), new Option("Molho tarê", "3.00")),
            "Bebidas", List.of(new Option("Sem gelo", "0.00"), new Option("Gelo extra", "0.00")),
            "Sobremesas", List.of(new Option("Calda extra", "3.00"), new Option("Acompanhamento de sorvete", "7.00")),
            "Acompanhamentos", List.of(new Option("Molho da casa", "3.00"), new Option("Queijo extra", "3.50")),
            "Padaria", List.of(new Option("Requeijão cremoso", "3.00"), new Option("Manteiga extra", "1.50")),
            "Cafés", List.of(new Option("Dose extra de espresso", "4.00"), new Option("Leite vegetal", "3.50"))
    );

    private CustomizationCatalog() {
    }

    public static List<Option> optionsFor(String category) {
        return OPTIONS.getOrDefault(category, List.of(
                new Option("Molho da casa", "3.00"),
                new Option("Porção extra", "5.00")));
    }

    public static BigDecimal priceFor(String category, String optionName) {
        return optionsFor(category).stream()
                .filter(option -> option.name().equals(optionName))
                .map(Option::price)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Personalização inválida: " + optionName));
    }

    public record Option(String name, BigDecimal price) {
        public Option(String name, String price) {
            this(name, new BigDecimal(price));
        }
    }
}
