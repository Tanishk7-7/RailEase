import java.util.List;

public interface TrainOperations {

    List<Train> getAllTrains();

    List<Train> searchTrains(int sourceStationId, int destinationStationId);

    List<Train> searchTrains(int sourceStationId, int destinationStationId, String journeyDate);

    List<Station> getAllStations();

    List<TrainRoute> getRoutesForTrain(int trainId);

    Train getTrainById(int trainId);

    int getAvailableSeats(int trainId, String journeyDate);
}
