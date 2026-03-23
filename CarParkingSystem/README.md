# Simple Car Parking System

A comprehensive Car Parking Management System built using Java and Vanilla web technologies. The project strongly utilizes Data Structure and Algorithm (DSA) concepts to efficiently manage the parking space and dynamically allocate slots.

## Features
- **Car Entry**: Auto-assign an available slot to an incoming car.
- **Car Exit**: Remove cars using their license plate.
- **Queueing**: If parking is full, cars are added to a waiting queue and automatically placed when a spot frees up.
- **Live Status**: Display visually which slots are occupied.

## Data Structures Used
- **Array**: Fixed-size representation of standard parking slots (e.g. 10 slots).
- **HashMap (`parkedCars`)**: Stores `<LicensePlate, SlotIndex>` for constant time O(1) lookups to instantly find where a car is.
- **Queue (`waitingQueue`)**: Follows FIFO logic. Arriving cars go to waitlist when the lot is full.
- **Stack (`recentExits`)**: Follows LIFO logic to track the history of recently departed cars.

## How to Run

1. **Start the Backend**
   - Open your IDE or terminal.
   - Navigate to `src` folder.
   - Compile and execute the `Main.java` file.
   - The Java backend will start an embedded HTTP Server on `http://localhost:8080`.

2. **Open the Frontend**
   - Navigate to the `frontend` directory.
   - Open `index.html` in your web browser.
   - You can now interact with the UI, which will send API requests directly to the running Java program.
