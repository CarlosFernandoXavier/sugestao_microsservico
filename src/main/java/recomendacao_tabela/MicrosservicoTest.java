package recomendacao_tabela;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MicrosservicoTest {
    /*
     * Seria interessante começar a usar o spring, pois assim poderia direcionar para uma
     * página e mostrar o PDF com a tabela contendo as recomendações
     * E poderia implementar o sistema lendo um .zip para pegar os traces.
     * Mas isso devo fazer depois que gerar as recomendações em tabela
     * */

    public static void main(String[] args) {
        List<Recomendacao> recomendacoes = new ArrayList<>();
        List<Integer> fatorRecomendacoes = List.of(20, 40, 60, 80, 90);

        for (Integer fatorRecomendacao : fatorRecomendacoes) {
            List<Microsservico> microsservicos = getMicroservicos(fatorRecomendacao);
            Recomendacao recomendacao = gerarDadosRecomendacao(microsservicos, fatorRecomendacao);
            recomendacoes.add(recomendacao);
        }
        gerarTabela(recomendacoes);
    }


    private static Recomendacao gerarDadosRecomendacao(List<Microsservico> microsservicos,
                                                       Integer fatorDecomposicao) {

        Microsservico apiMonolitica = getApiMonolitica(microsservicos);
        List<Microsservico> microssevico = getMicrosservicos(microsservicos, apiMonolitica);

        String servicosApiMonolitica = String.format("[%s]", apiMonolitica.getServicos());
        String microRecomendacao = "";
        if (microssevico.isEmpty()) {
            microRecomendacao = "Zero";
        } else {
            for (Microsservico m : microssevico) {
                microRecomendacao = microRecomendacao.isEmpty() ? microRecomendacao.concat("[") : microRecomendacao.concat(", [");
                microRecomendacao = microRecomendacao.concat(m.getServicos());
                microRecomendacao = microRecomendacao.concat("]");
            }
        }
        String resultado = String.format("%.2f", fatorDecomposicao / 100.00);
        return new Recomendacao(resultado, servicosApiMonolitica, microRecomendacao);
    }

    private static List<Microsservico> getMicroservicos(Integer fatoRecomendacao) {

        List<Microsservico> microList = new ArrayList<>();
        Map<String, List<String>> funcionalidades = new HashMap<>();
        Map<String, Integer> pesoFuncionalidades = new HashMap<>();
        mapearFuncionalidades(funcionalidades, pesoFuncionalidades);


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
                if (similaridade >= fatoRecomendacao) {
                    Integer indiceMicrosservico = ehFuncionalidadeExistente(microList, nomeFuncionalidades.get(indice1));
                    Integer indiceMicrosservico2 = ehFuncionalidadeExistente(microList, nomeFuncionalidades.get(indice2));
                    if (Objects.nonNull(indiceMicrosservico) && Objects.isNull(indiceMicrosservico2)) {
                        Microsservico servicosAux = microList.get(indiceMicrosservico);
                        microList.remove(servicosAux);
                        servicosAux.adicionarQuantidade(1);
                        servicosAux.setServicos(servicosAux.getServicos() + ", " + nomeFuncionalidades.get(indice2));
                        microList.add(indiceMicrosservico, servicosAux);

                        if (servicosAux.getQuantidadeServicos().equals(nomeFuncionalidades.size())) {
                            quebrarLaco = true;
                            break;
                        }

                    } else if (Objects.isNull(indiceMicrosservico) && Objects.isNull(indiceMicrosservico2)) {
                        Microsservico m = new Microsservico(nomeFuncionalidades.get(indice1) + ", "
                                + nomeFuncionalidades.get(indice2), 2);
                        microList.add(m);
                    }
                }
            }

            if (!quebrarLaco) {
                Boolean contemServico = false;

                for (int contador = 0; contador < microList.size(); contador++) {
                    if (microList.get(contador).getServicos().contains(nomeFuncionalidades.get(indice1))) {
                        contemServico = true;
                        break;
                    }
                }

                if (!contemServico) {
                    Microsservico m = new Microsservico(nomeFuncionalidades.get(indice1), 1);
                    microList.add(m);
                }
                indice1++;
            }
        }
        return microList;
    }

    private static Integer ehFuncionalidadeExistente(List<Microsservico> micro, String servico) {
        Integer indiceMicrosservico = null;
        for (int contador = 0; contador < micro.size(); contador++) {
            if (micro.get(contador).getServicos().contains(servico)) {
                indiceMicrosservico = contador;
                break;
            }
        }
        return indiceMicrosservico;
    }

    private static void gerarTabela(List<Recomendacao> recomendacaos) {

        try {
            String pasta = "pdf";
            Path path = Paths.get(FileSystemView.getFileSystemView()
                            .getHomeDirectory()
                            .getAbsolutePath(),
                    pasta);
            if (!Files.exists(path)) {
                path.toFile().mkdir();
            }

            String dest = path.toAbsolutePath() + "/relatorio2.pdf";

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dest));
            Document doc = new Document(pdfDoc);
            Table table = new Table(UnitValue.createPercentArray(new float[]{17, 17, 17}));
            table.setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20);

            //TODO tenho problema com acentos usando itext 7 - resolver no final
            table.addHeaderCell(celulaComBorda("Fator de decomposicao", TextAlignment.CENTER));
            table.addHeaderCell(celulaComBorda("API Monolitica", TextAlignment.CENTER));
            table.addHeaderCell(celulaComBorda("Microsservicos", TextAlignment.CENTER));

            for (Recomendacao recomendacao : recomendacaos) {
                table.addCell(celulaComBorda(">= " + recomendacao.getFatorRecomendacao(), TextAlignment.CENTER));
                table.addCell(celulaComBorda(recomendacao.getApiMonolitica(), TextAlignment.CENTER));
                table.addCell(celulaComBorda(recomendacao.getMicrosservicos(), TextAlignment.CENTER));
            }

            doc.add(table);
            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


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

    public static String converteEncode(String content) {
        Charset charset = StandardCharsets.UTF_8;
        ByteBuffer bb = charset.encode(content);
        return new String(bb.array());
    }

    private static Cell celulaComBorda(String texto, TextAlignment alinnhamento) {
        Paragraph paragrafo = new Paragraph(String.format("%s", texto))
                .setTextAlignment(alinnhamento);

       /* Paragraph paragrafo = new Paragraph(converteEncode(texto))
                .setTextAlignment(alinnhamento);*/
        return new Cell().add(paragrafo);
    }

    private static void mapearFuncionalidades(Map<String, List<String>> funcionalidades,
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

    private static Boolean jaExiste(String novaClasse, List<String> classes) {
        return classes.stream()
                .filter(classe -> classe.contains(novaClasse))
                .findFirst()
                .isPresent();
    }

    public static List<String> getNomeFuncionalidadesOrdenado(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        List<String> nomeFuncionalidades = new ArrayList<>();
        list.forEach(stringIntegerEntry ->
                nomeFuncionalidades.add(stringIntegerEntry.getKey()));
        return nomeFuncionalidades;
    }

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

/*    public static Boolean funcionalidadeJaAdicionada(String nomeFuncionalidade, List<Micro> microsservicos) {
        return microsservicos.stream()
                .filter(micro -> micro.getFuncionalidades().contains(nomeFuncionalidade))
                .findFirst()
                .isPresent();
    }*/

}