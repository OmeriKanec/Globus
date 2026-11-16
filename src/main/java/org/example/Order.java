package org.example;

import java.util.List;

public class Order {

    private String id;
    private String customerType; // "REGULAR", "VIP", "PREMIUM"
    private String paymentType;  // "CARD", "CASH", "TRANSFER"
    private List<String> items;
    private double amount;

    public Order(String id, String customerType, String paymentType, List<String> items, double amount) {
        this.id = id;
        this.customerType = customerType;
        this.paymentType = paymentType;
        this.items = items;
        this.amount = amount;
    }

    public String getId() { return id; }
    public String getCustomerType() { return customerType; }
    public String getPaymentType() { return paymentType; }
    public List<String> getItems() { return items; }
    public double getAmount() { return amount; }
}
