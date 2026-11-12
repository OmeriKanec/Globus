package org.example;

@FunctionalInterface
public interface ReceiptPrinter {
    void print(Order order, PricingResult pricing);
}
