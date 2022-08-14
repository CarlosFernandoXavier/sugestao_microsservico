package recomendacao_tabela.contrato;

import java.util.List;
import java.util.Map;

public interface ILeitorService {

    void mapearFuncionalidades(Map<String, List<String>> funcionalidades,
                               Map<String, Integer> pesoFuncionalidades);
}
