package com.parking.smart_parking_system.controller;

import com.parking.smart_parking_system.model.ParkingSlot;
import com.parking.smart_parking_system.model.VehicleType;
import com.parking.smart_parking_system.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/slots")
    public ResponseEntity<List<ParkingSlot>> getSlots(@RequestParam(required = false) Integer floor) {
        if (floor != null) {
            return ResponseEntity.ok(parkingService.getSlotsByFloor(floor));
        }
        return ResponseEntity.ok(parkingService.getAllSlots());
    }

    @PostMapping("/park")
    public ResponseEntity<String> park(@RequestParam String vehicleNumber, @RequestParam VehicleType vehicleType) {
        return ResponseEntity.ok(parkingService.parkVehicle(vehicleNumber, vehicleType));
    }

    @PostMapping("/book")
    public ResponseEntity<String> book(@RequestParam String vehicleNumber, @RequestParam VehicleType vehicleType) {
        return ResponseEntity.ok(parkingService.bookSlot(vehicleNumber, vehicleType));
    }

    @PostMapping("/remove")
    public ResponseEntity<String> remove(@RequestParam String vehicleNumber) {
        return ResponseEntity.ok(parkingService.removeVehicle(vehicleNumber));
    }

    @PostMapping("/removeBySlot")
    public ResponseEntity<String> removeBySlot(@RequestParam int slotNumber) {
        return ResponseEntity.ok(parkingService.removeVehicleBySlot(slotNumber));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(parkingService.getStats());
    }
}
