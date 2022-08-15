package recomendacao_tabela;

import recomendacao_tabela.contrato.ILeitorService;
import recomendacao_tabela.contrato.IMicrosservicoService;
import recomendacao_tabela.contrato.ITabelaService;
import recomendacao_tabela.model.Microsservico;
import recomendacao_tabela.model.Recomendacao;
import recomendacao_tabela.service.LeitorService;
import recomendacao_tabela.service.MicrosservicoService;
import recomendacao_tabela.service.TabelaService;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MicrosservicoTest {
    private final static Long MILLS_IN_DAY = 86400000L;
    /*
     * Seria interessante começar a usar o spring, pois assim poderia direcionar para uma
     * página e mostrar o PDF com a tabela contendo as recomendações
     * E poderia implementar o sistema lendo um .zip para pegar os traces.
     * Mas isso devo fazer depois que gerar as recomendações em tabela
     * */

    private static List<String> getPathArquivos(String diretorioDestino, List<String> nomeArquivos) {
        List<String> pathArquivos = new ArrayList<>();
        nomeArquivos.forEach(nome -> pathArquivos.add(diretorioDestino + "/" + nome));
        return pathArquivos;
    }

    public static void main(String[] args) {
        String zipFilePath = "src/main/resources/arquivos.zip"; //TODO diretório será passado pelo cliente
        String diretorioDestino = "src/main/resources/output";
        List<String> nomeArquivos = new ArrayList<>();
        descompactar(zipFilePath, diretorioDestino, nomeArquivos);
        List<String> pathArquivos = getPathArquivos(diretorioDestino, nomeArquivos);

        Map<String, List<String>> funcionalidades = new HashMap<>();
        Map<String, Integer> pesoFuncionalidades = new HashMap<>();

        ILeitorService leitorService = new LeitorService();
        leitorService.mapearFuncionalidades(pathArquivos, funcionalidades, pesoFuncionalidades);

        IMicrosservicoService microsservicoService = new MicrosservicoService();

        List<Recomendacao> recomendacoes = new ArrayList<>();
        List<Integer> fatorDecomposicoes = List.of(20, 40, 60, 80, 90);

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

    private static void descompactar(String zipFilePath, String destDir, List<String> nomeArquivos) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                nomeArquivos.add(fileName);
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static void lerPastaZipada() {
        String fileName = "src/main/resources/arquivos.zip";

        byte[] buffer = new byte[2048];

        Path outDir = Paths.get("src/main/resources/");
        try (FileInputStream fis = new FileInputStream(fileName);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipInputStream zis = new ZipInputStream(bis)) {

            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {

                Path filePath = outDir.resolve(ze.getName());
                try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                     BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                }


                System.out.println(String.format("File: %s Size: %d last modified July 15, 2022", ze.getName(), ze.getSize(),
                        LocalDate.ofEpochDay(ze.getTime() / MILLS_IN_DAY)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*byte[] buffer = new byte[2048];

        Path outDir = Paths.get("src/main/resources/output/");
        String zipFileName = "src/main/resources/arquivos.zip";

        try (FileInputStream fis = new FileInputStream(zipFileName);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipInputStream stream = new ZipInputStream(bis)) {

            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {

                Path filePath = outDir.resolve(entry.getName());

                try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                     BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {

                    int len;
                    while ((len = stream.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static String converteEncode(String content) {
        Charset charset = StandardCharsets.UTF_8;
        ByteBuffer bb = charset.encode(content);
        return new String(bb.array());
    }

}