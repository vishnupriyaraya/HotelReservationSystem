import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final DateTimeFormatter INPUT_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
        Hotel hotel = new Hotel();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Hotel Reservation System =====");
            System.out.println("1. Check Room Availability by Dates");
            System.out.println("2. View Available Rooms");
            System.out.println("3. Book a Room");
            System.out.println("4. Cancel Reservation");
            System.out.println("5. View All Bookings");
            System.out.println("6. View Room Schedule (all bookings)");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                /* -------------------------------------------------- */
                /* 1) Check availability for ALL rooms for a date range */
                /* -------------------------------------------------- */
                case "1" -> {
                    try {
                        System.out.print("Check‑in date (DD‑MM‑YYYY): ");
                        LocalDate in = LocalDate.parse(scanner.nextLine().trim(), INPUT_FMT);
                        System.out.print("Check‑out date (DD‑MM‑YYYY): ");
                        LocalDate out = LocalDate.parse(scanner.nextLine().trim(), INPUT_FMT);
                        if (!out.isAfter(in)) {
                            System.out.println("Check‑out must be after check‑in.");
                            break;
                        }
                        hotel.showAvailableRoomsByDate(in, out);
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format.");
                    }
                }

                /* -------------------------------------------------- */
                /* 2) View currently available rooms (simple flag)    */
                /* -------------------------------------------------- */
                case "2" -> {
                    List<Room> rooms = hotel.getAvailableRooms();
                    if (rooms.isEmpty()) System.out.println("No rooms available.");
                    else {
                        System.out.println("Room ID | Type | Price | Availability");
                        rooms.forEach(System.out::println);
                    }
                }

                /* -------------------------------------------------- */
                /* 3) Book a room with receipt                        */
                /* -------------------------------------------------- */
                case "3" -> {
                    try {
                        System.out.print("Enter your name: ");
                        String name = scanner.nextLine().trim();

                        System.out.print("Enter Room ID to book: ");
                        int roomId = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("Check‑in date (DD‑MM‑YYYY): ");
                        LocalDate in = LocalDate.parse(scanner.nextLine().trim(), INPUT_FMT);

                        System.out.print("Check‑out date (DD‑MM‑YYYY): ");
                        LocalDate out = LocalDate.parse(scanner.nextLine().trim(), INPUT_FMT);

                        if (!out.isAfter(in)) {
                            System.out.println("Check‑out must be after check‑in.");
                            break;
                        }

                        Reservation res = hotel.bookRoomWithReceipt(name, roomId, in, out);
                        if (res == null) {
                            System.out.println("? Booking failed. Try different room/date.");
                        } else {
                            System.out.println("\n======= Booking Receipt =======");
                            System.out.println("Customer:   " + res.getCustomerName());
                            System.out.println("Room ID:    " + res.getRoomId());
                            System.out.println("Check‑in:   " + res.getCheckinDate());
                            System.out.println("Check‑out:  " + res.getCheckoutDate());
                            System.out.println("Total Cost: Rs." + res.getTotalCost());
                            System.out.println("Status:     " + res.getStatus());
                            System.out.println("Payment:    Paid");
                            System.out.println("===============================");
                        }
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.out.println("Invalid input. Please check room ID and date format.");
                    }
                }

                /* -------------------------------------------------- */
                /* 4) Cancel a reservation                            */
                /* -------------------------------------------------- */
                case "4" -> {
                    try {
                        System.out.print("Enter Reservation ID to cancel: ");
                        int id = Integer.parseInt(scanner.nextLine().trim());
                        boolean ok = hotel.cancelBooking(id);
                        System.out.println(ok ? "✓ Reservation cancelled."
                                              : "? Cancellation failed or already cancelled.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid reservation ID.");
                    }
                }

                /* -------------------------------------------------- */
                /* 5) View all bookings                               */
                /* -------------------------------------------------- */
                case "5" -> hotel.viewAllBookings();

                /* -------------------------------------------------- */
                /* 6) View full room schedule (all bookings)          */
                /* -------------------------------------------------- */
                case "6" -> hotel.showRoomSchedule();

                /* -------------------------------------------------- */
                /* 0) Exit                                            */
                /* -------------------------------------------------- */
                case "0" -> {
                    System.out.println("Thank you for choosing our Hotel Reservation System. Have a great day!");
                    scanner.close();
                    return;
                }

                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
