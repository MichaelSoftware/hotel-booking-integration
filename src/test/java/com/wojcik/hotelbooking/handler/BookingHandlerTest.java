package com.wojcik.hotelbooking.handler;

import com.wojcik.hotelbooking.handler.BookingHandler;
import com.wojcik.hotelbooking.model.Booking;
import com.wojcik.hotelbooking.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BookingHandler via an embedded HTTP server.
 */
class BookingHandlerTest {
    private static HttpServer server;
    private static String baseUrl;
    private static final ObjectMapper json = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void startServer() throws Exception {
        // start on random port
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/bookings", new BookingHandler(new BookingRepository()));
        server.setExecutor(null);
        server.start();

        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void testCreateGetCancelHappyPath() throws Exception {
        // Create booking
        Map<String,Object> req = Map.of(
            "userId",    "hotelName",
            "hotelName", "HotelTest",
            "checkIn",   Instant.parse("2025-01-01T12:00:00Z").toString(),
            "checkOut",  Instant.parse("2025-01-02T12:00:00Z").toString()
        );
        String body = json.writeValueAsString(req);

        HttpRequest createReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/bookings"))
            .header("Content-Type","application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> createResp = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResp.statusCode());

        Map<?,?> created = json.readValue(createResp.body(), Map.class);
        assertNotNull(created.get("id"));
        UUID id = UUID.fromString(created.get("id").toString());

        // test the http request for Get Booking
        HttpRequest getReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/bookings/" + id))
            .GET().build();
        HttpResponse<String> getResp = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResp.statusCode());
        Map<?,?> fetched = json.readValue(getResp.body(), Map.class);
        assertEquals("username", fetched.get("userId"));

        // test the http request to Cancel Booking
        HttpRequest delReq = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/bookings/" + id))
            .DELETE().build();
        HttpResponse<Void> delResp = client.send(delReq, HttpResponse.BodyHandlers.discarding());
        assertEquals(204, delResp.statusCode());

        // Verify Deletion
        HttpResponse<String> after = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, after.statusCode());
    }
}