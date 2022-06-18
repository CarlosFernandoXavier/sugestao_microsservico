package recomendacao_microsservicos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recomendacao {

    private String nome;
    private List<Map<String, List<Classe>>> funcionalidades;

    public Recomendacao(String nome) {
        this.nome = nome;
        funcionalidades = new ArrayList<>();
    }

    public void adicionarFuncionalidade(String funcionalidade, List<Classe> classes) {
        Map<String, List<Classe>> map = new HashMap<>();
        map.put(funcionalidade, classes);
        funcionalidades.add(map);
    }

    public void adicionarFuncionalidade(Map<String, List<Classe>> map) {
        funcionalidades.add(map);
    }

    public String getNome() {
        return nome;
    }

    public List<Map<String, List<Classe>>> getFuncionalidades() {
        return funcionalidades;
    }
}
