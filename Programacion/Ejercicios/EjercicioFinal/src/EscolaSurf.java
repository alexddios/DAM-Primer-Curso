import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EscolaSurf {
    private String nom;
    private EstatEscola estat;

    private List<Monitor> monitors;
    private Map<String,Inscripcio> inscripcions;
    private List<Platja> platges;

    public EscolaSurf(String nom, EstatEscola estat){
        this.nom=nom;
        this.estat=estat;
        this.monitors= new ArrayList<>();
        this.inscripcions= new HashMap<>();
        this.platges = new ArrayList<>();
    }
    public Platja getPlatja(int posicio){
        if (validarPosicio(posicio)) return platges.get(posicio);
        return null;
    }

    private boolean validarPosicio(int posicio) {
        return posicio < platges.size() && posicio >= 0;
    }

    public Inscripcio buscarInscripcio(String codi){
        return inscripcions.get(codi);
    }



    public boolean eliminarInscripcio(String codi){
        if (validarInscripcio(codi)){
            inscripcions.remove(codi);
            return true;
        }
        return false;
    }
    private boolean validarInscripcio(String codi) {
        return inscripcions.containsKey(codi);
    }
    private boolean inscriure(
            String codiInscripcio,
            ClasseSurf classeSurf,
            ArrayList<Alumne> alumnes
    ){
        if(!validarEscola(codiInscripcio)) return false;

        Inscripcio inscripcio = new Inscripcio(codiInscripcio,classeSurf);

        for (Alumne a : alumnes){
            inscripcio.afegirAlumne(a);
        }
        this.inscripcions.put(codiInscripcio,inscripcio);
        return true;
    }
    private boolean validarEscola(String codi){
        if(this.estat != EstatEscola.OBERTA){
            return false;
        }
        if(buscarInscripcio(codi) != null){
            return false;
        }
        return true;
    }
}
