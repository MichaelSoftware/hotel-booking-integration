package com.wojcik.hotelbooking.model;
import java.time.Instant;
import java.util.UUID;
/*
Represents a single hotel reservation. Contains an auto-generated unique ID and the user who made it.
Also contains the hotel name, and a ISO-8601 checkin and checkout timestamp.
 */
public class Booking {
    private UUID id;                  // Unique UUID identifier
    private String userId;            // ID of the user making reservation
    private String hotelName;         // Hotel name
    private Instant checkIn;          // Check-in timestamp
    private Instant checkOut;         // Check-out timestamp

    // Default constructor for Jackson (Faster XML)
    public Booking() {}

    // Convenience constructor
    public Booking(String userId, String hotelName, Instant checkIn, Instant checkOut) {

        this.id = UUID.randomUUID();
        this.userId = userId;
        this.hotelName = hotelName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public Instant getCheckIn() { return checkIn; }
    public void setCheckIn(Instant checkIn) { this.checkIn = checkIn; }
    public Instant getCheckOut() { return checkOut; }
    public void setCheckOut(Instant checkOut) { this.checkOut = checkOut; }
}
