public class Station {
    private int stationId;
    private String stationName;
    private String city;

    public Station() {
    }

    public Station(int stationId, String stationName, String city) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.city = city;
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

    @Override
    public String toString() {
        return stationName + " (" + city + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Station other = (Station) obj;
        return stationId == other.stationId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(stationId);
    }
}
