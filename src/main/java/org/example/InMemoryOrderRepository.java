package org.example;

import java.util.ArrayList;
import java.util.List;

final class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    @Override
    public void save(Order order) {
        orders.add(order);
    }
}
