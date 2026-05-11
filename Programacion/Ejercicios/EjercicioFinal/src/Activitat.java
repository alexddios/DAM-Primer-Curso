public abstract class Activitat {
    protected final String dni;
    protected String nom;
    protected int duradaMinuts;

    public Activitat(String dni,String nom,int duradaMinuts){
        this.dni=dni;
        this.nom=nom;
        this.duradaMinuts=duradaMinuts;
    }

    public abstract void mostrarInformacio();
}
