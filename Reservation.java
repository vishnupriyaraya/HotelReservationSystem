public class Reservation {
    private int id;
    private String customerName;
    private int roomId;
    private String checkinDate;
    private String checkoutDate;
    private double totalCost;
    private String status;          // booked | cancelled

    public Reservation(int id, String customerName, int roomId,
                       String checkinDate, String checkoutDate,
                       double totalCost, String status) {
        this.id = id;
        this.customerName = customerName;
        this.roomId = roomId;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.totalCost = totalCost;
        this.status = status;
    }

    // getters (add more if needed)
    public int getId()            { return id; }
    public String getCustomerName(){ return customerName; }
    public int getRoomId()        { return roomId; }
    public String getCheckinDate(){ return checkinDate; }
    public String getCheckoutDate(){ return checkoutDate; }
    public double getTotalCost()  { return totalCost; }
    public String getStatus()     { return status; }
}
