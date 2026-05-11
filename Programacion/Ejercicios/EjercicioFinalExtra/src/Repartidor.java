public class Repartidor {
    private final String dni;
    private String nom;
    private String zona;

    public Repartidor(String dni,String nom,String zona){
        this.dni=dni;
        this.nom=nom;
        this.zona=zona;
    }

    @Override
    public String toString() {
        return "Repartidor{" +
                "dni='" + dni + '\'' +
                ", nom='" + nom + '\'' +
                ", zona='" + zona + '\'' +
                '}';
    }
}
