import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmpresaMissatgeria {
    private String nom;
    private EstatEmpresa estat;
    private final Map<String,Enviament> enviaments;
    private final List<Vehicle> vehicles;
    private final List<Repartidor> repartidors;

    public EmpresaMissatgeria(String nom, EstatEmpresa estat) {
    this.nom=nom;
    this.estat=estat;
    this.enviaments= new HashMap<>();
    this.vehicles = new ArrayList<>();
    this.repartidors = new ArrayList<>();
    }

    public Vehicle getVehicle(int posicio){
        if(posicio>=0 && posicio<vehicles.size()) return vehicles.get(posicio);
        return null;
    }
    public Enviament buscarEnviament(String codi){
        return enviaments.get(codi);
    }
    public boolean eliminarEnviament(String codi){
        return enviaments.remove(codi) != null;
    }
    public boolean crearEnviament(String codiEnviament,Client client,Paquet paquet,RutaRepartiment ruta){
        if(this.estat== EstatEmpresa.OBERTA && buscarEnviament(codiEnviament)==null){
            enviaments.put(codiEnviament,new Enviament(codiEnviament,ruta,paquet,client));
            return true;
        }
        return false;
    }
}
