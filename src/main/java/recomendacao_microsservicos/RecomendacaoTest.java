package recomendacao_microsservicos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class RecomendacaoTest {

    //Similaridade até 83%, funciona. Maior que isso, gera 1 microsserviço a mais
    private static final Integer PORCENTAGEM_SIMILARIDADE = 83;

    public static void main(String[] args) {


        List<Micro> microsservicos = new ArrayList<>();
        List<Map<String, List<Classe>>> estrutura = new ArrayList<>();

        Map<String, List<String>> funcionalidades = new HashMap<>();
        Map<String, Integer> pesoFuncionalidades = new HashMap<>();
        mapearFuncionalidades(funcionalidades, pesoFuncionalidades);
        gerarEstrutura(estrutura);

        List<String> nomeFuncionalidades = getNomeFuncionalidadesOrdenado(pesoFuncionalidades);

        List<String> classesFuncionalidadeA;
        List<String> classesFuncionalidadeB;

        Double similaridade;

        //TODO Premissa: deve haver ao menos 2 funcionalidades mapeadas
        for (int indice1 = 0; indice1 < nomeFuncionalidades.size(); indice1++) {
            classesFuncionalidadeA = funcionalidades.get(nomeFuncionalidades.get(indice1));

            for (int indice2 = indice1 + 1; indice2 < nomeFuncionalidades.size(); indice2++) {
                classesFuncionalidadeB = funcionalidades.get(nomeFuncionalidades.get(indice2));
                similaridade = getSimilaridade(classesFuncionalidadeA, classesFuncionalidadeB);
                if (similaridade >= PORCENTAGEM_SIMILARIDADE) {
                    if (funcionalidadeJaAdicionada(nomeFuncionalidades.get(indice1), microsservicos)) {
                        Integer indiceMicro = getIndiceMicro(nomeFuncionalidades.get(indice1), microsservicos);
                        Micro micro = microsservicos.get(indiceMicro);
                        microsservicos.remove(micro);
                        micro.adicionarFuncionalidade(nomeFuncionalidades.get(indice2));
                        microsservicos.add(indiceMicro, micro);
                    } else {
                        Micro micro = new Micro("Microsservico " + (microsservicos.size() + 1));
                        micro.adicionarFuncionalidade(nomeFuncionalidades.get(indice1));
                        micro.adicionarFuncionalidade(nomeFuncionalidades.get(indice2));
                        microsservicos.add(micro);
                    }
                }
            }
            if (!funcionalidadeJaAdicionada(nomeFuncionalidades.get(indice1), microsservicos)) {
                Micro micro = new Micro("Microsservico " + (microsservicos.size() + 1));
                micro.adicionarFuncionalidade(nomeFuncionalidades.get(indice1));
                microsservicos.add(micro);
            }
        }

        List<Recomendacao> recomendacao = gerarRecomendacao(estrutura, microsservicos);
        printRecomendacao(recomendacao);
        Integer a = 90;
    }

    private static void printRecomendacao(List<Recomendacao> recomendacao) {
        System.out.println("Recomendações de microsserviços:");
        ObjectMapper objectMapper = new ObjectMapper();
        recomendacao.stream().forEach(object -> {
            try {
                System.out.println(objectMapper.writeValueAsString(object));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    public static List<Recomendacao> gerarRecomendacao(List<Map<String, List<Classe>>> estrutura,
                                                       List<Micro> microsservicos) {

        List<Recomendacao> recomendacao = new ArrayList<>();

        for (int indice = 0; indice < microsservicos.size(); indice++) {
            Recomendacao m = new Recomendacao("Microsservico " +
                    (recomendacao.size() + 1));

            for(String funcionalidade : microsservicos.get(indice).getFuncionalidades()) {
                m = getEstruturaFuncionalidade(estrutura, funcionalidade, m);

            }
            recomendacao.add(m);
        }

        return recomendacao;
    }

    public static Recomendacao getEstruturaFuncionalidade(List<Map<String,
            List<Classe>>> estrutura, String funcionalidade, Recomendacao recomendacao) {

        for (Map<String, List<Classe>> estruturaMap : estrutura) {
            if (!Objects.isNull(estruturaMap.get(funcionalidade))) {
                List<Classe> classes = estruturaMap.get(funcionalidade);
                if (recomendacao.getClasses().isEmpty()) {
                    recomendacao.setClasses(classes);
                } else {
                    Integer a = 90;

                    for (Classe classeNova : classes) {
                        if (!ehClasseExistente(recomendacao, classeNova)) {
                            recomendacao.adicionarClasse(classeNova);
                        } else {
                            Integer indice = getIndiceClasseExistente(recomendacao, classeNova);
                            Classe classeSalva = recomendacao.getClasses().get(indice);

                            //buscar todos os métodos ainda não adicionados;
                            List<String> metodosNovos = getMetodosNaoAdicionados(classeSalva, classeNova.getMetodos());
                            if (!metodosNovos.isEmpty()) {
                                recomendacao.getClasses().remove(classeSalva);
                                metodosNovos.forEach(classeSalva::adicionarMetodo);
                                recomendacao.getClasses().add(indice, classeSalva);
                            }
                        }
                    }
                }
               // break;
            }
        }
        return recomendacao;
    }

    public static boolean ehClasseExistente(Recomendacao recomendacao, Classe classeNova) {
        return recomendacao.getClasses().stream()
                .filter(classe -> classe.getNomeClasse().equals(classeNova.getNomeClasse()))
                .findFirst()
                .isPresent();
    }

    public static Integer getIndiceClasseExistente(Recomendacao recomendacao, Classe classeNova) {
        Integer indice = null;
        for (int contador = 0; contador < recomendacao.getClasses().size(); contador++) {
            if (recomendacao.getClasses().get(contador).getNomeClasse().equals(classeNova.getNomeClasse())) {
                indice = contador;
                break;
            }
        }
        return indice;
    }

    public static List<String> getMetodosNaoAdicionados(Classe classe, List<String> metodosNovos) {
        List<String> metodos = new ArrayList<>();

        for (String metodoNovo : metodosNovos) {
            if (!ehMetodoAdicionado(classe, metodoNovo)) {
                metodos.add(metodoNovo);
            }
        }
        return metodos;
    }

    public static boolean ehMetodoAdicionado(Classe classe, String metodo) {
        return classe.getMetodos().stream()
                .filter(metodoSalvo -> metodoSalvo.equals(metodo))
                .findFirst()
                .isPresent();
    }


    public static Integer getIndiceMicro(String funcionalidade, List<Micro> microsservicos) {
        for (int indice = 0; indice < microsservicos.size(); indice++) {
            if (microsservicos.get(indice).getFuncionalidades().contains(funcionalidade)) {
                return indice;
            }
        }
        return null;
    }

    public static List<String> getNomeFuncionalidadesOrdenado(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        List<String> nomeFuncionalidades = new ArrayList<>();
        list.forEach(stringIntegerEntry ->
                nomeFuncionalidades.add(stringIntegerEntry.getKey()));
        return nomeFuncionalidades;
    }


    public static Boolean funcionalidadeJaAdicionada(String nomeFuncionalidade, List<Micro> microsservicos) {
        return microsservicos.stream()
                .filter(micro -> micro.getFuncionalidades().contains(nomeFuncionalidade))
                .findFirst()
                .isPresent();
    }


    //TODO estou considerando que sempre haverá ao menos 2 funcionalidades mapeadas
    public static Double getSimilaridade(List<String> classesFuncionalidadeA,
                                         List<String> classesFuncionalidadeB) {

        Integer igual = 0;
        for (String classeA : classesFuncionalidadeA) {
            if (classesFuncionalidadeB.contains(classeA)) {
                igual += 1;
            }
        }
        return Double.valueOf(igual * 100) / classesFuncionalidadeA.size();
    }

    public static Boolean jaExiste(String novaClasse, List<String> classes) {
        return classes.stream()
                .filter(classe -> classe.contains(novaClasse))
                .findFirst()
                .isPresent();
    }

    public static Boolean jaExiste(Classe novaClasse, List<Classe> classes) {
        return classes.stream()
                .filter(classe -> classe.getNomeClasse().equals(novaClasse.getNomeClasse()))
                .findFirst()
                .isPresent();
    }

    public static void mapearFuncionalidades(Map<String, List<String>> funcionalidades,
                                             Map<String, Integer> pesoFuncionalidades) {

        List<String> arquivos = List.of("src/main/resources/gerar_relatorio_por_filial.txt",
                "src/main/resources/buscar_itens.txt",
                "src/main/resources/buscar_item_pelo_id.txt",
                "src/main/resources/buscar_filiais.txt");

        List<String> classes;

        for (String nomeArquivo : arquivos) {
            File file = new File(nomeArquivo);

            String nomeFuncionalidade = file.getName().substring(0, file.getName().lastIndexOf("."));
            classes = new ArrayList<>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;

                while ((st = br.readLine()) != null) {
                    String classe = st;
                    if (classes.isEmpty()) {
                        classes.add(classe);
                    } else {
                        if (!jaExiste(classe, classes)) {
                            classes.add(classe);
                        }
                    }
                }
                pesoFuncionalidades.put(nomeFuncionalidade, classes.size());
                funcionalidades.put(nomeFuncionalidade, classes);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void gerarEstrutura(List<Map<String, List<Classe>>> funcionalidades) {

        List<String> arquivos = List.of("src/main/resources/gerar_relatorio_por_filial.txt",
                "src/main/resources/buscar_itens.txt",
                "src/main/resources/buscar_item_pelo_id.txt",
                "src/main/resources/buscar_filiais.txt");

        List<Classe> classes;
        File file;
        String nomeFuncionalidade;
        Classe classe;
        Map<String, List<Classe>> funcionalidade;

        for (String nomeArquivo : arquivos) {
            funcionalidade = new HashMap<>();
            classes = new ArrayList<>();
            file = new File(nomeArquivo);
            nomeFuncionalidade = file.getName().substring(0, file.getName().lastIndexOf("."));

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;

                while ((st = br.readLine()) != null) {

                    String linhaParcial = st.substring(0, st.lastIndexOf("("));
                    String nomeClasse = st.substring(0, linhaParcial.lastIndexOf("."));
                    String metodo = st.substring(linhaParcial.lastIndexOf(".") + 1);
                    classe = new Classe();
                    classe.setNome(nomeClasse);
                    classe.adicionarMetodo(metodo);

                    if (classes.isEmpty()) {
                        classes.add(classe);
                    } else {
                        if (!jaExiste(classe, classes)) {
                            classes.add(classe);
                        } else {
                            adicionarMetodo(classe, classes);
                        }
                    }
                }

                funcionalidade.put(nomeFuncionalidade, classes);
                funcionalidades.add(funcionalidade);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void adicionarMetodo(Classe novaClasse, List<Classe> classes) {
        Integer indiceClasse = null;
        for (int indice = 0; indice < classes.size(); indice++) {
            if (classes.get(indice).getNomeClasse().equals(novaClasse.getNomeClasse())) {
                indiceClasse = indice;
                break;
            }
        }

        Classe classeEncontrada = classes.get(indiceClasse);
        classes.remove(classeEncontrada);
        novaClasse.getMetodos().forEach(metodo -> classeEncontrada.adicionarMetodo(metodo));
        classes.add(indiceClasse, classeEncontrada);
    }
}
