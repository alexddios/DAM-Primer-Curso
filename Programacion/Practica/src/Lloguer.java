public class Lloguer {
    private final String codi;
    private int dies;
    private boolean pagat;
    private Vehicle vehicle;
    private Client client;

    public Lloguer(String codi,int dies, Vehicle vehicle,Client client){
        this.codi=codi;
        this.dies=dies;
        this.pagat=false;
        this.vehicle=vehicle;
        this.client=client;
    }

    public double calcularPreuFinal(){
        return vehicle.calcularPreuBase()*this.dies;
    }
}
