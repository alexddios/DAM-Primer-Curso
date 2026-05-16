import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Agencia {
    private String nom;

    private final Map<String, Lloguer> lloguers;
    private final List<Vehicle> vehicles;

    public Agencia(String nom){
        this.nom=nom;
        this.lloguers= new HashMap<>();
        this.vehicles= new ArrayList<>();
    }

    public Lloguer buscarLloguer(String codi){
        return lloguers.get(codi);
    }
    public boolean eliminarLloguer(String codi){
        if (lloguers.containsKey(codi)){
            lloguers.remove(codi);
            return true;
        }
        return false;
    }

    public boolean registrarLloguer(String codi, Vehicle vehicle, Client client, int dies){
        if(vehicle.getEstat()!= EstatVehicle.DISPONIBLE ) return false;
        if(lloguers.containsKey(codi)) return  false;

        vehicle.setEstat(EstatVehicle.LLOGAT);
        lloguers.put(codi, new Lloguer(codi,dies,vehicle,client));
        return true;
    }
}
