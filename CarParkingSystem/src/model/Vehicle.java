package model;

public class Vehicle {
    private String licensePlate;
    private String type; // Car, Bike, Truck

    public Vehicle(String licensePlate, String type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getType() {
        return type;
    }
}
