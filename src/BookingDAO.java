import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookingDAO {

    private final TrainDAO trainDAO = new TrainDAO();

    public Passenger registerPassenger(Connection conn, Passenger passenger) throws SQLException {

        String checkSql = "SELECT passenger_id, passenger_name, age, gender, phone FROM Passenger WHERE phone = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, passenger.getPhone());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    passenger.setPassengerId(rs.getInt("passenger_id"));
                    return passenger;
                }
            }
        }

        String insertSql = "INSERT INTO Passenger (passenger_name, age, gender, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, passenger.getPassengerName());
            pstmt.setInt(2, passenger.getAge());
            pstmt.setString(3, passenger.getGender());
            pstmt.setString(4, passenger.getPhone());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    passenger.setPassengerId(generatedKeys.getInt(1));
                }
            }
        }
        return passenger;
    }

    public Booking bookTicket(Passenger passenger, int trainId, int srcStationId, int destStationId,
                              String journeyDate, double fare) throws SQLException, IllegalArgumentException {

        Train train = trainDAO.getTrainById(trainId);
        if (train == null) {
            throw new IllegalArgumentException("Selected train does not exist.");
        }
        if (!train.isValidRoute(srcStationId, destStationId)) {
            throw new IllegalArgumentException("Invalid route: Source station must occur before Destination station on this train.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String seatCheckSql = "SELECT COUNT(*) AS booked FROM Booking WHERE train_id = ? AND journey_date = ? AND status = 'CONFIRMED' FOR UPDATE";
            try (PreparedStatement seatStmt = conn.prepareStatement(seatCheckSql)) {
                seatStmt.setInt(1, trainId);
                seatStmt.setString(2, journeyDate);
                try (ResultSet rs = seatStmt.executeQuery()) {
                    if (rs.next()) {
                        int booked = rs.getInt("booked");
                        if (booked >= train.getTotalSeats()) {
                            conn.rollback();
                            throw new IllegalStateException("No seats available on " + train.getTrainName() + " for date " + journeyDate + ".");
                        }
                    }
                }
            }

            Passenger savedPassenger = registerPassenger(conn, passenger);

            String pnr = generatePNR();

            String insertSql = "INSERT INTO Booking (pnr, passenger_id, train_id, journey_date, source_station_id, destination_station_id, fare, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";
            int generatedBookingId = 0;
            try (PreparedStatement bookingStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                bookingStmt.setString(1, pnr);
                bookingStmt.setInt(2, savedPassenger.getPassengerId());
                bookingStmt.setInt(3, trainId);
                bookingStmt.setString(4, journeyDate);
                bookingStmt.setInt(5, srcStationId);
                bookingStmt.setInt(6, destStationId);
                bookingStmt.setDouble(7, fare);
                bookingStmt.executeUpdate();

                try (ResultSet rs = bookingStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedBookingId = rs.getInt(1);
                    }
                }
            }

            conn.commit();

            Booking booking = new Booking(generatedBookingId, pnr, savedPassenger.getPassengerId(), trainId,
                    journeyDate, srcStationId, destStationId, fare, "CONFIRMED");
            booking.setPassengerName(savedPassenger.getPassengerName());
            booking.setPassengerPhone(savedPassenger.getPhone());
            booking.setTrainName(train.getTrainName());
            booking.setTrainNumber(train.getTrainNumber());

            TrainRoute srcRoute = train.getRouteForStation(srcStationId);
            TrainRoute dstRoute = train.getRouteForStation(destStationId);
            if (srcRoute != null) booking.setSourceStationName(srcRoute.getStationName());
            if (dstRoute != null) booking.setDestinationStationName(dstRoute.getStationName());

            return booking;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback error: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {

                }
            }
        }
    }

    public boolean cancelTicket(String pnr) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT status FROM Booking WHERE pnr = ? FOR UPDATE";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, pnr.trim());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    String currentStatus = rs.getString("status");
                    if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                        conn.rollback();
                        throw new IllegalStateException("This ticket is already cancelled.");
                    }
                }
            }

            String updateSql = "UPDATE Booking SET status = 'CANCELLED' WHERE pnr = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, pnr.trim());
                int updatedRows = updateStmt.executeUpdate();
                if (updatedRows > 0) {
                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {

                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public Booking getBookingByPNR(String pnr) {
        String sql = "SELECT b.booking_id, b.pnr, b.passenger_id, p.passenger_name, p.phone, "
                + "b.train_id, t.train_number, t.train_name, b.journey_date, "
                + "b.source_station_id, s1.station_name AS src_name, "
                + "b.destination_station_id, s2.station_name AS dst_name, "
                + "b.fare, b.status "
                + "FROM Booking b "
                + "JOIN Passenger p ON b.passenger_id = p.passenger_id "
                + "JOIN Train t ON b.train_id = t.train_id "
                + "JOIN Station s1 ON b.source_station_id = s1.station_id "
                + "JOIN Station s2 ON b.destination_station_id = s2.station_id "
                + "WHERE b.pnr = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking for PNR " + pnr + ": " + e.getMessage());
        }
        return null;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.pnr, b.passenger_id, p.passenger_name, p.phone, "
                + "b.train_id, t.train_number, t.train_name, b.journey_date, "
                + "b.source_station_id, s1.station_name AS src_name, "
                + "b.destination_station_id, s2.station_name AS dst_name, "
                + "b.fare, b.status "
                + "FROM Booking b "
                + "JOIN Passenger p ON b.passenger_id = p.passenger_id "
                + "JOIN Train t ON b.train_id = t.train_id "
                + "JOIN Station s1 ON b.source_station_id = s1.station_id "
                + "JOIN Station s2 ON b.destination_station_id = s2.station_id "
                + "ORDER BY b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all bookings: " + e.getMessage());
        }
        return list;
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking(
                rs.getInt("booking_id"),
                rs.getString("pnr"),
                rs.getInt("passenger_id"),
                rs.getInt("train_id"),
                String.valueOf(rs.getObject("journey_date")),
                rs.getInt("source_station_id"),
                rs.getInt("destination_station_id"),
                rs.getDouble("fare"),
                rs.getString("status")
        );
        b.setPassengerName(rs.getString("passenger_name"));
        b.setPassengerPhone(rs.getString("phone"));
        b.setTrainNumber(String.valueOf(rs.getObject("train_number")));
        b.setTrainName(rs.getString("train_name"));
        b.setSourceStationName(rs.getString("src_name"));
        b.setDestinationStationName(rs.getString("dst_name"));
        return b;
    }

    private String generatePNR() {
        int random6Digits = 100000 + new Random().nextInt(900000);
        return "RE" + random6Digits;
    }
}
