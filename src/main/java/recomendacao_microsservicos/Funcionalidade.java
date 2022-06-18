package recomendacao_microsservicos;

import java.util.List;

public class Funcionalidade {
    private String nome;
    private List<Classe> classes;

    public List<Classe> getClasses() {
        return classes;
    }

    public void setClasses(List<Classe> classes) {
        this.classes = classes;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String funcionalidade) {
        this.nome = funcionalidade;
    }
}
