import java.util.ArrayList;
import java.util.List;

public class Inscripcio implements Inscrivible{
    private final String codi;
    private boolean pagada;
    private final List<Alumne> alumnes;
    private ClasseSurf classeSurf;

    public Inscripcio(String codi, ClasseSurf classeSurf){
        this.codi=codi;
        this.pagada=false;
        this.alumnes= new ArrayList<>();
        this.classeSurf= classeSurf;
    }
    public void afegirAlumne(Alumne a){
        alumnes.add(a);
    }
    public boolean eliminarAlumne(int posicio){
        if(!validarAlumne(posicio)) return false;
        alumnes.remove(posicio);
        return true;
    }
    private boolean validarAlumne(int pos){
        if (!this.classeSurf.permetBaixa()) {
            return false;
        }
        if (alumnes == null || pos < 0 || pos >= alumnes.size()) {
            return false;
        }
        return true;
    }
}
