package recomendacao_microsservicos;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Original {
    //A recomendação deve ser a nível de serviço (funcionalidade), de classe (quais classes compõe o microsserviço)
    // e a nível de método (quais métodos compoem o microsserviço
    //TODO me preocupo depois em descompactar uma pasta zipada
    public static void main(String[] args) {

        Map<String, List<String>> funcionalidades = new HashMap<>();
        Map<String, Integer> pesoFuncionalidades = new HashMap<>();
        mapearFuncionalidades(funcionalidades, pesoFuncionalidades);

        List<String> nomeFuncionalidades = getNomeFuncionalidadesOrdenado(pesoFuncionalidades);

        List<String> classesFuncionalidadeA;
        List<String> classesFuncionalidadeB;
        List<String> microsservicos = new ArrayList<>();
        Double similaridade;
        //TODO Premissa: deve haver ao menos 2 funcionalidades mapeadas
        for (int indice1 = 0; indice1 < nomeFuncionalidades.size(); indice1++) {
            classesFuncionalidadeA = funcionalidades.get(nomeFuncionalidades.get(indice1));

            for (int indice2 = indice1 + 1; indice2 < nomeFuncionalidades.size(); indice2++) {
                classesFuncionalidadeB = funcionalidades.get(nomeFuncionalidades.get(indice2));
                similaridade = getSimilaridade(classesFuncionalidadeA, classesFuncionalidadeB);
                if (similaridade >= 50) {
                    if (funcionalidadeJaAdicionada(nomeFuncionalidades.get(indice1), microsservicos)) {
                        Integer index = getIndice(nomeFuncionalidades.get(indice1), microsservicos);
                        adicionarMicrosservico(nomeFuncionalidades, microsservicos, index, indice2);
                    } else {
                        microsservicos.add(nomeFuncionalidades.get(indice1) + ", " + nomeFuncionalidades.get(indice2));
                    }
                }
            }
            if (!funcionalidadeJaAdicionada(nomeFuncionalidades.get(indice1), microsservicos)) {
                microsservicos.add(nomeFuncionalidades.get(indice1));
            }
        }
        Integer a = 90;
    }

    public static List<String> getNomeFuncionalidadesOrdenado(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        list.forEach(System.out::println);

        List<String> nomeFuncionalidades = new ArrayList<>();
        list.forEach(stringIntegerEntry ->
                nomeFuncionalidades.add(stringIntegerEntry.getKey()));
        return nomeFuncionalidades;
    }

    public static void adicionarMicrosservico(List<String> nomeFuncionalidades,
                                              List<String> microsservicos,
                                              Integer indice1,
                                              Integer indice2) {

        String microsservico = microsservicos.get(indice1) + ", " + nomeFuncionalidades.get(indice2);
        microsservicos.remove(microsservicos.get(indice1));
        microsservicos.add(indice1, microsservico);
    }

    public static Boolean funcionalidadeJaAdicionada(String funcionalidade, List<String> microsservicos) {
        return microsservicos.stream()
                .filter(microsservico -> microsservico.contains(funcionalidade))
                .findFirst()
                .isPresent();
    }

    public static Integer getIndice(String funcionalidade, List<String> microsservicos) {
        for (int indice = 0; indice < microsservicos.size(); indice++) {
            if (microsservicos.get(indice).contains(funcionalidade)) {
                return indice;
            }
        }
        return null;
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


    public static void lerPastaZip() {
        String fileName = "src/main/resources/arquivos.zip";
        Long MILLS_IN_DAY = 86400000L;

        try (FileInputStream fis = new FileInputStream(fileName);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipInputStream zis = new ZipInputStream(bis)) {

            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {

                System.out.format("File: %s Size: %d Last Modified %s %n",
                        ze.getName(), ze.getSize(),
                        LocalDate.ofEpochDay(ze.getTime() / MILLS_IN_DAY));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void mapearFuncionalidades(Map<String, List<String>> funcionalidades,
                                             Map<String, Integer> pesoFuncionalidades) {

        List<String> arquivos = List.of("src/main/resources/gerar_relatorio_por_filial.txt", "src/main/resources/buscar_itens.txt",
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
                    /*TODO removo os métodos da classe
                    String linhaParcial = st.substring(0, st.lastIndexOf("("));
                    String classe = st.substring(0, linhaParcial.lastIndexOf("."));

                     */
                    String classe = st;
                    System.out.println(classe);
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

    public static void descompactarZip() {
        byte[] buffer = new byte[2048];

        Path outDir = Paths.get("C:\\Users\\carlo\\Documents\\teste-tcc");
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
        }
    }
}
