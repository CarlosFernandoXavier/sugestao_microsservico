package grafo;

import java.util.ArrayList;
import java.util.List;

public class Grafo {
    private List<Vertice> vertices; //é a caixinha
    private List<Aresta> arestas;

    public Grafo() {
        vertices = new ArrayList<>();
        arestas = new ArrayList<>();
    }

    public void adicionarVertice(String dado) {//O dado é o texto de dentro da caixa
        Vertice vertice = new Vertice(dado);
        vertices.add(vertice);
    }

    public void adicionarAresta(Integer peso, String dadoInicio, String dadoFim) {
        Vertice inicio = getVertice(dadoInicio);
        Vertice fim = getVertice(dadoFim);
        Aresta aresta = new Aresta(peso, inicio, fim);
        inicio.adicionarArestaSaida(aresta);
        fim.adicionarArestaEntrada(aresta);
        arestas.add(aresta);
    }

    public Vertice getVertice(String dado) {
        return this.vertices.stream()
                .filter(vertice -> vertice.getDado().equals(dado))
                .findFirst()
                .orElse(null);
    }

    public void buscaEmLargura() {
        List<Vertice> marcados = new ArrayList<>();
        List<Vertice> fila = new ArrayList<>();
        Vertice atual = this.vertices.get(0);
        marcados.add(atual);
        System.out.println(atual.getDado());
        fila.add(atual);
        while (fila.size() > 0) {
            Vertice visitado = fila.get(0);
            for(int i = 0; i < visitado.getArestasSaida().size();i++) {
                Vertice proximo = visitado.getArestasSaida().get(i).getFim();
                if (!marcados.contains(proximo)) {
                    marcados.add(proximo);
                    System.out.println(proximo.getDado());
                    fila.add(proximo);
                }
            }
            fila.remove(0);
        }
    }
}
