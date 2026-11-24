package com.minibank;

import java.util.UUID;

public class OrphanRemovalDemo {

    public static void main(String[] args) {
        try (BankService bank = new BankService()) {
            String suffix = UUID.randomUUID().toString().replace("-", "");
            Client client = bank.registerClient("Orphan removal demo", "orphan+" + suffix + "@example.com");
            Account account = bank.openAccount(client.getId(), "ORPHAN-" + suffix.substring(0, 26));

            Long accountId = account.getId();
            System.out.println("Счёт до удаления: " + bank.getAccount(accountId).getId());
            System.out.println("=== Удаление счёта из коллекции клиента ===");

            bank.removeAccount(client.getId(), accountId);

            try {
                bank.getAccount(accountId);
                throw new IllegalStateException("Счёт остался в БД после удаления из коллекции");
            } catch (AccountNotFoundException e) {
                System.out.println("Счёт " + accountId + " удалён из БД через orphanRemoval");
            }
        }
    }
}
