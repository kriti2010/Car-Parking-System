package com.parking.smart_parking_system.repository;

import com.parking.smart_parking_system.model.ParkingSlot;
import com.parking.smart_parking_system.model.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    List<ParkingSlot> findByFloorNumber(int floorNumber);

    Optional<ParkingSlot> findBySlotNumber(int slotNumber);

    Optional<ParkingSlot> findBySlotNumberAndFloorNumber(int slotNumber, int floorNumber);

    List<ParkingSlot> findByOccupiedFalse();

    List<ParkingSlot> findByOccupiedFalseAndSlotTypeIn(List<SlotType> compatibleTypes);
}
