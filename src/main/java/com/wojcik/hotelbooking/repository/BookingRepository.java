package com.wojcik.hotelbooking.repository;

import com.wojcik.hotelbooking.model.Booking;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage-repo for storing booking (objects)
 * This program uses ConcurrentHashmap as a thread-safe storage of booking ID to booking.
 */
public class BookingRepository {
    //map of booking ID to booking
    private final Map<UUID, Booking> storage = new ConcurrentHashMap<>();

    /**
     * Save a new booking. (either create or overwrite)
     */
    public Booking save(Booking booking) {
        storage.put(booking.getId(), booking); //create the booking to store and return the stored booking
        return booking;
    }

    /**
     * Find/Retrieve the booking by its ID. If the booking is found, return otherwise its null
     */
    public Booking find(UUID id) {
        return storage.get(id);
    }

    /**
     * Delete booking by ID of all the bookings that are currently stored.
     * @return true if a booking was removed
     */
    public boolean delete(UUID id) {
        return storage.remove(id) != null;
    }
}