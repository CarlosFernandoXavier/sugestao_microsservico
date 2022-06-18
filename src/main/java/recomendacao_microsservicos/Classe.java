package recomendacao_microsservicos;

import java.util.ArrayList;
import java.util.List;

public class Classe {
    private String nome;
    private List<String> metodo;

    public Classe() {
        nome = null;
        metodo = new ArrayList<>();
    }

    public List<String> getMetodos() {
        return metodo;
    }

    public void adicionarMetodo(String metodo) {
        this.metodo.add(metodo);
    }

    public String getNomeClasse() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
