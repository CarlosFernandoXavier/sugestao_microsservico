package recomendacao_tabela.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import recomendacao_tabela.model.Recomendacao;
import recomendacao_tabela.contrato.ITabelaService;

import javax.swing.filechooser.FileSystemView;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TabelaService implements ITabelaService {

    public void gerarTabela(List<Recomendacao> recomendacoes) {

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

            montarCabecalhoTabela(table);
            montarCorpoTabela(table, recomendacoes);

            doc.add(table);
            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Cell celulaComBorda(String texto, TextAlignment alinnhamento) {
        Paragraph paragrafo = new Paragraph(String.format("%s", texto))
                .setTextAlignment(alinnhamento);
        return new Cell().add(paragrafo);
    }

    private void montarCabecalhoTabela(Table table) {
        //TODO tenho problema com acentos usando itext 7 - resolver no final
        table.addHeaderCell(celulaComBorda("Fator de decomposicao", TextAlignment.CENTER));
        table.addHeaderCell(celulaComBorda("API Monolitica", TextAlignment.CENTER));
        table.addHeaderCell(celulaComBorda("Microsservicos", TextAlignment.CENTER));
    }

    private void montarCorpoTabela(Table table, List<Recomendacao> recomendacoes) {
        for (Recomendacao recomendacao : recomendacoes) {
            table.addCell(celulaComBorda(">= " + recomendacao.getFatorRecomendacao(), TextAlignment.CENTER));
            table.addCell(celulaComBorda(recomendacao.getApiMonolitica(), TextAlignment.CENTER));
            table.addCell(celulaComBorda(recomendacao.getMicrosservicos(), TextAlignment.CENTER));
        }
    }
}
