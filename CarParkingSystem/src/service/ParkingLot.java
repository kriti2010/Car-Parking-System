package service;

import model.Vehicle;
import java.util.*;

public class ParkingLot {
    private int capacity;
    public Vehicle[] slots; 
    private Map<String, Integer> parkedVehicles;
    private Queue<Vehicle> waitingQueue; 
    private Stack<Vehicle> recentExits;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.slots = new Vehicle[capacity];
        this.parkedVehicles = new HashMap<>();
        this.waitingQueue = new LinkedList<>();
        this.recentExits = new Stack<>();
    }

    public String parkCar(String licensePlate, String type) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return "Invalid license plate.";
        }
        if (parkedVehicles.containsKey(licensePlate)) {
            return type + " " + licensePlate + " is already parked.";
        }

        int availableSlot = -1;
        for (int i = 0; i < capacity; i++) {
            if (slots[i] == null) {
                availableSlot = i;
                break;
            }
        }

        Vehicle newVehicle = new Vehicle(licensePlate, type);
        if (availableSlot != -1) {
            slots[availableSlot] = newVehicle;
            parkedVehicles.put(licensePlate, availableSlot); // HashMap usage
            return type + " " + licensePlate + " parked successfully at Slot " + (availableSlot + 1);
        } else {
            waitingQueue.offer(newVehicle); // Queue usage
            return "Parking full. " + type + " " + licensePlate + " added to waiting queue.";
        }
    }

    public String bookCar(String licensePlate, String type) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return "Invalid license plate.";
        }
        if (parkedVehicles.containsKey("BOOKED-" + licensePlate) || parkedVehicles.containsKey(licensePlate)) {
            return "Vehicle " + licensePlate + " is already booked or parked.";
        }

        int availableSlot = -1;
        for (int i = 0; i < capacity; i++) {
            if (slots[i] == null) {
                availableSlot = i;
                break;
            }
        }

        if (availableSlot != -1) {
            Vehicle reservedVehicle = new Vehicle("BOOKED", type);
            slots[availableSlot] = reservedVehicle; 
            parkedVehicles.put("BOOKED-" + licensePlate, availableSlot);
            return "Slot " + (availableSlot + 1) + " successfully booked for " + type + " " + licensePlate;
        } else {
            return "Parking full. Cannot book right now.";
        }
    }

    public String unbookSlot(String slotStr) {
        try {
            int slotNumber = Integer.parseInt(slotStr);
            int slotIndex = slotNumber - 1; 

            if (slotIndex < 0 || slotIndex >= capacity) {
                return "Invalid slot number.";
            }

            Vehicle vehicle = slots[slotIndex];

            if (vehicle == null) {
                return "Slot " + slotNumber + " is already empty.";
            }

            // Check if it's actually just a booking and not a parked car (or allow
            // releasing parked cars too)
            slots[slotIndex] = null;

            // Remove from HashMap by searching for the value (O(N) since we only have slot
            // index)
            String plateToRemove = null;
            for (Map.Entry<String, Integer> entry : parkedVehicles.entrySet()) {
                if (entry.getValue() == slotIndex) {
                    plateToRemove = entry.getKey();
                    break;
                }
            }
            if (plateToRemove != null) {
                parkedVehicles.remove(plateToRemove);
            }

            if (!waitingQueue.isEmpty()) {
                Vehicle nextVehicle = waitingQueue.poll();
                slots[slotIndex] = nextVehicle;
                parkedVehicles.put(nextVehicle.getLicensePlate(), slotIndex);
                return "Slot " + slotNumber + " released. Waiting " + nextVehicle.getType() + " "
                        + nextVehicle.getLicensePlate() + " parked at Slot " + slotNumber + ".";
            }

            return "Slot " + slotNumber + " has been successfully released.";

        } catch (NumberFormatException e) {
            return "Invalid slot format.";
        }
    }

    public String removeCar(String licensePlate) {
        if (!parkedVehicles.containsKey(licensePlate)) {
            return "Vehicle " + licensePlate + " not found.";
        }

        int slotIndex = parkedVehicles.get(licensePlate);
        Vehicle departingVehicle = slots[slotIndex];
        String type = departingVehicle.getType();

        slots[slotIndex] = null;
        parkedVehicles.remove(licensePlate);
        recentExits.push(departingVehicle); // Stack usage

        if (!waitingQueue.isEmpty()) {
            Vehicle nextVehicle = waitingQueue.poll();
            slots[slotIndex] = nextVehicle;
            parkedVehicles.put(nextVehicle.getLicensePlate(), slotIndex);
            return type + " " + licensePlate + " removed. Waiting " + nextVehicle.getType() + " "
                    + nextVehicle.getLicensePlate() + " parked at Slot " + (slotIndex + 1) + ".";
        }

        return type + " " + licensePlate + " removed from Slot " + (slotIndex + 1) + ".";
    }

    public Map<Integer, String> getParkingStatus() {
        Map<Integer, String> status = new LinkedHashMap<>();
        for (int i = 0; i < capacity; i++) {
            if (slots[i] == null) {
                status.put(i + 1, "Empty");
            } else {
                status.put(i + 1, slots[i].getLicensePlate() + "-" + slots[i].getType());
            }
        }
        return status;
    }
}
