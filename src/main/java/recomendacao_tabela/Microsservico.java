package recomendacao_tabela;

public class Microsservico {

    private String servicos;
    private Integer quantidadeServicos;

    public Microsservico() {
        quantidadeServicos = 0;
    }

    public Microsservico(String servicos, Integer quantidadeServicos) {
        this.servicos = servicos;
        this.quantidadeServicos = quantidadeServicos;
    }

    public String getServicos() {
        return servicos;
    }

    public void setServicos(String servicos) {
        this.servicos = servicos;
    }

    public Integer getQuantidadeServicos() {
        return quantidadeServicos;
    }

    public void adicionarQuantidade(Integer quantidadeServicos) {
        this.quantidadeServicos = this.quantidadeServicos + quantidadeServicos;
    }
}
