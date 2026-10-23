public class Passenger extends Person {
    private int age;
    private String gender;

    public Passenger() {
        super();
    }

    public Passenger(int passengerId, String passengerName, int age, String gender, String phone) {
        super(passengerId, passengerName, phone);
        this.age = age;
        this.gender = gender;
    }

    public Passenger(String passengerName, int age, String gender, String phone) {
        super(passengerName, phone);
        this.age = age;
        this.gender = gender;
    }

    @Override
    public String getRole() {
        return "Passenger";
    }

    public int getPassengerId() {
        return getId();
    }

    public void setPassengerId(int passengerId) {
        setId(passengerId);
    }

    public String getPassengerName() {
        return getName();
    }

    public void setPassengerName(String passengerName) {
        setName(passengerName);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Passenger #" + getPassengerId() + " (" + getPassengerName() + ", Age: " + age + ", Gender: " + gender + ", Phone: " + getPhone() + ")";
    }
}
