package recomendacao_tabela.service;

import recomendacao_tabela.contrato.ILeitorService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeitorService implements ILeitorService {

    //TODO esta classe vou precisar mexer para ler uma pasta zipada contendo as funcionalidades

    public void mapearFuncionalidades(Map<String, List<String>> funcionalidades,
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

    private Boolean jaExiste(String novaClasse, List<String> classes) {
        return classes.stream()
                .filter(classe -> classe.contains(novaClasse))
                .findFirst()
                .isPresent();
    }
}
