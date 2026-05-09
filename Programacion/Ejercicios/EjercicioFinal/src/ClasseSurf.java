public class ClasseSurf extends Activitat{
    private String hora;
    private double preuBase;
    private NivellSurf nivell;
    private Platja platja;
    private Monitor monitor;

    public ClasseSurf(
            String dni,String nom,int duradaMinuts,
            String hora,double preuBase,NivellSurf nivell,Platja platja,Monitor monitor){
        super(dni,nom,duradaMinuts);
        this.hora = hora;
        this.preuBase = preuBase;
        this.nivell=nivell;
        this.platja=platja;
        this.monitor=monitor;
    }

    @Override
    public void mostrarInformacio() {
        System.out.printf("""
                Atribut heretat: %s
                Atribut propi: %s
                """,nom,hora);
    }
    public double calcularPreuFinal(){
        switch (this.nivell){
            case INICIACIO -> {
                return preuBase;
            }
            case INTERMEDI -> {
                return preuBase+5;
            }
            case AVANCAT -> {
                return preuBase+10;
            }
        }
        return 0;
    }
    public boolean permetBaixa(){
        return this.nivell != NivellSurf.AVANCAT;
    }
}
