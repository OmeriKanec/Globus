package org.example;

@FunctionalInterface
public interface OrderPricing {
    PricingResult calculate(Order order);
}
