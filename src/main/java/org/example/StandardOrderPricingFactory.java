package org.example;

import java.util.Map;

public class StandardOrderPricingFactory extends OrderPricingFactory {
    @Override
    protected PricingStrategy createDiscountStrategy() {
        Map<String, PricingStrategy> discounts = Map.of(
                "REGULAR", order -> 0.0,
                "VIP", order -> order.getAmount() * 0.05,
                "PREMIUM", order -> order.getAmount() * 0.10
        );
        return order -> strategyFor(discounts, order.getCustomerType(), "customer type").calculate(order);
    }

    @Override
    protected PricingStrategy createCommissionStrategy() {
        Map<String, PricingStrategy> commissions = Map.of(
                "CARD", order -> order.getAmount() * 0.02,
                "CASH", order -> 0.0,
                "TRANSFER", order -> order.getAmount() * 0.01
        );
        return order -> strategyFor(commissions, order.getPaymentType(), "payment type").calculate(order);
    }

    private static PricingStrategy strategyFor(Map<String, PricingStrategy> strategies, String type, String description) {
        PricingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new RuntimeException("Unknown " + description + ": " + type);
        }
        return strategy;
    }
}
