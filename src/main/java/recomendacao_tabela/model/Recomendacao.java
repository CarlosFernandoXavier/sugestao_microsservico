package recomendacao_tabela.model;

public class Recomendacao {
    private String fatorRecomendacao;
    private String apiMonolitica;
    private String microsservicos;

    public Recomendacao(String fatorRecomendacao, String apiMonolitica, String microsservicos) {
        this.fatorRecomendacao = fatorRecomendacao;
        this.apiMonolitica = apiMonolitica;
        this.microsservicos = microsservicos;
    }

    public String getFatorRecomendacao() {
        return fatorRecomendacao;
    }

    public String getApiMonolitica() {
        return apiMonolitica;
    }

    public String getMicrosservicos() {
        return microsservicos;
    }
}
