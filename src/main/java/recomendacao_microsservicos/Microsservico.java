package recomendacao_microsservicos;

import java.util.ArrayList;
import java.util.List;

public class Microsservico {
    private String nome;
    private List<Funcionalidade> funcionalidades;

    public Microsservico(String nome) {
        this.nome = nome;
        funcionalidades = new ArrayList<>();
    }

    public void adicionarFuncionalidade(Funcionalidade funcionalidade) {
        funcionalidades.add(funcionalidade);
    }

    public List<Funcionalidade> getFuncionalidades() {
        return funcionalidades;
    }
}
