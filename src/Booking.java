import java.util.ArrayList;
import java.util.List;

public class Booking {
    public static class BookingPassengerItem {
        private final int bookingPassengerId;
        private final String passengerName;
        private final int age;
        private final String gender;
        private final int passengerNumber;

        public BookingPassengerItem(int bookingPassengerId, String passengerName, int age, String gender, int passengerNumber) {
            this.bookingPassengerId = bookingPassengerId;
            this.passengerName = passengerName;
            this.age = age;
            this.gender = gender;
            this.passengerNumber = passengerNumber;
        }

        public int getBookingPassengerId() {
            return bookingPassengerId;
        }

        public String getPassengerName() {
            return passengerName;
        }

        public int getAge() {
            return age;
        }

        public String getGender() {
            return gender;
        }

        public int getPassengerNumber() {
            return passengerNumber;
        }

        @Override
        public String toString() {
            return passengerName + " (" + gender + ", Age: " + age + ")";
        }
    }

    private int bookingId;
    private String pnr;
    private int passengerId;
    private String passengerName;
    private String passengerPhone;
    private int passengerAge;
    private String passengerGender;
    private int trainId;
    private String trainNumber;
    private String trainName;
    private String journeyDate;
    private int sourceStationId;
    private String sourceStationName;
    private int destinationStationId;
    private String destinationStationName;
    private double fare;
    private String status;
    private String bookingTime;

    private int scheduleId;
    private int trainClassId;
    private String className;
    private String classDescription;
    private String departureTime;
    private String arrivalTime;

    // Payment details
    private Integer paymentId;
    private Double paymentAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionReference;
    private String paymentDate;

    // Cancellation details
    private Integer cancellationId;
    private String cancellationReason;
    private String cancellationDate;
    private Double refundAmount;

    // Passenger list
    private List<BookingPassengerItem> bookingPassengers = new ArrayList<>();

    public Booking() {
    }

    public Booking(int bookingId, String pnr, int passengerId, int trainId, String journeyDate,
                   int sourceStationId, int destinationStationId, double fare, String status) {
        this.bookingId = bookingId;
        this.pnr = pnr;
        this.passengerId = passengerId;
        this.trainId = trainId;
        this.journeyDate = journeyDate;
        this.sourceStationId = sourceStationId;
        this.destinationStationId = destinationStationId;
        this.fare = fare;
        this.status = status;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
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

    public String getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }

    public int getSourceStationId() {
        return sourceStationId;
    }

    public void setSourceStationId(int sourceStationId) {
        this.sourceStationId = sourceStationId;
    }

    public String getSourceStationName() {
        return sourceStationName;
    }

    public void setSourceStationName(String sourceStationName) {
        this.sourceStationName = sourceStationName;
    }

    public int getDestinationStationId() {
        return destinationStationId;
    }

    public void setDestinationStationId(int destinationStationId) {
        this.destinationStationId = destinationStationId;
    }

    public String getDestinationStationName() {
        return destinationStationName;
    }

    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getTrainClassId() {
        return trainClassId;
    }

    public void setTrainClassId(int trainClassId) {
        this.trainClassId = trainClassId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getClassDescription() {
        return classDescription;
    }

    public void setClassDescription(String classDescription) {
        this.classDescription = classDescription;
    }

    public int getPassengerAge() {
        return passengerAge;
    }

    public void setPassengerAge(int passengerAge) {
        this.passengerAge = passengerAge;
    }

    public String getPassengerGender() {
        return passengerGender;
    }

    public void setPassengerGender(String passengerGender) {
        this.passengerGender = passengerGender;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Integer getCancellationId() {
        return cancellationId;
    }

    public void setCancellationId(Integer cancellationId) {
        this.cancellationId = cancellationId;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(String cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public List<BookingPassengerItem> getBookingPassengers() {
        return bookingPassengers;
    }

    public void setBookingPassengers(List<BookingPassengerItem> bookingPassengers) {
        this.bookingPassengers = bookingPassengers != null ? bookingPassengers : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "PNR: " + pnr + " | Train: " + trainName + " | Status: " + status;
    }
}
