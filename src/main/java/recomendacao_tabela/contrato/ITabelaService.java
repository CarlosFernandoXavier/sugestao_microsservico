package recomendacao_tabela.contrato;

import recomendacao_tabela.model.Recomendacao;

import java.util.List;

public interface ITabelaService {

    void gerarTabela(List<Recomendacao> recomendacaos);
}
