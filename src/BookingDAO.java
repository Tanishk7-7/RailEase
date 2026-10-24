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
        if (passenger.getPassengerId() > 0) {
            String idSql = "SELECT passenger_id, passenger_name, age, gender, phone FROM Passenger WHERE passenger_id = ?";
            try (PreparedStatement idStmt = conn.prepareStatement(idSql)) {
                idStmt.setInt(1, passenger.getPassengerId());
                try (ResultSet rs = idStmt.executeQuery()) {
                    if (rs.next()) {
                        return passenger;
                    }
                }
            }
        }

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
        return bookTicket(passenger, trainId, srcStationId, destStationId, journeyDate, null, fare, "UPI");
    }

    public Booking bookTicket(Passenger passenger, int trainId, int srcStationId, int destStationId,
                              String journeyDate, Integer trainClassId, double fare) throws SQLException, IllegalArgumentException {
        return bookTicket(passenger, trainId, srcStationId, destStationId, journeyDate, trainClassId, fare, "UPI");
    }

    public Booking bookTicket(Passenger passenger, int trainId, int srcStationId, int destStationId,
                              String journeyDate, Integer trainClassId, double fare, String paymentMethod) throws SQLException, IllegalArgumentException {

        Train train = trainDAO.getTrainById(trainId);
        if (train == null) {
            throw new IllegalArgumentException("Selected train does not exist.");
        }
        if (!train.isValidRoute(srcStationId, destStationId)) {
            throw new IllegalArgumentException("Invalid route: Source station must occur before Destination station on this train.");
        }

        String dbPaymentMethod = "UPI";
        if (paymentMethod != null) {
            String m = paymentMethod.trim().toUpperCase().replace(" ", "_");
            if (m.contains("CARD")) {
                dbPaymentMethod = "CARD";
            } else if (m.contains("NET") || m.contains("BANK")) {
                dbPaymentMethod = "NET_BANKING";
            } else if (m.contains("CASH")) {
                dbPaymentMethod = "CASH";
            } else {
                dbPaymentMethod = "UPI";
            }
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String seatCheckSql = "SELECT COUNT(*) AS booked FROM Booking b "
                    + "JOIN TrainSchedule ts ON b.schedule_id = ts.schedule_id "
                    + "WHERE b.train_id = ? AND ts.journey_date = ? AND b.status = 'CONFIRMED' FOR UPDATE";
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

            if (trainClassId == null || trainClassId <= 0) {
                trainClassId = getValidTrainClassId(conn, trainId);
            }

            int scheduleId = getOrCreateScheduleId(conn, trainId, journeyDate);

            Passenger savedPassenger = registerPassenger(conn, passenger);

            String pnr = generatePNR();

            String insertSql = "INSERT INTO Booking (pnr, passenger_id, train_id, schedule_id, train_class_id, "
                    + "source_station_id, destination_station_id, fare, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";
            int generatedBookingId = 0;
            try (PreparedStatement bookingStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                bookingStmt.setString(1, pnr);
                bookingStmt.setInt(2, savedPassenger.getPassengerId());
                bookingStmt.setInt(3, trainId);
                bookingStmt.setInt(4, scheduleId);
                bookingStmt.setInt(5, trainClassId);
                bookingStmt.setInt(6, srcStationId);
                bookingStmt.setInt(7, destStationId);
                bookingStmt.setDouble(8, fare);
                bookingStmt.executeUpdate();

                try (ResultSet rs = bookingStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedBookingId = rs.getInt(1);
                    }
                }
            }

            String bpSql = "INSERT INTO BookingPassenger (booking_id, passenger_id, passenger_name, age, gender, passenger_number) "
                    + "VALUES (?, ?, ?, ?, ?, 1)";
            try (PreparedStatement bpStmt = conn.prepareStatement(bpSql)) {
                bpStmt.setInt(1, generatedBookingId);
                bpStmt.setInt(2, savedPassenger.getPassengerId());
                bpStmt.setString(3, savedPassenger.getPassengerName());
                bpStmt.setInt(4, savedPassenger.getAge());
                bpStmt.setString(5, savedPassenger.getGender());
                bpStmt.executeUpdate();
            }

            // Generate demo/simulated transaction reference
            String txnPrefix;
            if ("CARD".equals(dbPaymentMethod)) {
                txnPrefix = "SIM-CARD-";
            } else if ("NET_BANKING".equals(dbPaymentMethod)) {
                txnPrefix = "SIM-NB-";
            } else {
                txnPrefix = "SIM-UPI-";
            }
            String txnRef = txnPrefix + pnr;

            String paySql = "INSERT INTO Payment (booking_id, amount, payment_method, payment_status, transaction_reference) "
                    + "VALUES (?, ?, ?, 'SUCCESS', ?)";
            try (PreparedStatement payStmt = conn.prepareStatement(paySql)) {
                payStmt.setInt(1, generatedBookingId);
                payStmt.setDouble(2, fare);
                payStmt.setString(3, dbPaymentMethod);
                payStmt.setString(4, txnRef);
                payStmt.executeUpdate();
            }

            conn.commit();

            Booking booking = new Booking(generatedBookingId, pnr, savedPassenger.getPassengerId(), trainId,
                    journeyDate, srcStationId, destStationId, fare, "CONFIRMED");
            booking.setPassengerName(savedPassenger.getPassengerName());
            booking.setPassengerPhone(savedPassenger.getPhone());
            booking.setPassengerAge(savedPassenger.getAge());
            booking.setPassengerGender(savedPassenger.getGender());
            booking.setTrainName(train.getTrainName());
            booking.setTrainNumber(train.getTrainNumber());
            booking.setScheduleId(scheduleId);
            booking.setTrainClassId(trainClassId);
            booking.setPaymentAmount(fare);
            booking.setPaymentMethod(dbPaymentMethod);
            booking.setPaymentStatus("SUCCESS");
            booking.setTransactionReference(txnRef);

            TrainRoute srcRoute = train.getRouteForStation(srcStationId);
            TrainRoute dstRoute = train.getRouteForStation(destStationId);
            if (srcRoute != null) booking.setSourceStationName(srcRoute.getStationName());
            if (dstRoute != null) booking.setDestinationStationName(dstRoute.getStationName());

            booking.getBookingPassengers().add(new Booking.BookingPassengerItem(
                    1, savedPassenger.getPassengerName(), savedPassenger.getAge(), savedPassenger.getGender(), 1
            ));

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

    public int getValidTrainClassId(Connection conn, int trainId) throws SQLException {
        String classSql = "SELECT train_class_id FROM TrainClass WHERE train_id = ? ORDER BY train_class_id ASC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(classSql)) {
            stmt.setInt(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("train_class_id");
                }
            }
        }
        throw new SQLException("No train class found for train ID: " + trainId);
    }

    public int getOrCreateScheduleId(Connection conn, int trainId, String journeyDate) throws SQLException {
        String findSql = "SELECT schedule_id FROM TrainSchedule WHERE train_id = ? AND journey_date = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setInt(1, trainId);
            stmt.setString(2, journeyDate);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("schedule_id");
                }
            }
        }

        String timeSql = "SELECT departure_time, arrival_time FROM TrainSchedule WHERE train_id = ? LIMIT 1";
        String depTime = "08:00:00";
        String arrTime = "14:00:00";
        try (PreparedStatement stmt = conn.prepareStatement(timeSql)) {
            stmt.setInt(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    depTime = rs.getString("departure_time");
                    arrTime = rs.getString("arrival_time");
                }
            }
        }

        String insertSql = "INSERT INTO TrainSchedule (train_id, journey_date, departure_time, arrival_time, schedule_status) "
                + "VALUES (?, ?, ?, ?, 'SCHEDULED')";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, trainId);
            stmt.setString(2, journeyDate);
            stmt.setString(3, depTime);
            stmt.setString(4, arrTime);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create train schedule for train ID " + trainId + " on " + journeyDate);
    }

    public boolean cancelTicket(String pnr) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT booking_id, fare, status FROM Booking WHERE pnr = ? FOR UPDATE";
            int bookingId = 0;
            double fare = 0.0;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, pnr.trim());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    bookingId = rs.getInt("booking_id");
                    fare = rs.getDouble("fare");
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
                    String payUpdateSql = "UPDATE Payment SET payment_status = 'REFUNDED' WHERE booking_id = ?";
                    try (PreparedStatement payStmt = conn.prepareStatement(payUpdateSql)) {
                        payStmt.setInt(1, bookingId);
                        payStmt.executeUpdate();
                    }

                    String cancelSql = "INSERT INTO Cancellation (booking_id, cancellation_reason, refund_amount) VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE cancellation_reason = VALUES(cancellation_reason), refund_amount = VALUES(refund_amount)";
                    try (PreparedStatement cancelStmt = conn.prepareStatement(cancelSql)) {
                        cancelStmt.setInt(1, bookingId);
                        cancelStmt.setString(2, "Passenger cancelled the journey");
                        cancelStmt.setDouble(3, fare);
                        cancelStmt.executeUpdate();
                    }

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
        String sql = "SELECT b.booking_id, b.pnr, b.passenger_id, p.passenger_name, p.phone, p.age AS pass_age, p.gender AS pass_gender, "
                + "b.train_id, t.train_number, t.train_name, "
                + "b.schedule_id, ts.journey_date, ts.departure_time, ts.arrival_time, "
                + "b.train_class_id, tc.class_name, tc.description AS class_desc, "
                + "b.source_station_id, s1.station_name AS src_name, "
                + "b.destination_station_id, s2.station_name AS dst_name, "
                + "b.fare, b.status, b.booking_time, "
                + "pay.payment_id, pay.amount AS payment_amount, pay.payment_method, pay.payment_status, pay.transaction_reference, pay.payment_date, "
                + "c.cancellation_id, c.cancellation_reason, c.cancellation_date, c.refund_amount "
                + "FROM Booking b "
                + "JOIN Passenger p ON b.passenger_id = p.passenger_id "
                + "JOIN Train t ON b.train_id = t.train_id "
                + "JOIN TrainSchedule ts ON b.schedule_id = ts.schedule_id "
                + "LEFT JOIN TrainClass tc ON b.train_class_id = tc.train_class_id "
                + "JOIN Station s1 ON b.source_station_id = s1.station_id "
                + "JOIN Station s2 ON b.destination_station_id = s2.station_id "
                + "LEFT JOIN Payment pay ON b.booking_id = pay.booking_id "
                + "LEFT JOIN Cancellation c ON b.booking_id = c.booking_id "
                + "WHERE b.pnr = ? "
                + "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Booking booking = mapResultSetToBooking(rs);

                    // Fetch passenger details from BookingPassenger if present
                    String bpSql = "SELECT booking_passenger_id, passenger_name, age, gender, passenger_number "
                            + "FROM BookingPassenger WHERE booking_id = ? ORDER BY passenger_number ASC";
                    try (PreparedStatement bpStmt = conn.prepareStatement(bpSql)) {
                        bpStmt.setInt(1, booking.getBookingId());
                        try (ResultSet bpRs = bpStmt.executeQuery()) {
                            List<Booking.BookingPassengerItem> bpList = new ArrayList<>();
                            while (bpRs.next()) {
                                bpList.add(new Booking.BookingPassengerItem(
                                        bpRs.getInt("booking_passenger_id"),
                                        bpRs.getString("passenger_name"),
                                        bpRs.getInt("age"),
                                        bpRs.getString("gender"),
                                        bpRs.getInt("passenger_number")
                                ));
                            }
                            booking.setBookingPassengers(bpList);
                        }
                    } catch (SQLException bpEx) {
                        System.err.println("Notice: Could not load BookingPassenger records: " + bpEx.getMessage());
                    }

                    return booking;
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
                + "b.train_id, t.train_number, t.train_name, "
                + "b.schedule_id, ts.journey_date, ts.departure_time, ts.arrival_time, "
                + "b.train_class_id, tc.class_name, "
                + "b.source_station_id, s1.station_name AS src_name, "
                + "b.destination_station_id, s2.station_name AS dst_name, "
                + "b.fare, b.status, b.booking_time "
                + "FROM Booking b "
                + "JOIN Passenger p ON b.passenger_id = p.passenger_id "
                + "JOIN Train t ON b.train_id = t.train_id "
                + "JOIN TrainSchedule ts ON b.schedule_id = ts.schedule_id "
                + "LEFT JOIN TrainClass tc ON b.train_class_id = tc.train_class_id "
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

    public List<Booking> getBookingsByPassengerId(int passengerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.pnr, b.passenger_id, p.passenger_name, p.phone, "
                + "b.train_id, t.train_number, t.train_name, "
                + "b.schedule_id, ts.journey_date, ts.departure_time, ts.arrival_time, "
                + "b.train_class_id, tc.class_name, "
                + "b.source_station_id, s1.station_name AS src_name, "
                + "b.destination_station_id, s2.station_name AS dst_name, "
                + "b.fare, b.status, b.booking_time "
                + "FROM Booking b "
                + "JOIN Passenger p ON b.passenger_id = p.passenger_id "
                + "JOIN Train t ON b.train_id = t.train_id "
                + "JOIN TrainSchedule ts ON b.schedule_id = ts.schedule_id "
                + "LEFT JOIN TrainClass tc ON b.train_class_id = tc.train_class_id "
                + "JOIN Station s1 ON b.source_station_id = s1.station_id "
                + "JOIN Station s2 ON b.destination_station_id = s2.station_id "
                + "WHERE b.passenger_id = ? "
                + "ORDER BY b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, passengerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBooking(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings for passenger #" + passengerId + ": " + e.getMessage());
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

        try {
            b.setBookingTime(String.valueOf(rs.getObject("booking_time")));
        } catch (SQLException ignored) {}

        try {
            b.setPassengerAge(rs.getInt("pass_age"));
            b.setPassengerGender(rs.getString("pass_gender"));
        } catch (SQLException ignored) {}

        try {
            b.setScheduleId(rs.getInt("schedule_id"));
            b.setTrainClassId(rs.getInt("train_class_id"));
            b.setClassName(rs.getString("class_name"));
            b.setClassDescription(rs.getString("class_desc"));
            b.setDepartureTime(String.valueOf(rs.getObject("departure_time")));
            b.setArrivalTime(String.valueOf(rs.getObject("arrival_time")));
        } catch (SQLException ignored) {}

        try {
            int payId = rs.getInt("payment_id");
            if (!rs.wasNull() && payId > 0) {
                b.setPaymentId(payId);
                b.setPaymentAmount(rs.getDouble("payment_amount"));
                b.setPaymentMethod(rs.getString("payment_method"));
                b.setPaymentStatus(rs.getString("payment_status"));
                b.setTransactionReference(rs.getString("transaction_reference"));
                b.setPaymentDate(String.valueOf(rs.getObject("payment_date")));
            }
        } catch (SQLException ignored) {}

        try {
            int cancelId = rs.getInt("cancellation_id");
            if (!rs.wasNull() && cancelId > 0) {
                b.setCancellationId(cancelId);
                b.setCancellationReason(rs.getString("cancellation_reason"));
                b.setCancellationDate(String.valueOf(rs.getObject("cancellation_date")));
                b.setRefundAmount(rs.getDouble("refund_amount"));
            }
        } catch (SQLException ignored) {}

        return b;
    }

    private String generatePNR() {
        int random6Digits = 100000 + new Random().nextInt(900000);
        return "RE" + random6Digits;
    }
}
