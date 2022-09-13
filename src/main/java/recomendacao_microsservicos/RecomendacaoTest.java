package recomendacao_microsservicos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import recomendacao_microsservicos.response.ClassResponse;
import recomendacao_microsservicos.response.MethodResponse;
import recomendacao_microsservicos.response.Microservice;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RecomendacaoTest {

    private static final Integer LIMITE_DECOMPOSICAO = 50;
    private final static String DIRETORIO_LEITURA = "src/main/resources/arquivos.zip";
    //private final static String DIRETORIO_LEITURA = "src/main/resources/trace-shopping-cart.zip";
    //private final static String DIRETORIO_LEITURA = "src/main/resources/trace-blog-api.zip";

    public static void main(String[] args) {
        List<String> arquivos = buscarNomeArquivos(DIRETORIO_LEITURA);
        //TODO pacotes que serão considerados
      /*  List<String> packages = List.of("me.zhulin.shopapi.api",
                "me.zhulin.shopapi.service.impl");*/

        List<String> packages = List.of("com.unisinos.sistema.adapter.inbound.controller",
                "com.unisinos.sistema.application.service",
                "com.unisinos.sistema.adapter.outbound.repository");

      /*  List<String> packages = List.of("com.springboot.blog.controller",
                "com.springboot.blog.service.impl");*/

        Map<String, List<Class>> functionalities = convertFunctionalityFiles(arquivos, packages);
        Map<String, List<Column>> similarityTable = createSimilarityTable(functionalities);
        List<Microservice> microsservicos = groupFunctionalitiesBySimilatiry(similarityTable, functionalities);
        printMicrosservices(microsservicos);

    }

    private static List<Microservice> groupFunctionalitiesBySimilatiry(Map<String, List<Column>> similarityTable,
                                                                       Map<String, List<Class>> funcionalidadesMap) {
        List<String> similarities = new ArrayList<>();
        List<Microservice> microservices = new ArrayList<>();

        similarityTable.forEach((row, columns) -> {
            String functionalities = row;
            List<Column> colunasFiltradas = columns.stream()
                    .filter(column -> column.getThreshold() >= LIMITE_DECOMPOSICAO)
                    .collect(Collectors.toList());

            if (similarities.isEmpty()) {
                List<ClassResponse> classResponses = new ArrayList<>();
                gerarClassesParaMicrosservico(funcionalidadesMap, functionalities,
                        classResponses);

                for (Column colunaFiltrada : colunasFiltradas) {
                    gerarClassesParaMicrosservico(funcionalidadesMap, colunaFiltrada.getNomeFuncionalidade(),
                            classResponses);
                    functionalities += ", " + colunaFiltrada.getNomeFuncionalidade();
                }

                Microservice micro = new Microservice();
                micro.setId("Microservice " + (microservices.size() + 1));
                micro.setFunctionalities(functionalities);
                micro.setClasses(classResponses);
                microservices.add(micro);
                similarities.add(functionalities);
            } else {
                //PAra cada coluna, preciso ver se a funcionalidade já não existe em algum microsserviço
                Integer indiceMicrosservico1 = getIndiceMicrosservico(similarities, functionalities);
                Integer indiceMicrosservico2 = getIndiceMicrosservico(similarities, colunasFiltradas);

                if (Objects.isNull(indiceMicrosservico1) && Objects.isNull(indiceMicrosservico2)) {
                    List<ClassResponse> classResponses = new ArrayList<>();
                    gerarClassesParaMicrosservico(funcionalidadesMap, functionalities, classResponses);

                    for (Column colunaFiltrada : colunasFiltradas) {
                        gerarClassesParaMicrosservico(funcionalidadesMap, colunaFiltrada.getNomeFuncionalidade(),
                                classResponses);
                        functionalities += ", " + colunaFiltrada.getNomeFuncionalidade();
                    }
                    Microservice micro = new Microservice();
                    micro.setId("Microservice " + (microservices.size() + 1));
                    micro.setFunctionalities(functionalities);
                    micro.setClasses(classResponses);
                    microservices.add(micro);

                    similarities.add(functionalities);
                } else if (!Objects.isNull(indiceMicrosservico1) && Objects.isNull(indiceMicrosservico2)) {
                    Microservice micro = microservices.get(indiceMicrosservico1);
                    microservices.remove(micro);
                    List<ClassResponse> classResponses = micro.getClasses();

                    for (Column colunaFiltrada : colunasFiltradas) {
                        gerarClassesParaMicrosservico(funcionalidadesMap, colunaFiltrada.getNomeFuncionalidade(), classResponses);
                        if (!micro.getFunctionalities().contains(colunaFiltrada.getNomeFuncionalidade())) {
                            micro.setFunctionalities(micro.getFunctionalities() + ", " + colunaFiltrada.getNomeFuncionalidade());
                        }
                    }
                    micro.setClasses(classResponses);
                    microservices.add(indiceMicrosservico1, micro);

                } else if (Objects.isNull(indiceMicrosservico1) && !Objects.isNull(indiceMicrosservico2)) {
                    Microservice micro = microservices.get(indiceMicrosservico2);
                    microservices.remove(micro);
                    List<ClassResponse> classResponses = micro.getClasses();

                    gerarClassesParaMicrosservico(funcionalidadesMap, functionalities, classResponses);
                    micro.setFunctionalities(micro.getFunctionalities() + ", " + functionalities);
                    micro.setClasses(classResponses);
                    microservices.add(indiceMicrosservico2, micro);
                }
            }
        });
        return microservices;
    }

    private static Integer getIndiceMicrosservico(List<String> microsservicos, String functionality) {
        Integer indice = null;
        for (int contador = 0; contador < microsservicos.size(); contador++) {
            if (microsservicos.get(contador).contains(functionality)) {
                indice = contador;
                break;
            }
        }
        return indice;
    }

    private static Integer getIndiceMicrosservico(List<String> microsservicos, List<Column> functionalities) {
        Integer indice = null;
        for (Column functionality : functionalities) {
            for (int contador = 0; contador < microsservicos.size(); contador++) {
                if (microsservicos.get(contador).contains(functionality.getNomeFuncionalidade())) {
                    indice = contador;
                    break;
                }
            }
            if (!Objects.isNull(indice)) {
                break;
            }
        }
        return indice;
    }

    private static void gerarClassesParaMicrosservico(Map<String, List<Class>> funcionalidadesMap,
                                                      String functionalities,
                                                      List<ClassResponse> classResponses) {

        List<Class> listaClasses = funcionalidadesMap.get(functionalities);

        listaClasses.forEach(classeA -> {
            if (!ehClasseAdicionada(classResponses, classeA)) {
                List<String> methodNames = new ArrayList<>();
                classeA.getMethodName().forEach(methodNames::add);
                MethodResponse methodResponse = new MethodResponse(methodNames);
                ClassResponse classResponse = new ClassResponse(classeA.getClassName(), methodResponse);
                classResponses.add(classResponse);
            } else {
                Integer indice = getIndiceClassedicionada(classResponses, classeA);
                ClassResponse classResponse = classResponses.get(indice);

                //Adicionar apenas os métodos que não estão presentes ainda
                List<String> metodosNaoAdicionados = classeA.getMethodName().stream()
                        .filter(nameMethod -> !ehMetodoJaAdicionado(classResponse.getMethods().getNames(), nameMethod))
                        .collect(Collectors.toList());

                metodosNaoAdicionados.forEach(metodoNaoAdicionado -> classResponse.getMethods().getNames().add(metodoNaoAdicionado));
            }
        });
    }

    private static boolean ehMetodoJaAdicionado(List<String> metodos, String metodoNovo) {
        return metodos.stream().anyMatch(metodo -> metodo.equals(metodoNovo));
    }

    private static boolean ehClasseAdicionada(List<ClassResponse> classResponses, Class classeA) {
        if (classResponses.isEmpty()) return false;
        return classResponses.stream().
                filter(classeResponse -> classeResponse.getName().equals(classeA.getClassName()))
                .findFirst()
                .isPresent();
    }

    private static Integer getIndiceClassedicionada(List<ClassResponse> classResponses, Class classeA) {
        Integer indice = null;
        for (int contador = 0; contador < classResponses.size(); contador++) {
            if (classResponses.get(contador).getName().equals(classeA.getClassName())) {
                indice = contador;
                break;
            }
        }
        return indice;
    }

    private static Map<String, List<Column>> createSimilarityTable(Map<String, List<Class>> functionalities) {

        Map<String, List<Column>> similarityTable = new HashMap<>();

        functionalities.forEach((functionality1, classes1) -> {
            List<Column> colunas = new ArrayList<>();
            functionalities.forEach((functionality2, classes2) -> {
                if (!functionality1.equals(functionality2)) {
                    List<Class> classesIguais = intersection(classes1, classes2);
                    Double similarity = Double.valueOf(classesIguais.size() * (0.1 * classesIguais.size())) /
                            Double.valueOf(classes1.size() * (0.1 * classes1.size())) * 100;
                    colunas.add(new Column(functionality2, similarity));
                }
            });
            similarityTable.put(functionality1, colunas);
        });
        return similarityTable;
    }

    private static List<Class> intersection(List<Class> classesF1, List<Class> classesF2) {
        List<Class> classesSimilares = new ArrayList<>();
        for (int contador1 = 0; contador1 < classesF1.size(); contador1++) {
            for (int contador2 = 0; contador2 < classesF2.size(); contador2++) {
                if (classesF1.get(contador1).getClassName().equals(classesF2.get(contador2).getClassName())) {
                    if (classesSimilares.isEmpty()) {
                        classesSimilares.add(classesF1.get(contador1));
                    } else if (!classeJaExistente(classesSimilares, classesF1.get(contador1))) {
                        classesSimilares.add(classesF1.get(contador1));
                    }
                }
            }
        }
        return classesSimilares;
    }

    private static boolean classeJaExistente(List<Class> classes, Class classeNova) {
        return classes.stream()
                .filter(classe ->
                        classe.getClassName().equals(classeNova.getClassName()))
                .findFirst()
                .isPresent();
    }

    private static void printMicrosservices(List<Microservice> microsservicos) {

        microsservicos.forEach(microservice -> {
            try {
                String json = new ObjectMapper().writeValueAsString(microservice);
                System.out.println(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private static Map<String, List<Class>> convertFunctionalityFiles(List<String> functionalityFiles,
                                                                      List<String> packages) {
        Map<String, List<Class>> functionalityMaps = new HashMap<>();
        Class classe;
        List<Class> classes;
        File file;
        String nomeFuncionalidade;
        for (String nomeArquivo : functionalityFiles) {
            file = new File(nomeArquivo);
            nomeFuncionalidade = file.getName().substring(0, file.getName().lastIndexOf("."));
            classes = new ArrayList<>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String st;
                while ((st = br.readLine()) != null) {
                    String[] linha = st.split(", ");

                    String className = linha[0].substring(linha[0].lastIndexOf(": ") + 2);
                    String packageName = className.substring(0, className.lastIndexOf("."));
                    if (isAuthorizedPackage(packageName, packages)) {
                        classe = new Class();
                        classe.setClassName(className);
                        classe.setPackageName(packageName);

                        //TODO corrigir o nome dessa variável abaixo
                        List<String> t = new ArrayList<>();

                        t.add(linha[1].substring(linha[1].lastIndexOf(": ") + 2));
                        classe.setMethodName(t);

                        Integer indiceClasse = getIndiceClasse(classes, classe);
                        if (Objects.isNull(indiceClasse)) {
                            classes.add(classe);
                        } else {
                            Class classeA = classes.get(indiceClasse);
                            classes.remove(classeA);
                            classeA.addMethodName(linha[1].substring(linha[1].lastIndexOf(": ") + 2));
                            classes.add(indiceClasse, classeA);
                        }
                    }


                }
                functionalityMaps.put(nomeFuncionalidade, classes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return functionalityMaps;
    }

    private static Boolean isAuthorizedPackage(String packageName, List<String> packages) {
        return packages.stream()
                .filter(pack -> pack.equals(packageName))
                .findFirst()
                .isPresent();

    }

    private static Integer getIndiceClasse(List<Class> classes, Class classe) {
        Integer indice = null;
        if (classes.isEmpty()) return null;

        for (int contador = 0; contador < classes.size(); contador++) {
            if (classes.get(contador).getClassName().equals(classe.getClassName())) {
                indice = contador;
                break;
            }
        }
        return indice;
    }

    private static List<String> buscarNomeArquivos(String diretorioLeitura) {
        String diretorioDestino = "src/main/resources/output";
        List<String> nomeArquivos = new ArrayList<>();
        descompactar(diretorioLeitura, diretorioDestino, nomeArquivos);
        return getPathArquivos(diretorioDestino, nomeArquivos);
    }

    private static List<String> getPathArquivos(String diretorioDestino, List<String> nomeArquivos) {
        List<String> pathArquivos = new ArrayList<>();
        nomeArquivos.forEach(nome -> pathArquivos.add(diretorioDestino + "/" + nome));
        return pathArquivos;
    }

    private static void descompactar(String zipFilePath, String destDir, List<String> nomeArquivos) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                nomeArquivos.add(fileName);
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to " + newFile.getAbsolutePath());
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
}
