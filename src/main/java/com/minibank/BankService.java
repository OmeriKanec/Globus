package com.minibank;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BankService {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/minibank";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "3009";

    private final String url;
    private final String user;
    private final String password;

    public BankService() {
        this(DB_URL, DB_USER, DB_PASSWORD);
    }

    BankService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Client registerClient(String name, String email) {
        String sql = """
                INSERT INTO clients (name, email)
                VALUES (?, ?)
                RETURNING id, name, email
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Client insert returned no row");
                }
                return readClient(result);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new EmailAlreadyExistsException(email, e);
            }
            throw new IllegalStateException("Failed to register client", e);
        }
    }

    public Account openAccount(Long clientId, String accountNumber) {

        getClient(clientId);

        String sql = """
                INSERT INTO accounts (client_id, account_number)
                VALUES (?, ?)
                RETURNING id, client_id, account_number, balance
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, clientId);
            statement.setString(2, accountNumber);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Account insert returned no row");
                }
                return readAccount(result);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open account", e);
        }
    }

    public Account getAccount(Long accountId) {
        String sql = """
                SELECT id, client_id, account_number, balance
                FROM accounts
                WHERE id = ?
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, accountId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new AccountNotFoundException(accountId);
                }
                return readAccount(result);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get account", e);
        }
    }

    public Client getClient(Long clientId) {
        String sql = """
                SELECT id, name, email
                FROM clients
                WHERE id = ?
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, clientId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new ClientNotFoundException(clientId);
                }
                return readClient(result);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get client", e);
        }
    }

    public void deposit(Long accountId, BigDecimal amount) {
        try (Connection connection = openConnection()) {
            creditAccount(connection, accountId, amount);
            recordTransaction(connection, accountId, amount, "DEPOSIT");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to deposit money", e);
        }
    }

    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть больше 0");
        }

        try (Connection connection = openConnection()) {
            debitAccount(connection, fromAccountId, amount);
            recordTransaction(connection, fromAccountId, amount.negate(), "TRANSFER_OUT");


            creditAccount(connection, toAccountId, amount);
            recordTransaction(connection, toAccountId, amount, "TRANSFER_IN");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to transfer money", e);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void debitAccount(Connection connection, Long accountId, BigDecimal amount)
            throws SQLException {
        String sql = """
                UPDATE accounts
                SET balance = balance - ?
                WHERE id = ? AND balance >= ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setObject(2, accountId);
            statement.setBigDecimal(3, amount);

            if (statement.executeUpdate() == 0) {
                getAccount(accountId);
                throw new InsufficientFundsException(accountId);
            }
        }
    }

    private void creditAccount(Connection connection, Long accountId, BigDecimal amount)
            throws SQLException {
        String sql = """
                UPDATE accounts
                SET balance = balance + ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, amount);
            statement.setObject(2, accountId);

            if (statement.executeUpdate() == 0) {
                throw new AccountNotFoundException(accountId);
            }
        }
    }

    private void recordTransaction(Connection connection, Long accountId,
                                   BigDecimal amount, String type) throws SQLException {
        String sql = """
                INSERT INTO transactions (account_id, amount, type)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, accountId);
            statement.setBigDecimal(2, amount);
            statement.setString(3, type);
            statement.executeUpdate();
        }
    }

    private Client readClient(ResultSet result) throws SQLException {
        return new Client(result.getLong("id"), result.getString("name"),
                result.getString("email"));
    }

    private Account readAccount(ResultSet result) throws SQLException {
        return new Account(result.getLong("id"), result.getLong("client_id"),
                result.getString("account_number"), result.getBigDecimal("balance"));
    }
}
