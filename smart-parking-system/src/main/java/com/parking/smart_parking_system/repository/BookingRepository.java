package com.parking.smart_parking_system.repository;

import com.parking.smart_parking_system.model.Booking;
import com.parking.smart_parking_system.model.BookingStatus;
import com.parking.smart_parking_system.model.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByVehicleNumberAndStatus(String vehicleNumber, BookingStatus status);

    List<Booking> findByStatus(BookingStatus status);

    Optional<Booking> findBySlotAndStatusIn(ParkingSlot slot, Collection<BookingStatus> statuses);
}
