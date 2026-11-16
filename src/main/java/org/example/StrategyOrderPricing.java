package org.example;

final class StrategyOrderPricing implements OrderPricing {
    private final PricingStrategy discountStrategy;
    private final PricingStrategy commissionStrategy;

    StrategyOrderPricing(PricingStrategy discountStrategy, PricingStrategy commissionStrategy) {
        this.discountStrategy = discountStrategy;
        this.commissionStrategy = commissionStrategy;
    }

    @Override
    public PricingResult calculate(Order order) {
        double discount = discountStrategy.calculate(order);
        double commission = commissionStrategy.calculate(order);
        double finalAmount = order.getAmount() - discount + commission;
        return new PricingResult(discount, commission, finalAmount);
    }
}
