package recomendacao_tabela.service;

import recomendacao_tabela.model.Microsservico;
import recomendacao_tabela.model.Recomendacao;
import recomendacao_tabela.contrato.IMicrosservicoService;

import java.util.*;

public class MicrosservicoService implements IMicrosservicoService {

    public List<Microsservico> getMicroservicos(Map<String, List<String>> funcionalidades,
                                                Map<String, Integer> pesoFuncionalidades,
                                                Integer fatoDecomposicao) {

        List<Microsservico> microsservicos = new ArrayList<>();
        List<String> nomeFuncionalidades = getNomeFuncionalidadesOrdenado(pesoFuncionalidades);
        List<String> classesFuncionalidadeA;
        List<String> classesFuncionalidadeB;

        Double similaridade;

        Boolean quebrarLaco = false;
        int indice1 = 0;
        while (indice1 < nomeFuncionalidades.size() && !quebrarLaco) {
            classesFuncionalidadeA = funcionalidades.get(nomeFuncionalidades.get(indice1));

            for (int indice2 = indice1 + 1; indice2 < nomeFuncionalidades.size(); indice2++) {
                classesFuncionalidadeB = funcionalidades.get(nomeFuncionalidades.get(indice2));
                similaridade = getSimilaridade(classesFuncionalidadeA, classesFuncionalidadeB);
                if (similaridade >= fatoDecomposicao) {

                    Integer indiceMicrosservico = getIndiceFuncionalidadeExistente(microsservicos,
                            nomeFuncionalidades.get(indice1));

                    Integer indiceMicrosservico2 = getIndiceFuncionalidadeExistente(microsservicos,
                            nomeFuncionalidades.get(indice2));

                    if (Objects.nonNull(indiceMicrosservico) && Objects.isNull(indiceMicrosservico2)) {
                        Microsservico microsservico = microsservicos.get(indiceMicrosservico);
                        microsservicos.remove(microsservico);
                        microsservico.adicionarQuantidade(1);
                        microsservico.setServicos(microsservico.getServicos() + ", " + nomeFuncionalidades.get(indice2));
                        microsservicos.add(indiceMicrosservico, microsservico);

                        if (microsservico.getQuantidadeServicos().equals(nomeFuncionalidades.size())) {
                            quebrarLaco = true;
                            break;
                        }

                    } else if (Objects.isNull(indiceMicrosservico) && Objects.isNull(indiceMicrosservico2)) {
                        Microsservico microsservico = new Microsservico(nomeFuncionalidades.get(indice1) + ", "
                                + nomeFuncionalidades.get(indice2), 2);
                        microsservicos.add(microsservico);
                    }
                }
            }

            if (!quebrarLaco) {
                Boolean contemServico = false;

                for (int contador = 0; contador < microsservicos.size(); contador++) {
                    if (microsservicos.get(contador).getServicos().contains(nomeFuncionalidades.get(indice1))) {
                        contemServico = true;
                        break;
                    }
                }

                if (!contemServico) {
                    Microsservico microsservico = new Microsservico(nomeFuncionalidades.get(indice1), 1);
                    microsservicos.add(microsservico);
                }
                indice1++;
            }
        }
        return microsservicos;
    }

    public Recomendacao gerarDadosRecomendacao(List<Microsservico> microsservicos,
                                               Integer fatorDecomposicao) {

        Microsservico apiMonolitica = getApiMonolitica(microsservicos);
        List<Microsservico> microssevico = getMicrosservicos(microsservicos, apiMonolitica);

        String servicosApiMonolitica = String.format("[%s]", apiMonolitica.getServicos());
        String recomendacaoMicrosservico = "";
        if (microssevico.isEmpty()) {
            recomendacaoMicrosservico = "Zero";
        } else {
            for (Microsservico m : microssevico) {
                recomendacaoMicrosservico = recomendacaoMicrosservico.isEmpty() ? recomendacaoMicrosservico.concat("[")
                        : recomendacaoMicrosservico.concat(", [");
                recomendacaoMicrosservico = recomendacaoMicrosservico.concat(m.getServicos());
                recomendacaoMicrosservico = recomendacaoMicrosservico.concat("]");
            }
        }
        String resultado = String.format("%.2f", fatorDecomposicao / 100.00);
        return new Recomendacao(resultado, servicosApiMonolitica, recomendacaoMicrosservico);
    }

    private static Microsservico getApiMonolitica(List<Microsservico> microsservicos) {
        Microsservico escolhido = microsservicos.get(0);
        for (int contador = 1; contador < microsservicos.size(); contador++) {
            if (escolhido.getQuantidadeServicos() < microsservicos.get(contador).getQuantidadeServicos()) {
                escolhido = microsservicos.get(contador);
            }
        }
        return escolhido;
    }

    private static List<Microsservico> getMicrosservicos(List<Microsservico> microsservicos,
                                                         Microsservico microsservico) {
        microsservicos.remove(microsservico);
        if (microsservicos.size() == 0) return Collections.emptyList();
        return microsservicos;
    }

    private Double getSimilaridade(List<String> classesFuncionalidadeA,
                                   List<String> classesFuncionalidadeB) {

        Integer igual = 0;
        for (String classeA : classesFuncionalidadeA) {
            if (classesFuncionalidadeB.contains(classeA)) {
                igual += 1;
            }
        }
        return Double.valueOf(igual * 100) / classesFuncionalidadeA.size();
    }

    private Integer getIndiceFuncionalidadeExistente(List<Microsservico> microsservicos, String servico) {
        Integer indiceMicrosservico = null;
        for (int contador = 0; contador < microsservicos.size(); contador++) {
            if (microsservicos.get(contador).getServicos().contains(servico)) {
                indiceMicrosservico = contador;
                break;
            }
        }
        return indiceMicrosservico;
    }

    private List<String> getNomeFuncionalidadesOrdenado(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        List<String> nomeFuncionalidades = new ArrayList<>();
        list.forEach(stringIntegerEntry ->
                nomeFuncionalidades.add(stringIntegerEntry.getKey()));
        return nomeFuncionalidades;
    }
}
