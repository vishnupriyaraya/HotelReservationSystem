import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Hotel {

    private final Connection conn = DBConnection.connect();
    private static final DateTimeFormatter USER_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public Hotel() {
        createTablesIfMissing();
        insertDefaultRoomsIfEmpty();
        addColumnsIfMissing();
    }

    /* ---------- DB bootstrap ---------- */
    private void createTablesIfMissing() {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT,
                    price REAL,
                    is_available INTEGER DEFAULT 1
                );
            """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_name TEXT,
                    room_id INTEGER,
                    payment_status TEXT
                );
            """);
        } catch (SQLException e) {
            System.out.println("Create table error: " + e.getMessage());
        }
    }

    private void insertDefaultRoomsIfEmpty() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS n FROM rooms")) {
            if (rs.next() && rs.getInt("n") == 0) {
                st.executeUpdate("""
                    INSERT INTO rooms (type, price, is_available) VALUES
                    ('Standard',1000,1), ('Deluxe',2000,1), ('Suite',3000,1);
                """);
            }
        } catch (SQLException e) {
            System.out.println("Insert rooms error: " + e.getMessage());
        }
    }

    private void addColumnsIfMissing() {
        String[] alter = {
            "ALTER TABLE reservations ADD COLUMN status TEXT DEFAULT 'booked'",
            "ALTER TABLE reservations ADD COLUMN checkin_date TEXT",
            "ALTER TABLE reservations ADD COLUMN checkout_date TEXT",
            "ALTER TABLE reservations ADD COLUMN total_cost REAL"
        };
        for (String sql : alter) {
            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            } catch (SQLException e) {
                if (!e.getMessage().toLowerCase().contains("duplicate column"))
                    System.out.println("Add column error: " + e.getMessage());
            }
        }
    }

    /* ---------- Helpers ---------- */
    private boolean overlaps(int roomId, LocalDate in, LocalDate out) throws SQLException {
        String q = """
            SELECT 1 FROM reservations
            WHERE room_id=? AND status='booked'
              AND NOT(date(checkout_date)<=date(?) OR date(checkin_date)>=date(?))
        """;
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setInt(1, roomId);
            ps.setString(2, in.toString());
            ps.setString(3, out.toString());
            return ps.executeQuery().next();
        }
    }

    private double nightsCost(double daily, LocalDate in, LocalDate out) {
        return ChronoUnit.DAYS.between(in, out) * daily;
    }

    private String fmt(LocalDate d) { return d.format(USER_FMT); }

    private String fmt(String yyyyMMdd) {
        try { return fmt(LocalDate.parse(yyyyMMdd)); }
        catch (Exception e) { return yyyyMMdd; }
    }

    /* ---------- Public room list ---------- */
    public List<Room> getAvailableRooms() {
        List<Room> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM rooms WHERE is_available=1")) {
            while (rs.next()) {
                list.add(new Room(rs.getInt("room_id"),
                                   rs.getString("type"),
                                   rs.getDouble("price"),
                                   true));
            }
        } catch (SQLException e) {
            System.out.println("Fetch rooms error: " + e.getMessage());
        }
        return list;
    }

    /* ---------- NEW: Show availability for specific dates ---------- */
    public void showAvailableRoomsByDate(LocalDate in, LocalDate out) {
        System.out.printf("%nAvailability from %s to %s%n", fmt(in), fmt(out));
        System.out.println("Room ID | Type | Price | Available?");
        System.out.println("------------------------------------");

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM rooms ORDER BY room_id")) {
            while (rs.next()) {
                int id = rs.getInt("room_id");
                String type = rs.getString("type");
                double price = rs.getDouble("price");
                boolean free = !overlaps(id, in, out);
                System.out.printf("%d | %s | Rs.%.0f | %s%n",
                        id, type, price, free ? "YES" : "NO");
            }
        } catch (SQLException e) {
            System.out.println("Availability check error: " + e.getMessage());
        }
    }

    /* ---------- Booking with receipt ---------- */
    public Reservation bookRoomWithReceipt(String cust, int roomId, LocalDate in, LocalDate out) {
        try {
            if (overlaps(roomId, in, out)) {
                System.out.println("Room already booked for those dates.");
                return null;
            }

            double price;
            try (PreparedStatement p =
                     conn.prepareStatement("SELECT price FROM rooms WHERE room_id=?")) {
                p.setInt(1, roomId);
                ResultSet r = p.executeQuery();
                if (!r.next()) { System.out.println("Room ID not found."); return null; }
                price = r.getDouble("price");
            }

            double total = nightsCost(price, in, out);
            String ins = """
                INSERT INTO reservations
                (customer_name, room_id, payment_status, status, checkin_date, checkout_date, total_cost)
                VALUES (?, ?, 'Paid', 'booked', ?, ?, ?)
            """;
            try (PreparedStatement p = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, cust);
                p.setInt(2, roomId);
                p.setString(3, in.toString());
                p.setString(4, out.toString());
                p.setDouble(5, total);
                p.executeUpdate();

                ResultSet keys = p.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);

                    try (PreparedStatement upd =
                             conn.prepareStatement("UPDATE rooms SET is_available=0 WHERE room_id=?")) {
                        upd.setInt(1, roomId);
                        upd.executeUpdate();
                    }

                    return new Reservation(id, cust, roomId,
                            in.toString(), out.toString(), total, "booked");
                }
            }
        } catch (SQLException e) {
            System.out.println("Booking error: " + e.getMessage());
        }
        return null;
    }

    /* ---------- Cancel booking ---------- */
    public boolean cancelBooking(int id) {
        String q = "SELECT room_id FROM reservations WHERE id=? AND status='booked'";
        try (PreparedStatement p = conn.prepareStatement(q)) {
            p.setInt(1, id);
            ResultSet r = p.executeQuery();
            if (!r.next()) return false;

            int roomId = r.getInt("room_id");

            try (PreparedStatement upd =
                     conn.prepareStatement("UPDATE reservations SET status='cancelled' WHERE id=?")) {
                upd.setInt(1, id);
                upd.executeUpdate();
            }
            try (PreparedStatement free =
                     conn.prepareStatement("UPDATE rooms SET is_available=1 WHERE room_id=?")) {
                free.setInt(1, roomId);
                free.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Cancel error: " + e.getMessage());
            return false;
        }
    }

    /* ---------- View functions ---------- */
    public void viewAllBookings() {
        System.out.println("ID | Name | Room | Checkin | Checkout | Cost | Status");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM reservations ORDER BY id")) {
            while (rs.next()) {
                System.out.printf("%d | %s | %d | %s | %s | Rs.%.0f | %s%n",
                        rs.getInt("id"), rs.getString("customer_name"),
                        rs.getInt("room_id"),
                        fmt(rs.getString("checkin_date")),
                        fmt(rs.getString("checkout_date")),
                        rs.getDouble("total_cost"),
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            System.out.println("View bookings error: " + e.getMessage());
        }
    }

    public void showRoomSchedule() {
        try (Statement st = conn.createStatement();
             ResultSet rooms = st.executeQuery("SELECT * FROM rooms ORDER BY room_id")) {
            while (rooms.next()) {
                int id = rooms.getInt("room_id");
                String type = rooms.getString("type");
                double price = rooms.getDouble("price");
                System.out.printf("\nRoom %d - %s (Rs.%.0f):%n", id, type, price);

                String bSql = """
                    SELECT checkin_date, checkout_date, status
                    FROM reservations
                    WHERE room_id = ?
                    ORDER BY checkin_date
                """;
                try (PreparedStatement pst = conn.prepareStatement(bSql)) {
                    pst.setInt(1, id);
                    ResultSet bks = pst.executeQuery();
                    boolean any = false;
                    while (bks.next()) {
                        any = true;
                        System.out.printf("  %s: %s to %s%n",
                                bks.getString("status"),
                                fmt(bks.getString("checkin_date")),
                                fmt(bks.getString("checkout_date")));
                    }
                    if (!any) System.out.println("  Available for all dates.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Schedule error: " + e.getMessage());
        }
    }
}
