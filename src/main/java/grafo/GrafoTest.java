package grafo;

public class GrafoTest {
    public static void main(String[] args) {
        Grafo grafo = new Grafo();
        grafo.adicionarVertice("João");
        grafo.adicionarVertice("Lorenzo");
        grafo.adicionarVertice("Creuza");
        grafo.adicionarVertice("Cléber");
        grafo.adicionarVertice("Cláudio");

        grafo.adicionarAresta(2, "João", "Lorenzo");
        grafo.adicionarAresta(1, "João", "Creuza");
        grafo.adicionarAresta(3, "Cláudio", "João");
        grafo.adicionarAresta(2, "Cláudio", "Lorenzo");
        grafo.adicionarAresta(3, "Lorenzo", "Cléber");
        grafo.adicionarAresta(1, "Cléber", "Creuza");
        grafo.buscaEmLargura();

    }
}
