package com.minibank;

import java.util.List;

public class NPlusOneDemo {

    public static void main(String[] args) {
        try (BankService bank = new BankService()) {
            System.out.println("=== Начало загрузки через JOIN FETCH ===");

            List<Client> clients = bank.getClientsWithAccounts();

            for (Client client : clients) {
                System.out.println(
                        client.getName()
                        + ", счетов: " + client.getAccounts().size()
                );
            }

            System.out.println("Клиентов: " + clients.size());
            System.out.println("=== Конец загрузки через JOIN FETCH ===");
        }
    }
}
