# Hotel Booking Integration

Java 11, Technical Assessment Submission: I have created REST service for hotel bookings. 
Meeting the requirements, It serves as a reference integration for a new API partner expected to handle **3 million booking requests per hour**. 
All endpoints communicate over HTTP with JSON payloads.

Only essential files have been ported to GitHub. 
This has been tested via InteliJ to Download (as zip) and run.

Instructions and features listed below:
## Features

- **Create Booking**: `POST /bookings`
- **View Booking**: `GET /bookings/{id}`
- **List All Bookings**: `GET /bookings`
- **Cancel Booking**: `DELETE /bookings/{id}`

All users share the same role/permissions.

## Stack Used:
- **Developed On: InteliJ IDEA 2025
- **Java 11.0.27** (ms-11.0.27)
- **JDK built-in HttpServer** (no external frameworks)
- **Jackson Databind & JavaTime module** for JSON serialization/deserialization of `java.time.Instant`
- **JUnit 5** for integration tests

## Running the Server

1. **Build & Compile**
2. **Run**
   Look for 
   - hotel-booking-integration/src/main/java/com/wojcik/hotelbooking/Server.java
   - Run `Server.main()`

The server will start on [**http://localhost:8080**](http://localhost:8080) and serve both the REST API and the static UI (`index.html`).

Tested on Firefox and Chrome).

## API Endpoints
                     
| Method | Path             | Description                                    |
| ------ | ---------------- | -------------------------                      |
| POST   | `/bookings`      | Create a new hotel booking                     |
| GET    | `/bookings`      | List all hotel bookings                        |
| GET    | `/bookings/{id}` | Retrieve a single hotel booking                |
| DELETE | `/bookings/{id}` | Cancel (delete) a booking (no content 204/404) |


## Additional Notes

- All endpoints return standard HTTP status codes: `200`, `201`, `204`, `404`, `500`.
- JSON date/time fields use ISO-8601 strings (e.g., `2025-01-01T12:00:00Z`).

```
created by: Michael Wojcik
exclusively for HRSGroup
```
(file will privatize on August 1, 2025)
