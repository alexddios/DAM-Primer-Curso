public class Vehicle {
    private final String matricula;
    private double capacitatKg;

    public Vehicle(String matricula,double capacitatKg){
        this.matricula=matricula;
        this.capacitatKg=capacitatKg;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "matricula='" + matricula + '\'' +
                ", capacitatKg=" + capacitatKg +
                '}';
    }
}
