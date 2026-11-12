package org.example;

@FunctionalInterface
public interface OrderRepository {
    void save(Order order);
}
