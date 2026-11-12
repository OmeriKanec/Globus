package org.example;

import java.util.List;
import java.util.Objects;

public class OrderService {
    private final OrderPricing pricing;
    private final ReceiptPrinter receiptPrinter;
    private final OrderRepository repository;

    public OrderService() {
        this(new StandardOrderPricingFactory().create(),
                new ConsoleReceiptPrinter(), new InMemoryOrderRepository());
    }

    public OrderService(OrderPricing pricing, ReceiptPrinter receiptPrinter, OrderRepository repository) {
        this.pricing = Objects.requireNonNull(pricing, "pricing");
        this.receiptPrinter = Objects.requireNonNull(receiptPrinter, "receiptPrinter");
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public double processOrder(Order order) {
        OrderValidator.validate(order);
        PricingResult result = pricing.calculate(order);
        receiptPrinter.print(order, result);
        repository.save(order);
        return result.finalAmount();
    }

    public static void main(String[] args) {
        OrderService service = new OrderService();

        Order order1 = new Order("1", "VIP", "CARD", List.of("Laptop", "Mouse"), 1000.0);
        Order order2 = new Order("2", "REGULAR", "CASH", List.of("Book"), 20.0);

        System.out.println("Final amount 1: " + service.processOrder(order1));
        System.out.println("Final amount 2: " + service.processOrder(order2));
    }
}

