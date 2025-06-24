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

/**
 * HTTP handler for /bookings endpoints.
 *   - POST   /bookings        (create)
 *   - GET    /bookings        (list all)
 *   - GET    /bookings/{id}   (retrieve one)
 *   - DELETE /bookings/{id}   (cancel/delete)
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
        // Configure Jackson to handle java.time.Instant in ISO format
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
//if-else statement for user handling
        if ("POST".equalsIgnoreCase(method) && path.equals("/bookings")) {
            handleCreate(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.startsWith("/bookings/")) {
            handleGet(exchange);
        } else if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/bookings/")) {
            handleCancel(exchange);
        } else {
            //if all else fails, display 'unrecognized route' HTML 404 code.
            exchange.sendResponseHeaders(404, -1);
            exchange.getResponseBody().close();
        }
    }
//create a new booking from the JSON payload
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
            //this code will return all bookings as a JSON Array
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, resp.length);
            //Code 201, means booking is created and the booking is live. Should output data.
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(resp);
            }
        }
    }
    //this section fetches a single booking by ID or if its missing display 404
    private void handleGet(HttpExchange exchange) throws IOException {
        UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/", 3)[2]);
        Booking found = repo.find(id);
        if (found == null) {
            //if null, display html 404, file not found
            exchange.sendResponseHeaders(404, -1);
            exchange.getResponseBody().close();
            return;
        }
        byte[] resp = json.writeValueAsBytes(found);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        //display html code 200: Okay 'fetched successfully'
        exchange.sendResponseHeaders(200, resp.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(resp);
        }
    }
//this code section handles booking cancellation, (deletes via ID)
    //result will output either a 404, file is deleted or a 204: No Content, booking cancelled.
    private void handleCancel(HttpExchange exchange) throws IOException {
        UUID id = UUID.fromString(exchange.getRequestURI().getPath().split("/", 3)[2]);
        boolean removed = repo.delete(id);

        int status = removed ? 204 : 404;
        exchange.sendResponseHeaders(status, -1);
        exchange.getResponseBody().close();
        //once file is deleted, close the exchange
    }
}
