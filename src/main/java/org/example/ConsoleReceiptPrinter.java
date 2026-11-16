package org.example;

final class ConsoleReceiptPrinter implements ReceiptPrinter {
    @Override
    public void print(Order order, PricingResult pricing) {
        System.out.println("=== RECEIPT ===");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Items: " + order.getItems());
        System.out.println("Base amount: " + order.getAmount());
        System.out.println("Discount: -" + pricing.discount());
        System.out.println("Commission: +" + pricing.commission());
        System.out.println("Final amount: " + pricing.finalAmount());
        System.out.println("================");
    }
}
