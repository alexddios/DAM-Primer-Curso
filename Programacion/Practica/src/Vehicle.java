public abstract class Vehicle implements Llogable {
    protected String matricula;
    protected String marca;
    protected double preuDiari;
    protected EstatVehicle estat;

    public Vehicle(String matricula,String marca, double preuDiari,EstatVehicle estat){
        this.matricula=matricula;
        this.marca=marca;
        this.preuDiari=preuDiari;
        this.estat=estat;
    }

    public abstract void mostrarDetalls();

    public EstatVehicle getEstat(){
        return  this.estat;
    }
    // En Vehicle.java añade:
    public void setEstat(EstatVehicle estat){
        this.estat = estat;
    }
}
