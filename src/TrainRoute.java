public class TrainRoute {
    private int routeId;
    private int trainId;
    private int stationId;
    private String stationName;
    private String city;
    private int stopNumber;
    private double distanceFromSource;

    public TrainRoute() {
    }

    public TrainRoute(int routeId, int trainId, int stationId, int stopNumber, double distanceFromSource) {
        this.routeId = routeId;
        this.trainId = trainId;
        this.stationId = stationId;
        this.stopNumber = stopNumber;
        this.distanceFromSource = distanceFromSource;
    }

    public TrainRoute(int routeId, int trainId, int stationId, String stationName, String city, int stopNumber, double distanceFromSource) {
        this.routeId = routeId;
        this.trainId = trainId;
        this.stationId = stationId;
        this.stationName = stationName;
        this.city = city;
        this.stopNumber = stopNumber;
        this.distanceFromSource = distanceFromSource;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getStopNumber() {
        return stopNumber;
    }

    public void setStopNumber(int stopNumber) {
        this.stopNumber = stopNumber;
    }

    public double getDistanceFromSource() {
        return distanceFromSource;
    }

    public void setDistanceFromSource(double distanceFromSource) {
        this.distanceFromSource = distanceFromSource;
    }

    @Override
    public String toString() {
        String name = (stationName != null) ? stationName : ("Station #" + stationId);
        return "Stop " + stopNumber + ": " + name + " (" + distanceFromSource + " km)";
    }
}
