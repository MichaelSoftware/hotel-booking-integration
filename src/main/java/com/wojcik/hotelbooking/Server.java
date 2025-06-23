package com.wojcik.hotelbooking;

import com.wojcik.hotelbooking.handler.BookingHandler;
import com.wojcik.hotelbooking.repository.BookingRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * create an HTTP Server on locahost:8080
 * this should serve both the REST API and any static files needed
 */
public class Server {
    public static void main(String[] args) throws Exception {
        //create the server
        //// 1) REST API context with debug logging and a proper close:
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // 2) Instantiate your in-memory repo
        BookingRepository repo = new BookingRepository();

                // Log every request
        // Tell the client we will close this connection
        // 3) REST API context (with logging, CORS, and forced close)
        // 3) REST API context
        server.createContext("/bookings", exchange -> {
            try {
                System.out.println("[API] " +
                        exchange.getRequestMethod() + " " +
                        exchange.getRequestURI());
                // delegate to your handler, which sends CORS headers
                new BookingHandler(repo).handle(exchange);
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
        });

        // 4) Static UI context
        server.createContext("/", exchange -> {
            try {
                String raw = exchange.getRequestURI().getPath();
                String clean = raw.equals("/") ? "index.html" : raw.substring(1);
                Path file = Path.of("public", clean);

                if (Files.exists(file) && !Files.isDirectory(file)) {
                    byte[] bytes = Files.readAllBytes(file);
                    String mime = Files.probeContentType(file);
                    exchange.getResponseHeaders()
                            .set("Content-Type", mime != null ? mime : "application/octet-stream");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream out = exchange.getResponseBody()) {
                        out.write(bytes);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } finally {
                exchange.close();
            }
        });

        // 5) kick it off
        server.setExecutor(null); // default executor
        System.out.println("Server running on http://localhost:8080");
        server.start();
    }
}