import java.util.ArrayList;
import java.util.List;

public class RutaRepartiment extends ServeiLlogistic implements Planificable{
    private String horaSortida;
    private PrioritatEnviament prioritat;
    private final List<Parada> parades;
    Repartidor repartidor;
    Vehicle vehicle;

    public RutaRepartiment(String codi, String nom, double costBase,
                           String horaSortida, PrioritatEnviament prioritat, Repartidor repartidor, Vehicle vehicle){
        super(codi,nom,costBase);
        this.horaSortida=horaSortida;
        this.prioritat=prioritat;
        this.parades= new ArrayList<>();
        this.repartidor=repartidor;
        this.vehicle=vehicle;
    }







    @Override
    public void mostrarInformacio() {
        System.out.printf("""
                Codi: %s
                Nom: %s
                Cost: %f
                Hora Sortida: %s
                Prioritat: %s
                Repartidor: %s
                Vehicle: %s
                """,codi,nom,costBase,horaSortida,prioritat,repartidor,vehicle);
    }
    public double calcularCostFinal(){
        switch (this.prioritat){
            case NORMAL -> {
                return this.costBase;
            }
            case URGENT -> {
                return this.costBase*1.1;
            }
            case EXPRESS -> {
                return this.costBase*1.2;
            }
        }
        return 0;
    }
    public boolean permetCanvis(){
        return this.prioritat != PrioritatEnviament.EXPRESS;
    }
    public void afegirParada(Parada p){
        parades.add(p);
    }
    public boolean eliminarParada(int posicio){
        if(posicio>=0 && posicio<parades.size()){
            parades.remove(posicio);
            return true;
        }
        return false;
    }
}
