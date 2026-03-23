package com.parking.smart_parking_system.service;

import com.parking.smart_parking_system.model.*;
import com.parking.smart_parking_system.repository.BookingRepository;
import com.parking.smart_parking_system.repository.ParkingSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ParkingService {

    private final ParkingSlotRepository slotRepository;
    private final BookingRepository bookingRepository;

    public ParkingService(ParkingSlotRepository slotRepository, BookingRepository bookingRepository) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        initializeSlotsIfEmpty();
    }

    private void initializeSlotsIfEmpty() {
        if (slotRepository.count() == 0) {
            List<ParkingSlot> initialSlots = new ArrayList<>();
            // Floor 1: Small & Medium
            initialSlots.add(new ParkingSlot(101, 1, SlotType.SMALL));
            initialSlots.add(new ParkingSlot(102, 1, SlotType.SMALL));
            initialSlots.add(new ParkingSlot(103, 1, SlotType.MEDIUM));
            // Floor 2: Medium & Large
            initialSlots.add(new ParkingSlot(201, 2, SlotType.MEDIUM));
            initialSlots.add(new ParkingSlot(202, 2, SlotType.LARGE));
            slotRepository.saveAll(initialSlots);
        }
    }

    public List<ParkingSlot> getAllSlots() {
        return slotRepository.findAll();
    }

    public List<ParkingSlot> getSlotsByFloor(int floor) {
        return slotRepository.findByFloorNumber(floor);
    }

    @Transactional
    public String parkVehicle(String vehicleNumber, VehicleType vehicleType) {
        Optional<Booking> activeBooking = bookingRepository.findByVehicleNumberAndStatus(vehicleNumber,
                BookingStatus.BOOKED);

        ParkingSlot slot;
        if (activeBooking.isPresent()) {
            slot = activeBooking.get().getSlot();
            activeBooking.get().setEntryTime(LocalDateTime.now());
            activeBooking.get().setStatus(BookingStatus.PARKED);
        } else {
            slot = findAvailableSlotForType(vehicleType);
            if (slot == null)
                return "No compatible slot available!";

            Booking newBooking = new Booking(vehicleNumber, vehicleType, slot);
            newBooking.setEntryTime(LocalDateTime.now());
            newBooking.setStatus(BookingStatus.PARKED);
            bookingRepository.save(newBooking);
        }

        slot.setOccupied(true);
        slot.setVehicleNumber(vehicleNumber);
        slotRepository.save(slot);
        return "Vehicle " + vehicleNumber + " parked in Floor " + slot.getFloorNumber() + " Slot "
                + slot.getSlotNumber();
    }

    @Transactional
    public String bookSlot(String vehicleNumber, VehicleType vehicleType) {
        ParkingSlot slot = findAvailableSlotForType(vehicleType);
        if (slot == null)
            return "No compatible slot available for booking!";

        Booking booking = new Booking(vehicleNumber, vehicleType, slot);
        bookingRepository.save(booking);

        slot.setOccupied(true); // Reserved
        slot.setVehicleNumber(vehicleNumber + " (Booked)");
        slotRepository.save(slot);

        return "Booking confirmed for " + vehicleNumber + " at Floor " + slot.getFloorNumber() + " Slot "
                + slot.getSlotNumber();
    }

    @Transactional
    public String removeVehicle(String vehicleNumber) {
        Booking booking = bookingRepository.findByVehicleNumberAndStatus(vehicleNumber, BookingStatus.PARKED)
                .orElseThrow(() -> new RuntimeException("No active parking found for vehicle: " + vehicleNumber));

        return processExit(booking);
    }

    @Transactional
    public String removeVehicleBySlot(int slotNumber) {
        ParkingSlot slot = slotRepository.findBySlotNumber(slotNumber)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotNumber));

        if (!slot.isOccupied()) {
            throw new RuntimeException("Slot " + slotNumber + " is not occupied.");
        }

        // Handle both PARKED and BOOKED (Reserved) statuses
        Booking booking = bookingRepository
                .findBySlotAndStatusIn(slot, Arrays.asList(BookingStatus.PARKED, BookingStatus.BOOKED))
                .orElseThrow(() -> new RuntimeException("No active booking/parking found for slot: " + slotNumber));

        return processExit(booking);
    }

    private String processExit(Booking booking) {
        LocalDateTime exitTime = LocalDateTime.now();
        booking.setExitTime(exitTime);

        // Use 0.0 if entryTime is null (for reserved slots being released)
        double fee = 0.0;
        if (booking.getEntryTime() != null) {
            fee = calculateFee(booking.getEntryTime(), exitTime, booking.getVehicleType());
        }

        booking.setTotalFee(fee);
        booking.setStatus(BookingStatus.COMPLETED);

        ParkingSlot slot = booking.getSlot();
        slot.setOccupied(false);
        slot.setVehicleNumber(null);

        slotRepository.save(slot);
        bookingRepository.save(booking);

        if (booking.getEntryTime() == null) {
            return "Booking cancelled for slot " + slot.getSlotNumber();
        }

        return String.format("Vehicle removed. Duration: %d mins. Total Fee: ₹%.2f",
                Duration.between(booking.getEntryTime(), exitTime).toMinutes(), fee);
    }

    public Map<String, Object> getStats() {
        List<ParkingSlot> allSlots = slotRepository.findAll();
        List<Booking> completedBookings = bookingRepository.findByStatus(BookingStatus.COMPLETED);

        long occupied = allSlots.stream().filter(ParkingSlot::isOccupied).count();
        double totalRevenue = completedBookings.stream()
                .mapToDouble(b -> b.getTotalFee() != null ? b.getTotalFee() : 0.0).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSlots", allSlots.size());
        stats.put("availableSlots", allSlots.size() - occupied);
        stats.put("occupiedSlots", occupied);
        stats.put("totalRevenue", totalRevenue);
        stats.put("occupancyPercentage", (allSlots.size() > 0 ? (occupied * 100.0 / allSlots.size()) : 0.0));

        return stats;
    }

    private ParkingSlot findAvailableSlotForType(VehicleType vehicleType) {
        List<SlotType> compatibleTypes = getCompatibleSlotTypes(vehicleType);
        List<ParkingSlot> available = slotRepository.findByOccupiedFalseAndSlotTypeIn(compatibleTypes);
        return available.isEmpty() ? null : available.get(0);
    }

    private List<SlotType> getCompatibleSlotTypes(VehicleType vehicleType) {
        switch (vehicleType) {
            case BIKE:
                return Arrays.asList(SlotType.SMALL, SlotType.MEDIUM, SlotType.LARGE);
            case CAR:
                return Arrays.asList(SlotType.MEDIUM, SlotType.LARGE);
            case TRUCK:
                return Collections.singletonList(SlotType.LARGE);
            default:
                return Collections.emptyList();
        }
    }

    private double calculateFee(LocalDateTime entry, LocalDateTime exit, VehicleType type) {
        long minutes = Duration.between(entry, exit).toMinutes();
        if (minutes <= 30)
            return 0.0;

        double ratePerHour;
        switch (type) {
            case BIKE:
                ratePerHour = 10.0;
                break;
            case TRUCK:
                ratePerHour = 40.0;
                break;
            case CAR:
            default:
                ratePerHour = 20.0;
                break;
        }

        double hours = Math.ceil(minutes / 60.0);
        return hours * ratePerHour;
    }
}
