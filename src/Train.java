import java.util.ArrayList;
import java.util.List;

public class Train {
    private int trainId;
    private String trainNumber;
    private String trainName;
    private int totalSeats;
    private List<TrainRoute> routes;
    private int availableSeats;

    public Train() {
        this.routes = new ArrayList<>();
    }

    public Train(int trainId, String trainNumber, String trainName, int totalSeats) {
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.routes = new ArrayList<>();
    }

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public List<TrainRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(List<TrainRoute> routes) {
        this.routes = routes;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public TrainRoute getRouteForStation(int stationId) {
        if (routes == null) return null;
        for (TrainRoute route : routes) {
            if (route.getStationId() == stationId) {
                return route;
            }
        }
        return null;
    }

    public boolean isValidRoute(int sourceStationId, int destStationId) {
        TrainRoute srcStop = getRouteForStation(sourceStationId);
        TrainRoute dstStop = getRouteForStation(destStationId);
        return (srcStop != null && dstStop != null && srcStop.getStopNumber() < dstStop.getStopNumber());
    }

    public double getDistanceBetween(int sourceStationId, int destStationId) {
        TrainRoute srcStop = getRouteForStation(sourceStationId);
        TrainRoute dstStop = getRouteForStation(destStationId);
        if (srcStop != null && dstStop != null) {
            return Math.abs(dstStop.getDistanceFromSource() - srcStop.getDistanceFromSource());
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return trainNumber + " - " + trainName;
    }
}
