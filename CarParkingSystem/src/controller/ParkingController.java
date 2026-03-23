package controller;

import service.ParkingLot;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

public class ParkingController {
    private ParkingLot parkingLot;

    public ParkingController(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/park", new ParkHandler());
        server.createContext("/api/book", new BookHandler());
        server.createContext("/api/unbook", new UnbookHandler());
        server.createContext("/api/remove", new RemoveHandler());
        server.createContext("/api/status", new StatusHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Backend Server running on http://localhost:8080");
    }

    private void handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
        }
    }

    class ParkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String plate = extractParam(query, "plate");
                String type = extractParam(query, "type");
                if (type == null || type.isEmpty())
                    type = "Car"; // Default
                String response = parkingLot.parkCar(plate, type);
                sendResponse(exchange, response);
            }
        }
    }

    class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String plate = extractParam(query, "plate");
                String type = extractParam(query, "type");
                if (type == null || type.isEmpty())
                    type = "Car"; // Default
                String response = parkingLot.bookCar(plate, type);
                sendResponse(exchange, response);
            }
        }
    }

    class UnbookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String slotStr = extractParam(query, "slot");
                String response = parkingLot.unbookSlot(slotStr);
                sendResponse(exchange, response);
            }
        }
    }

    class RemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String plate = extractParam(query, "plate");
                String response = parkingLot.removeCar(plate);
                sendResponse(exchange, response);
            }
        }
    }

    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handleCors(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<Integer, String> status = parkingLot.getParkingStatus();
                StringBuilder json = new StringBuilder("{");
                int count = 0;
                for (Map.Entry<Integer, String> entry : status.entrySet()) {
                    json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                    if (++count < status.size())
                        json.append(",");
                }
                json.append("}");

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.length());
                OutputStream os = exchange.getResponseBody();
                os.write(json.toString().getBytes());
                os.close();
            }
        }
    }

    private String extractParam(String query, String param) {
        if (query == null || query.isEmpty())
            return "";
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(param)) {
                return keyValue[1];
            }
        }
        return "";
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
