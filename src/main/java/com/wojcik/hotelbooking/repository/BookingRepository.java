package com.wojcik.hotelbooking.repository;

import com.wojcik.hotelbooking.model.Booking;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for storing bookings.
 */
public class BookingRepository {
    private final Map<UUID, Booking> storage = new ConcurrentHashMap<>();

    /**
     * Save a new booking.
     */
    public Booking save(Booking booking) {
        storage.put(booking.getId(), booking);
        return booking;
    }

    /**
     * Find booking by ID.
     */
    public Booking find(UUID id) {
        return storage.get(id);
    }

    /**
     * Delete booking by ID.
     * @return true if a booking was removed
     */
    public boolean delete(UUID id) {
        return storage.remove(id) != null;
    }
}