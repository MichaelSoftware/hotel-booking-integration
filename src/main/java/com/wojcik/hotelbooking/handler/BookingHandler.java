package com.wojcik.hotelbooking.handler;

import com.sun.net.httpserver.Headers;
import com.wojcik.hotelbooking.model.Booking;
import com.wojcik.hotelbooking.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Handles booking-related HTTP requests: create, view, cancel.
 */
public class BookingHandler implements HttpHandler {
    private final BookingRepository repo;
    private final ObjectMapper json = new ObjectMapper()
            // support java.time types (Instant, LocalDate, etc.)
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            // serialize dates as ISO strings, not timestamps
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public BookingHandler(BookingRepository repo) {
        this.repo = repo;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        Headers h = exchange.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        // Handle preflight
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        if ("POST".equalsIgnoreCase(method) && path.equals("/bookings")) {
            handleCreate(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.startsWith("/bookings/")) {
            handleGet(exchange);
        } else if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/bookings/")) {
            handleCancel(exchange);
        } else {
            exchange.sendResponseHeaders(404, -1);
            exchange.getResponseBody().close();
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            Booking request = json.readValue(in, Booking.class);
            Booking saved = repo.save(new Booking(
                    request.getUserId(),
                    request.getHotelName(),
                    request.getCheckIn(),
                    request.getCheckOut()
            ));

            byte[] resp = json.writeValueAsBytes(saved);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, resp.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(resp);
            }
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/", 3)[2]);
        Booking found = repo.find(id);
        if (found == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.getResponseBody().close();
            return;
        }
        byte[] resp = json.writeValueAsBytes(found);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resp.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(resp);
        }
    }

    private void handleCancel(HttpExchange exchange) throws IOException {
        UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/", 3)[2]);
        boolean removed = repo.delete(id);

        int status = removed ? 204 : 404;
        exchange.sendResponseHeaders(status, -1);
        exchange.getResponseBody().close();
    }
}