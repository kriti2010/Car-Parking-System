package com.parking.smart_parking_system.model;

import jakarta.persistence.*;

@Entity
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int slotNumber;
    private int floorNumber;

    @Enumerated(EnumType.STRING)
    private SlotType slotType;

    private boolean occupied;
    private String vehicleNumber; // Currently parked vehicle

    public ParkingSlot() {
    }

    public ParkingSlot(int slotNumber, int floorNumber, SlotType slotType) {
        this.slotNumber = slotNumber;
        this.floorNumber = floorNumber;
        this.slotType = slotType;
        this.occupied = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public void setSlotType(SlotType slotType) {
        this.slotType = slotType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}
