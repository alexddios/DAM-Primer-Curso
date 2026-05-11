public class Enviament {
    private String codi;
    private boolean lliurat;
    private RutaRepartiment rutaRepartiment;
    private Paquet paquet;
    private Client client;

    public Enviament(String codi,RutaRepartiment rutaRepartiment, Paquet paquet, Client client) {
        this.codi = codi;
        this.lliurat = false;
        this.rutaRepartiment = rutaRepartiment;
        this.paquet = paquet;
        this.client = client;
    }
}
