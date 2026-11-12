package org.example;

@FunctionalInterface
public interface PricingStrategy {
    double calculate(Order order);
}
