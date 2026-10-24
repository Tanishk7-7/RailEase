import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrainDAO implements TrainOperations {

    @Override
    public List<Train> getAllTrains() {
        List<Train> list = new ArrayList<>();
        String sql = "SELECT t.train_id, t.train_number, t.train_name, t.total_seats, "
                + "(SELECT GROUP_CONCAT(DISTINCT tc.class_name ORDER BY tc.train_class_id SEPARATOR ', ') "
                + " FROM TrainClass tc WHERE tc.train_id = t.train_id) AS available_classes "
                + "FROM Train t ORDER BY t.train_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Train train = new Train(
                        rs.getInt("train_id"),
                        String.valueOf(rs.getObject("train_number")),
                        rs.getString("train_name"),
                        rs.getInt("total_seats")
                );
                train.setAvailableClasses(rs.getString("available_classes"));
                List<TrainRoute> routes = getRoutesForTrain(train.getTrainId());
                train.setRoutes(routes);
                if (routes != null && !routes.isEmpty()) {
                    train.setSourceStationName(routes.get(0).getStationName());
                    train.setDestinationStationName(routes.get(routes.size() - 1).getStationName());
                    train.setJourneyDistance(routes.get(routes.size() - 1).getDistanceFromSource());
                }
                list.add(train);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching trains: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Train> searchTrains(int sourceStationId, int destinationStationId, String journeyDate) {
        List<Train> list = new ArrayList<>();

        if (journeyDate == null || journeyDate.trim().isEmpty()) {
            return searchTrains(sourceStationId, destinationStationId);
        }

        String sql = "SELECT t.train_id, t.train_number, t.train_name, t.total_seats, "
                + "ts.journey_date, ts.departure_time, ts.arrival_time, "
                + "s1.station_name AS source_station_name, s2.station_name AS dest_station_name, "
                + "ABS(r2.distance_from_source - r1.distance_from_source) AS journey_distance, "
                + "(SELECT GROUP_CONCAT(DISTINCT tc.class_name ORDER BY tc.train_class_id SEPARATOR ', ') "
                + " FROM TrainClass tc WHERE tc.train_id = t.train_id) AS available_classes "
                + "FROM Train t "
                + "JOIN TrainRoute r1 ON t.train_id = r1.train_id "
                + "JOIN Station s1 ON r1.station_id = s1.station_id "
                + "JOIN TrainRoute r2 ON t.train_id = r2.train_id "
                + "JOIN Station s2 ON r2.station_id = s2.station_id "
                + "JOIN TrainSchedule ts ON t.train_id = ts.train_id "
                + "WHERE s1.station_name = (SELECT station_name FROM Station WHERE station_id = ?) "
                + "  AND s2.station_name = (SELECT station_name FROM Station WHERE station_id = ?) "
                + "  AND r1.stop_number < r2.stop_number "
                + "  AND ts.journey_date = ? "
                + "  AND ts.schedule_status <> 'CANCELLED' "
                + "ORDER BY ts.departure_time ASC, t.train_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sourceStationId);
            pstmt.setInt(2, destinationStationId);
            pstmt.setString(3, journeyDate.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Train train = new Train(
                            rs.getInt("train_id"),
                            String.valueOf(rs.getObject("train_number")),
                            rs.getString("train_name"),
                            rs.getInt("total_seats")
                    );
                    train.setJourneyDate(String.valueOf(rs.getObject("journey_date")));
                    train.setDepartureTime(String.valueOf(rs.getObject("departure_time")));
                    train.setArrivalTime(String.valueOf(rs.getObject("arrival_time")));
                    train.setSourceStationName(rs.getString("source_station_name"));
                    train.setDestinationStationName(rs.getString("dest_station_name"));
                    train.setJourneyDistance(rs.getDouble("journey_distance"));
                    train.setAvailableClasses(rs.getString("available_classes"));
                    train.setRoutes(getRoutesForTrain(train.getTrainId()));
                    list.add(train);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching trains between stations " + sourceStationId + " and " + destinationStationId + " on date " + journeyDate + ": " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Train> searchTrains(int sourceStationId, int destinationStationId) {
        List<Train> list = new ArrayList<>();

        String sql = "SELECT DISTINCT t.train_id, t.train_number, t.train_name, t.total_seats, "
                + "s1.station_name AS source_station_name, s2.station_name AS dest_station_name, "
                + "ABS(r2.distance_from_source - r1.distance_from_source) AS journey_distance, "
                + "(SELECT GROUP_CONCAT(DISTINCT tc.class_name ORDER BY tc.train_class_id SEPARATOR ', ') "
                + " FROM TrainClass tc WHERE tc.train_id = t.train_id) AS available_classes "
                + "FROM Train t "
                + "JOIN TrainRoute r1 ON t.train_id = r1.train_id "
                + "JOIN Station s1 ON r1.station_id = s1.station_id "
                + "JOIN TrainRoute r2 ON t.train_id = r2.train_id "
                + "JOIN Station s2 ON r2.station_id = s2.station_id "
                + "WHERE s1.station_name = (SELECT station_name FROM Station WHERE station_id = ?) "
                + "  AND s2.station_name = (SELECT station_name FROM Station WHERE station_id = ?) "
                + "  AND r1.stop_number < r2.stop_number "
                + "ORDER BY t.train_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sourceStationId);
            pstmt.setInt(2, destinationStationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Train train = new Train(
                            rs.getInt("train_id"),
                            String.valueOf(rs.getObject("train_number")),
                            rs.getString("train_name"),
                            rs.getInt("total_seats")
                    );
                    train.setSourceStationName(rs.getString("source_station_name"));
                    train.setDestinationStationName(rs.getString("dest_station_name"));
                    train.setJourneyDistance(rs.getDouble("journey_distance"));
                    train.setAvailableClasses(rs.getString("available_classes"));
                    train.setRoutes(getRoutesForTrain(train.getTrainId()));
                    list.add(train);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching trains between stations " + sourceStationId + " and " + destinationStationId + ": " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Station> getAllStations() {
        List<Station> stations = new ArrayList<>();
        String sql = "SELECT MIN(station_id) AS station_id, station_name, city FROM Station GROUP BY station_name, city ORDER BY station_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                stations.add(new Station(
                        rs.getInt("station_id"),
                        rs.getString("station_name"),
                        rs.getString("city")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching stations: " + e.getMessage());
        }
        return stations;
    }

    @Override
    public List<TrainRoute> getRoutesForTrain(int trainId) {
        List<TrainRoute> routes = new ArrayList<>();
        String sql = "SELECT r.route_id, r.train_id, r.station_id, s.station_name, s.city, r.stop_number, r.distance_from_source "
                + "FROM TrainRoute r "
                + "JOIN Station s ON r.station_id = s.station_id "
                + "WHERE r.train_id = ? "
                + "ORDER BY r.stop_number ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    routes.add(new TrainRoute(
                            rs.getInt("route_id"),
                            rs.getInt("train_id"),
                            rs.getInt("station_id"),
                            rs.getString("station_name"),
                            rs.getString("city"),
                            rs.getInt("stop_number"),
                            rs.getDouble("distance_from_source")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching routes for train #" + trainId + ": " + e.getMessage());
        }
        return routes;
    }

    @Override
    public Train getTrainById(int trainId) {
        String sql = "SELECT train_id, train_number, train_name, total_seats FROM Train WHERE train_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Train train = new Train(
                            rs.getInt("train_id"),
                            String.valueOf(rs.getObject("train_number")),
                            rs.getString("train_name"),
                            rs.getInt("total_seats")
                    );
                    train.setRoutes(getRoutesForTrain(trainId));
                    return train;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching train by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int getAvailableSeats(int trainId, String journeyDate) {
        Train train = getTrainById(trainId);
        if (train == null) return 0;

        String sql = "SELECT COUNT(*) AS booked_seats "
                + "FROM Booking b "
                + "JOIN TrainSchedule ts ON b.schedule_id = ts.schedule_id "
                + "WHERE b.train_id = ? AND ts.journey_date = ? AND b.status = 'CONFIRMED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainId);
            pstmt.setString(2, journeyDate);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int booked = rs.getInt("booked_seats");
                    int available = train.getTotalSeats() - booked;
                    return Math.max(available, 0);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating seat availability: " + e.getMessage());
        }
        return train.getTotalSeats();
    }

    public static class TrainClassItem {
        private final int classId;
        private final String className;
        private final String description;

        public TrainClassItem(int classId, String className, String description) {
            this.classId = classId;
            this.className = className;
            this.description = description;
        }

        public int getClassId() {
            return classId;
        }

        public String getClassName() {
            return className;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return className + (description != null && !description.isEmpty() ? " - " + description : "");
        }
    }

    public List<TrainClassItem> getClassesForTrain(int trainId) {
        List<TrainClassItem> classes = new ArrayList<>();
        String sql = "SELECT train_class_id, class_name, description FROM TrainClass WHERE train_id = ? ORDER BY train_class_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trainId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    classes.add(new TrainClassItem(
                            rs.getInt("train_class_id"),
                            rs.getString("class_name"),
                            rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching classes for train #" + trainId + ": " + e.getMessage());
        }
        return classes;
    }
}
