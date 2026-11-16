package org.example;

public abstract class OrderPricingFactory {
    public final OrderPricing create() {
        return new StrategyOrderPricing(createDiscountStrategy(), createCommissionStrategy());
    }

    protected abstract PricingStrategy createDiscountStrategy();

    protected abstract PricingStrategy createCommissionStrategy();
}
