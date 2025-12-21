package cli;

import wms.core.service.*;
import wms.core.repository.ExcelRepository;
import java.util.*;

public class CliApp {

    private static final AdminService adminService = new AdminService();
    private static final InboundService inboundService = new InboundService();
    private static final InventoryService inventoryService = new InventoryService();
    private static final OutboundService outboundService = new OutboundService();
    private static final Scanner sc = new Scanner(System.in);

    private static final Map<String, String> users = Map.of(
            "admin", "ADMIN",
            "inbound1", "INBOUND",
            "inventory1", "INVENTORY",
            "outbound1", "OUTBOUND"
    );

    public static void main(String[] args) {
        while (true) {
            String username = loginPrompt();
            String role = users.get(username);
            System.out.println("Selamat datang, " + username + " (" + role + ")");
            if ("ADMIN".equals(role)) {
                adminMenu();
            } else {
                operatorMenu(role);
            }
        }
    }

    private static String loginPrompt() {
        while (true) {
            System.out.println("=== WMS CLI Login ===");
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            if (users.containsKey(username)) {
                return username;
            } else {
                System.out.println("User tidak ditemukan, coba lagi.\n");
            }
        }
    }

    private static void adminMenu() {
        while (true) {
            System.out.println("\n=== Menu Admin ===");
            System.out.println("1. Lihat Lokasi");
            System.out.println("2. Tambah Lokasi");
            System.out.println("0. Logout");
            System.out.print("Pilih menu: ");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> listLocations();
                    case "2" -> addLocation();
                    case "0" -> {
                        System.out.println("Logout...\n");
                        return;
                    }
                    default -> System.out.println("Pilihan tidak valid.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void operatorMenu(String role) {
        while (true) {
            System.out.println("\n=== Menu Operator (" + role + ") ===");
            switch (role) {
                case "INBOUND" -> System.out.println("1. Inbound Barang");
                case "INVENTORY" -> System.out.println("1. Relocate Barang");
                case "OUTBOUND" -> System.out.println("1. Outbound Barang");
            }
            System.out.println("0. Logout");
            System.out.print("Pilih menu: ");
            String choice = sc.nextLine().trim();
            try {
                if ("0".equals(choice)) {
                    System.out.println("Logout...\n");
                    return;
                }
                switch (role) {
                    case "INBOUND" -> inbound();
                    case "INVENTORY" -> relocate();
                    case "OUTBOUND" -> outbound();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void listLocations() {
        System.out.println("\n=== Daftar Lokasi ===");
        ExcelRepository.get().locations().forEach(System.out::println);
    }

    private static void addLocation() {
        System.out.print("Masukkan kode lokasi baru: ");
        String loc = sc.nextLine().trim();
        adminService.addLocation(loc);
        System.out.println("Lokasi berhasil ditambahkan.");
    }

    private static void inbound() {
        System.out.print("UPC: "); String upc = sc.nextLine().trim();
        System.out.print("Manufacturer: "); String manu = sc.nextLine().trim();
        System.out.print("Category: "); String cat = sc.nextLine().trim();
        System.out.print("Qty: "); int qty = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Location: "); String loc = sc.nextLine().trim();
        inboundService.receive(upc, manu, cat, qty, loc);
        System.out.println("Inbound berhasil.");
    }

    private static void relocate() {
        System.out.print("UPC: "); String upc = sc.nextLine().trim();
        System.out.print("SKU: "); String sku = sc.nextLine().trim();
        System.out.print("From Location: "); String from = sc.nextLine().trim();
        System.out.print("To Location: "); String to = sc.nextLine().trim();
        System.out.print("Qty: "); int qty = Integer.parseInt(sc.nextLine().trim());
        inventoryService.relocate(upc, sku, from, to, qty);
        System.out.println("Relocate berhasil.");
    }

    private static void outbound() {
        System.out.print("UPC: "); String upc = sc.nextLine().trim();
        System.out.print("Location: "); String loc = sc.nextLine().trim();
        System.out.print("Qty: "); int qty = Integer.parseInt(sc.nextLine().trim());
        outboundService.pick(upc, loc, qty);
        System.out.println("Outbound berhasil.");
    }
}
