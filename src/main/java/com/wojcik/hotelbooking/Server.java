package com.wojcik.hotelbooking;

import com.wojcik.hotelbooking.handler.BookingHandler;
import com.wojcik.hotelbooking.repository.BookingRepository;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This will bootstrap an embedded HTTP server on port 8080
 * and will link/wire:
 *   1) /bookings → BookingHandler (REST API)
 *   2) /         → serves static files
 *
 *    this should serve both the REST API and any static files needed
 */

//bind server to localhost:8080

public class Server {
    public static void main(String[] args) throws Exception {
        //create the server
        // REST API context with debug logging and a proper close:
        //bind the server to localhost:8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // Instantiate the in-memory repo
        BookingRepository repo = new BookingRepository();

        // Log every request (This is the REST API context)
        // Tell the client we will close this connection
        // REST API context (with logging, CORS, and forced close)
        server.createContext("/bookings", exchange -> {
            try {
                System.out.println("[API] " +
                        exchange.getRequestMethod() + " " +
                        exchange.getRequestURI());
                // delegate to handler, which sends CORS headers
                new BookingHandler(repo).handle(exchange);
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
            //closes the exchange after try-catch (finally) loop sequence.
        });

        // Static file context for public/index.html, JavaScript and CSS for UI Looks.
        server.createContext("/", exchange -> {
            try {
                String raw = exchange.getRequestURI().getPath();
                String clean = raw.equals("/") ? "index.html" : raw.substring(1);
                //should be able to open the booking app right on doing localhost:8080 / entering URL
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

        // launch
        server.setExecutor(null); // default executor (uses the default thread pool)
        System.out.println("Application is running successfully on http://localhost:8080");
        //this message should appear in the java console... server should successfully be running.
        server.start();
    }
}
