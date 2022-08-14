package recomendacao_tabela;

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

    public void setFatorRecomendacao(String fatorRecomendacao) {
        this.fatorRecomendacao = fatorRecomendacao;
    }

    public String getApiMonolitica() {
        return apiMonolitica;
    }

    public void setApiMonolitica(String apiMonolitica) {
        this.apiMonolitica = apiMonolitica;
    }

    public String getMicrosservicos() {
        return microsservicos;
    }

    public void setMicrosservicos(String microsservicos) {
        this.microsservicos = microsservicos;
    }
}
