public class UserAccount {
    private final int userId;
    private final Integer passengerId;
    private final String username;
    private final String role;
    private final String passengerName;
    private final String passengerPhone;

    public UserAccount(int userId, Integer passengerId, String username, String role, String passengerName, String passengerPhone) {
        this.userId = userId;
        this.passengerId = passengerId;
        this.username = username;
        this.role = role != null ? role.toUpperCase() : "PASSENGER";
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
    }

    public int getUserId() {
        return userId;
    }

    public Integer getPassengerId() {
        return passengerId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getPassengerName() {
        return passengerName != null && !passengerName.isEmpty() ? passengerName : username;
    }

    public String getPassengerPhone() {
        return passengerPhone != null ? passengerPhone : "";
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isPassenger() {
        return "PASSENGER".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", passengerId=" + passengerId +
                '}';
    }
}
