package recomendacao_tabela;

import recomendacao_tabela.contrato.ILeitorService;
import recomendacao_tabela.contrato.IMicrosservicoService;
import recomendacao_tabela.contrato.ITabelaService;
import recomendacao_tabela.model.Microsservico;
import recomendacao_tabela.model.Recomendacao;
import recomendacao_tabela.service.LeitorService;
import recomendacao_tabela.service.MicrosservicoService;
import recomendacao_tabela.service.TabelaService;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicrosservicoTest {
    /*
     * Seria interessante começar a usar o spring, pois assim poderia direcionar para uma
     * página e mostrar o PDF com a tabela contendo as recomendações
     * E poderia implementar o sistema lendo um .zip para pegar os traces.
     * Mas isso devo fazer depois que gerar as recomendações em tabela
     * */

    public static void main(String[] args) {
        List<Recomendacao> recomendacoes = new ArrayList<>();
        List<Integer> fatorDecomposicoes = List.of(20, 40, 60, 80, 90);

        Map<String, List<String>> funcionalidades = new HashMap<>();
        Map<String, Integer> pesoFuncionalidades = new HashMap<>();

        ILeitorService leitorService = new LeitorService();
        leitorService.mapearFuncionalidades(funcionalidades, pesoFuncionalidades);

        IMicrosservicoService microsservicoService = new MicrosservicoService();

        for (Integer fatorDecomposicao : fatorDecomposicoes) {
            List<Microsservico> microsservicos = microsservicoService.getMicroservicos(funcionalidades,
                    pesoFuncionalidades,
                    fatorDecomposicao);

            Recomendacao recomendacao = microsservicoService.gerarDadosRecomendacao(microsservicos, fatorDecomposicao);
            recomendacoes.add(recomendacao);
        }
        ITabelaService tabelaService = new TabelaService();
        tabelaService.gerarTabela(recomendacoes);
    }

    public static String converteEncode(String content) {
        Charset charset = StandardCharsets.UTF_8;
        ByteBuffer bb = charset.encode(content);
        return new String(bb.array());
    }









/*    public static Boolean funcionalidadeJaAdicionada(String nomeFuncionalidade, List<Micro> microsservicos) {
        return microsservicos.stream()
                .filter(micro -> micro.getFuncionalidades().contains(nomeFuncionalidade))
                .findFirst()
                .isPresent();
    }*/

}