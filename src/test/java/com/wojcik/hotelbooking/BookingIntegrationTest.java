// BookingIntegrationTest.java
package com.wojcik.hotelbooking;

import com.wojcik.hotelbooking.handler.BookingHandler;
import com.wojcik.hotelbooking.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
 * Happy-path integration tests for the /bookings REST API.
 */
public class BookingIntegrationTest {
    private static HttpServer server;
    private static String baseUrl;
    private static final ObjectMapper json = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void startServer() throws Exception {
        // Start an embedded HTTP server on a random free port
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/bookings", new BookingHandler(new BookingRepository()));
        server.setExecutor(null);
        server.start();

        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
        //should generate localhost:8080 by default. If this port is not available find
        //different available port.
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    } //after execution stop server.

    @Test
    void testCreateViewCancel() throws Exception {
        // 1) CREATE
        Map<String,Object> bookingReq = Map.of(
                "userId",    "clientname",
                "hotelName", "hotelname",
                "checkIn",   Instant.parse("2025-01-01T12:00:00Z").toString(),
                "checkOut",  Instant.parse("2025-01-31T12:00:00Z").toString()
        );
        String reqBody = json.writeValueAsString(bookingReq); //parse the JSON to String

        HttpRequest createReq = HttpRequest.newBuilder() //create the URI (/bookings) and build
                .uri(URI.create(baseUrl + "/bookings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        HttpResponse<String> createResp = client.send(createReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResp.statusCode()); //if all goes well HTML code 201 (created)

        Map<?,?> created = json.readValue(createResp.body(), Map.class);
        assertNotNull(created.get("id"));
        UUID id = UUID.fromString(created.get("id").toString());

        // 2) VIEW
        HttpRequest viewReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/bookings/" + id))
                .GET()
                .build();
        HttpResponse<String> viewResp = client.send(viewReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, viewResp.statusCode()); //if request passes gen code: 200

        Map<?,?> fetched = json.readValue(viewResp.body(), Map.class);
        assertEquals("clientname", fetched.get("userId"));
        assertEquals("hotelname", fetched.get("hotelName"));

        // 3) CANCEL
        HttpRequest cancelReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/bookings/" + id))
                .DELETE()
                .build();
        HttpResponse<Void> cancelResp = client.send(cancelReq, HttpResponse.BodyHandlers.discarding());
        assertEquals(204, cancelResp.statusCode()); //if canceled generate code 204

        // 4) VERIFY DELETION
        HttpResponse<String> after = client.send(viewReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, after.statusCode()); //html code 404, if file is deleted. does not exist.
    }
}