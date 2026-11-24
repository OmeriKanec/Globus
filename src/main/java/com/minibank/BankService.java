package com.minibank;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.hibernate.JDBCException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class BankService implements AutoCloseable {

    private final EntityManagerFactory factory;

    public BankService() {
        factory = Persistence.createEntityManagerFactory("minibank");
    }

    BankService(String url, String user, String password) {
        factory = Persistence.createEntityManagerFactory("minibank", Map.of(
                "jakarta.persistence.jdbc.url", url,
                "jakarta.persistence.jdbc.user", user,
                "jakarta.persistence.jdbc.password", password
        ));
    }

    public Client registerClient(String name, String email) {
        try (EntityManager entityManager = factory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                Client client = new Client();
                client.setName(name);
                client.setEmail(email);
                entityManager.persist(client);
                transaction.commit();
                return client;
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (PersistenceException e) {
            for (Throwable cause = e; cause != null; cause = cause.getCause()) {
                if (cause instanceof JDBCException databaseError
                        && "23505".equals(databaseError.getSQLState())
                        && emailExists(email)) {
                    throw new EmailAlreadyExistsException(email, e);
                }
            }
            throw new IllegalStateException("Failed to register client", e);
        }
    }

    public Account openAccount(Long clientId, String accountNumber) {
        try (EntityManager entityManager = factory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                Client client = findClient(entityManager, clientId);
                Account account = new Account();
                account.setAccountNumber(accountNumber);
                client.addAccount(account);
                entityManager.persist(account);
                transaction.commit();
                return account;
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    public void removeAccount(Long clientId, Long accountId) {
        try (EntityManager entityManager = factory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                Client client = findClient(entityManager, clientId);
                Account account = findAccount(entityManager, accountId, LockModeType.PESSIMISTIC_WRITE);
                if (!client.getId().equals(account.getClient().getId())) {
                    throw new IllegalArgumentException("Счёт не принадлежит указанному клиенту");
                }
                client.removeAccount(account);
                transaction.commit();
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    public Account getAccount(Long accountId) {
        try (EntityManager entityManager = factory.createEntityManager()) {
            return findAccount(entityManager, accountId, LockModeType.NONE);
        }
    }

    public Client getClient(Long clientId) {
        try (EntityManager entityManager = factory.createEntityManager()) {
            return findClient(entityManager, clientId);
        }
    }

    public List<Client> getClients() {
        try (EntityManager entityManager = factory.createEntityManager()) {
            return entityManager.createQuery(
                    "select c from Client c order by c.id",
                    Client.class
            ).getResultList();
        }
    }

    public List<Client> getClientsWithAccounts() {
        try (EntityManager entityManager = factory.createEntityManager()) {
            return entityManager.createQuery(
                    "select distinct c from Client c left join fetch c.accounts order by c.id",
                    Client.class
            ).getResultList();
        }
    }

    public void deposit(Long accountId, BigDecimal amount) {
        try (EntityManager entityManager = factory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                Account account = findAccount(entityManager, accountId, LockModeType.PESSIMISTIC_WRITE);
                account.setBalance(account.getBalance().add(amount));
                recordTransaction(entityManager, account, amount, "DEPOSIT");
                transaction.commit();
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }

        try (EntityManager entityManager = factory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                Account from = findAccount(entityManager, fromAccountId, LockModeType.PESSIMISTIC_WRITE);
                if (from.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientFundsException(fromAccountId);
                }
                from.setBalance(from.getBalance().subtract(amount));
                recordTransaction(entityManager, from, amount.negate(), "TRANSFER_OUT");
                transaction.commit();
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }

        try (EntityManager entityManager = factory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                Account to = findAccount(entityManager, toAccountId, LockModeType.PESSIMISTIC_WRITE);
                to.setBalance(to.getBalance().add(amount));
                recordTransaction(entityManager, to, amount, "TRANSFER_IN");
                transaction.commit();
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    private boolean emailExists(String email) {
        // После ошибки INSERT проверяем email через новый EntityManager:
        // предыдущая транзакция уже откатилась, а её контекст закрыт.
        try (EntityManager entityManager = factory.createEntityManager()) {
            return !entityManager.createQuery(
                    "select c.id from Client c where c.email = :email",
                    Long.class
            ).setParameter("email", email)
                    .setMaxResults(1)
                    .getResultList()
                    .isEmpty();
        }
    }

    private Client findClient(EntityManager entityManager, Long clientId) {
        Client client = clientId == null ? null : entityManager.find(Client.class, clientId);
        if (client == null) {
            throw new ClientNotFoundException(clientId);
        }
        return client;
    }

    private Account findAccount(EntityManager entityManager, Long accountId, LockModeType lockMode) {
        Account account = accountId == null
                ? null : entityManager.find(Account.class, accountId, lockMode);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }
        return account;
    }

    private void recordTransaction(EntityManager entityManager, Account account,
                                   BigDecimal amount, String type) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(amount);
        transaction.setType(type);
        entityManager.persist(transaction);
    }

    @Override
    public void close() {
        if (factory.isOpen()) {
            factory.close();
        }
    }
}
