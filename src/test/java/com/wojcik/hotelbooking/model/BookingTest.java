package com.wojcik.hotelbooking.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Booking model.
 * HotelX and Y
 * userA and userB for testing
 */
class BookingTest {
    @Test
    void testConvenienceConstructorAndGetters() {
        Instant now = Instant.parse("2025-01-01T12:00:00Z"); //here lets test the booking date 
        Instant later = now.plusSeconds(3600); //delay of 3600
        Booking b = new Booking("userA", "HotelX", now, later);

        // ID should be auto-generated
        //aTest for userA
        UUID id = b.getId();
        assertNotNull(id, "ID must not be null");

        // Other fields should match constructor args
        assertEquals("userA", b.getUserId());
        assertEquals("HotelX", b.getHotelName());
        assertEquals(now, b.getCheckIn());
        assertEquals(later, b.getCheckOut());
    }

    @Test
    void testSettersUpdateFields() {
        Booking b = new Booking();

        // Test setting ID
        UUID newId = UUID.randomUUID();
        b.setId(newId);
        assertEquals(newId, b.getId());

        // Test other setters
        //b test for userB
        b.setUserId("userB");
        b.setHotelName("HotelY");
        Instant ci = Instant.parse("2025-01-01T12:00:00Z");
        Instant co = Instant.parse("2025-01-02T12:00:00Z");
        b.setCheckIn(ci);
        b.setCheckOut(co);

        assertEquals("userB", b.getUserId());
        assertEquals("HotelY", b.getHotelName());
        assertEquals(ci, b.getCheckIn());
        assertEquals(co, b.getCheckOut());
    }
}
