package com.wojcik.hotelbooking;

import com.wojcik.hotelbooking.model.Booking;
import com.wojcik.hotelbooking.repository.BookingRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Here is the Happy-path unit tests for BookingRepository.
 */
public class BookingRepositoryTest {
    @Test
    void testSaveFindDelete() {
        BookingRepository repo = new BookingRepository();

        // Create a booking
        Booking b = new Booking(
                "Name",
                "HotelName",
                Instant.parse("2025-01-01T12:00:00Z"),
                Instant.parse("2025-12-31T12:00:00Z")
        );
        repo.save(b);
        UUID id = b.getId(); //gets the UUID value through b.getID

        // Find The Booking
        Booking fetched = repo.find(id);
        assertNotNull(fetched, "Locating Booking Info");
        assertEquals("Name", fetched.getUserId());
        assertEquals("HotelName", fetched.getHotelName());

        // Delete it
        boolean removed = repo.delete(id);
        assertTrue(removed, "Booking is being deleted");

        // Now find returns null
        assertNull(repo.find(id), "Deleted booking should no longer exist");
        //application test finished
    }
}