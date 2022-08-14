package recomendacao_tabela.contrato;

import recomendacao_tabela.model.Microsservico;
import recomendacao_tabela.model.Recomendacao;

import java.util.List;
import java.util.Map;

public interface IMicrosservicoService {

    List<Microsservico> getMicroservicos(Map<String, List<String>> funcionalidades,
                                         Map<String, Integer> pesoFuncionalidades,
                                         Integer fatoDecomposicao);

    Recomendacao gerarDadosRecomendacao(List<Microsservico> microsservicos,
                                        Integer fatorDecomposicao);
}
