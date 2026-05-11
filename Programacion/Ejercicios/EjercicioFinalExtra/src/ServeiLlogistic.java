public abstract class ServeiLlogistic {
    protected final String codi;
    protected String nom;
    protected double costBase;

    public ServeiLlogistic(String codi,String nom,double costBase){
        this.codi=codi;
        this.nom=nom;
        this.costBase=costBase;
    }

    public abstract void mostrarInformacio();
}
