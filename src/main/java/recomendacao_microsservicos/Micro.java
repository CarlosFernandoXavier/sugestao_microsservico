package recomendacao_microsservicos;

import java.util.ArrayList;
import java.util.List;

public class Micro {
    private String nome;
    private List<String> funcionalidades;

    public Micro(String nome) {
        this.nome = nome;
        funcionalidades = new ArrayList<>();
    }

    public void adicionarFuncionalidade(String funcionalidade) {
        funcionalidades.add(funcionalidade);
    }

    public List<String> getFuncionalidades() {
        return funcionalidades;
    }
}
