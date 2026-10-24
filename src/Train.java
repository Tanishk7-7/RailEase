import java.util.ArrayList;
import java.util.List;

public class Train {
    private int trainId;
    private String trainNumber;
    private String trainName;
    private int totalSeats;
    private List<TrainRoute> routes;
    private int availableSeats;
    private String journeyDate;
    private String departureTime;
    private String arrivalTime;
    private String sourceStationName;
    private String destinationStationName;
    private double journeyDistance;
    private String availableClasses;

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

    public String getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getSourceStationName() {
        return sourceStationName;
    }

    public void setSourceStationName(String sourceStationName) {
        this.sourceStationName = sourceStationName;
    }

    public String getDestinationStationName() {
        return destinationStationName;
    }

    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
    }

    public double getJourneyDistance() {
        return journeyDistance;
    }

    public void setJourneyDistance(double journeyDistance) {
        this.journeyDistance = journeyDistance;
    }

    public String getAvailableClasses() {
        return availableClasses;
    }

    public void setAvailableClasses(String availableClasses) {
        this.availableClasses = availableClasses;
    }

    @Override
    public String toString() {
        return trainNumber + " - " + trainName;
    }
}
