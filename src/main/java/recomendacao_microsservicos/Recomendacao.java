package recomendacao_microsservicos;

import java.util.ArrayList;
import java.util.List;

public class Recomendacao {

    private String nome;
    private List<Classe> classes;

    public Recomendacao(String nome) {
        this.nome = nome;
        classes = new ArrayList<>();
    }

    public void adicionarClasse(Classe classe) {
        classes.add(classe);
    }

    public String getNome() {
        return nome;
    }

    public List<Classe> getClasses() {
        return classes;
    }

    public void setClasses(List<Classe> classes) {
        this.classes = classes;
    }
}
