package com.minibank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class BankHttpServer {

    private static final Gson GSON = new GsonBuilder()
            .setStrictness(Strictness.STRICT)
            .create();

    public static void main(String[] args) throws IOException {
        BankService bank = new BankService();
        try {
            HttpServer server = createServer(bank, 8080);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.stop(0);
                } finally {
                    bank.close();
                }
            }));
            server.start();
        } catch (IOException | RuntimeException e) {
            bank.close();
            throw e;
        }

        System.out.println("MiniBank запущен на http://127.0.0.1:8080");
        System.out.println("Перевод: POST http://127.0.0.1:8080/transfer");
        System.out.println("JSON: {\"fromAccountId\":1,\"toAccountId\":2,\"amount\":100.00}");
        System.out.println("Клиенты: POST http://127.0.0.1:8080/clients");
        System.out.println("Счета: POST http://127.0.0.1:8080/accounts");
    }

    static HttpServer createServer(BankService bank, int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/transfer", exchange -> handleTransfer(exchange, bank));
        server.createContext("/clients", exchange -> handleClients(exchange, bank));
        server.createContext("/accounts", exchange -> handleAccounts(exchange, bank));
        return server;
    }

    private static void handleTransfer(HttpExchange exchange, BankService bank) throws IOException {
        try (exchange) {
            if (!checkPostRequest(exchange, "/transfer")) {
                return;
            }

            try {
                TransferRequest request = readRequest(exchange, TransferRequest.class);
                if (request == null || request.fromAccountId() == null
                        || request.toAccountId() == null || request.amount() == null) {
                    throw new IllegalArgumentException(
                            "Нужно передать fromAccountId, toAccountId и amount в JSON");
                }

                bank.transfer(request.fromAccountId(), request.toAccountId(), request.amount());
                sendResponse(exchange, 200, "Перевод выполнен");
            } catch (RuntimeException e) {
                sendError(exchange, e);
            }
        }
    }

    private static void handleClients(HttpExchange exchange, BankService bank) throws IOException {
        try (exchange) {
            if (!checkPostRequest(exchange, "/clients")) {
                return;
            }

            try {
                ClientRequest request = readRequest(exchange, ClientRequest.class);
                if (request == null || request.name() == null || request.email() == null) {
                    throw new IllegalArgumentException("Нужно передать name и email в JSON");
                }

                Client client = bank.registerClient(request.name(), request.email());
                ClientResponse response = new ClientResponse(client.getId(), client.getName(), client.getEmail());
                sendResponse(exchange, 200, GSON.toJson(response), "application/json; charset=UTF-8");
            } catch (RuntimeException e) {
                sendError(exchange, e);
            }
        }
    }

    private static void handleAccounts(HttpExchange exchange, BankService bank) throws IOException {
        try (exchange) {
            if (!checkPostRequest(exchange, "/accounts")) {
                return;
            }

            try {
                AccountRequest request = readRequest(exchange, AccountRequest.class);
                if (request == null || request.clientId() == null || request.accountNumber() == null) {
                    throw new IllegalArgumentException(
                            "Нужно передать clientId и accountNumber в JSON");
                }

                Account account = bank.openAccount(request.clientId(), request.accountNumber());
                AccountResponse response = new AccountResponse(account.getId(), account.getClient().getId(),
                        account.getAccountNumber(), account.getBalance());
                sendResponse(exchange, 200, GSON.toJson(response), "application/json; charset=UTF-8");
            } catch (RuntimeException e) {
                sendError(exchange, e);
            }
        }
    }

    private static boolean checkPostRequest(HttpExchange exchange, String path) throws IOException {
        if (!exchange.getRequestURI().getPath().equals(path)) {
            sendResponse(exchange, 404, "Путь не найден");
            return false;
        }
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.getResponseHeaders().set("Allow", "POST");
            sendResponse(exchange, 405, "Используйте POST " + path);
            return false;
        }
        return true;
    }

    private static <T> T readRequest(HttpExchange exchange, Class<T> requestType) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return GSON.fromJson(body, requestType);
    }

    private static void sendError(HttpExchange exchange, RuntimeException exception)
            throws IOException {
        int code;
        if (exception instanceof AccountNotFoundException
                || exception instanceof ClientNotFoundException) {
            code = 404;
        } else if (exception instanceof InsufficientFundsException
                || exception instanceof EmailAlreadyExistsException) {
            code = 409;
        } else if (exception instanceof JsonParseException
                || exception instanceof IllegalArgumentException) {
            code = 400;
        } else {
            exception.printStackTrace();
            sendResponse(exchange, 500, "Внутренняя ошибка сервера");
            return;
        }
        sendResponse(exchange, code, "Ошибка: " + exception.getMessage());
    }

    private static void sendResponse(HttpExchange exchange, int code, String response)
            throws IOException {
        sendResponse(exchange, code, response, "text/plain; charset=UTF-8");
    }

    private static void sendResponse(HttpExchange exchange, int code, String response,
                                     String contentType) throws IOException {
        byte[] body = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        if (exchange.getRequestMethod().equals("HEAD")) {
            exchange.sendResponseHeaders(code, -1);
            return;
        }
        exchange.sendResponseHeaders(code, body.length);
        exchange.getResponseBody().write(body);
    }

    private record ClientResponse(Long id, String name, String email) {
    }

    private record AccountResponse(Long id, Long clientId, String accountNumber, BigDecimal balance) {
    }

    private record TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {
    }

    private record ClientRequest(String name, String email) {
    }

    private record AccountRequest(Long clientId, String accountNumber) {
    }
}
