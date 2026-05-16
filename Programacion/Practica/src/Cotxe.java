public class Cotxe extends  Vehicle{
    private int places;

    public Cotxe(String matricula, String marca, double preuDiari,EstatVehicle estat, int places){
        super(matricula,marca,preuDiari,estat);
        this.places=places;
    }

    @Override
    public void mostrarDetalls() {
        System.out.printf("""
                Matricula: %s,
                Marca: %s,
                Preu Diari: %f
                Estat: %s
                Places: %d
                """,matricula,marca,preuDiari,estat,places);
    }

    @Override
    public double calcularPreuBase() {
        return places>4 ? preuDiari*1.5 : preuDiari;
    }
}
