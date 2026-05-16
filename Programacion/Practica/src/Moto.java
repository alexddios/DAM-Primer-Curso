public class Moto  extends Vehicle{
    private boolean teMaleter;

    public Moto(String matricula,String marca,double preuDiari,EstatVehicle estat,boolean teMaleter){
        super(matricula,marca,preuDiari,estat);
        this.teMaleter=teMaleter;
    }

    @Override
    public void mostrarDetalls(){
        System.out.printf("""
                Matricula: %s
                Marca: %s
                Preu Diari: %f
                Estat: %s
                Te Maleter: %b
                """,matricula,marca,preuDiari,estat,teMaleter);
    }

    @Override
    public double calcularPreuBase(){
        if (teMaleter){
            return preuDiari+10;
        }
        return preuDiari;
    }
}
