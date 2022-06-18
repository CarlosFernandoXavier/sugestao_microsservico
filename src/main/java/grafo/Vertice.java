package grafo;

import java.util.ArrayList;
import java.util.List;

public class Vertice {
    private String dado;
    private List<Aresta> arestasEntrada;
    private List<Aresta> arestasSaida;

    public Vertice(String valor) {
        dado = valor;
        arestasEntrada = new ArrayList<>();
        arestasSaida = new ArrayList<>();
    }

    public String getDado() {
        return dado;
    }

    public void setDado(String dado) {
        this.dado = dado;
    }

    public void adicionarArestaEntrada(Aresta aresta) {
        this.arestasEntrada.add(aresta);
    }

    public void adicionarArestaSaida(Aresta aresta) {
        this.arestasSaida.add(aresta);
    }

    public List<Aresta> getArestasEntrada() {
        return arestasEntrada;
    }

    public void setArestasEntrada(List<Aresta> arestasEntrada) {
        this.arestasEntrada = arestasEntrada;
    }

    public List<Aresta> getArestasSaida() {
        return arestasSaida;
    }

    public void setArestasSaida(List<Aresta> arestasSaida) {
        this.arestasSaida = arestasSaida;
    }
}
