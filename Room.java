// === Room.java ===
public class Room {
    private int roomId;
    private String type;
    private double price;
    private boolean isAvailable;

    public Room(int roomId, String type, double price, boolean isAvailable) {
        this.roomId = roomId;
        this.type = type;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    public int getRoomId()      { return roomId; }
    public String getType()     { return type; }
    public double getPrice()    { return price; }
    public boolean isAvailable(){ return isAvailable; }

    @Override
    public String toString() {
        // Replaced the Unicode rupee sign with plain ASCII text "Rs."
        return roomId + " | " + type + " | Rs." + price + " | " +
               (isAvailable ? "Available" : "Booked");
    }
}

