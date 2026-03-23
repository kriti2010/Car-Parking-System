import controller.ParkingController;
import service.ParkingLot;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Initialize Parking Lot with 10 slots
        ParkingLot parkingLot = new ParkingLot(10);
        ParkingController controller = new ParkingController(parkingLot);

        try {
            System.out.println("Starting Simple Car Parking System...");
            controller.startServer();
            System.out.println("Server is successfully running!");
            System.out.println("Now, open frontend/index.html in your web browser.");
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
