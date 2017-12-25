package org.hobbit.sdk.examples.dummybenchmark;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Pavel Smirnov
 */

public class DataGenerator extends AbstractDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...
    }

    @Override
    protected void generateData() throws Exception {
        // Create your data inside this method. You might want to use the
        // id of this data generator and the number of all data generators
        // running in parallel.
        int dataGeneratorId = getGeneratorId();
        int numberOfGenerators = getNumberOfGenerators();

        logger.debug("generateData()");

        logger.info("Loading correct models");
        String path = "src/main/resources/test/correct";
        Map<String, List<Model>> correct = readFiles(path);

        logger.info("Loading wrong models");
        path = "src/main/resources/test/wrong/";
        Map<String, List<Model>> wrong = readFiles(path);

        logger.info("Loading train correct models");
        path = "src/main/resources/train/correct/";
        Map<String, List<Model>> trainCorrect = readFiles(path);

        logger.info("Loading train wrong models");
        path = "src/main/java/resources/train/wrong/";
        Map<String, List<Model>> trainWrong = readFiles(path);


        // Sending Data
        logger.info("Sending correct Models to TaskGenerator");
        sendData(correct);

        logger.info("Sending wrong Models to TaskGenerator");
        sendData(wrong);

        logger.info("Sending train correct models to TaskGenerator");
        sendData(trainCorrect);

        logger.info("Sending train wrong models to TaskGenerator");
        sendData(trainWrong);


    }

    private void sendData(Map<String, List<Model>> correct) {
        for (Map.Entry<String, List<Model>> entry : correct.entrySet()) {
            entry.getValue().forEach(model -> {
                try {
                    // logger.info(entry.getKey() + "\n" + model.toString());
                    byte[] data = modelToBytes(model, entry.getKey());
                    sendDataToTaskGenerator(data);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private Map<String, List<Model>> readFiles(String directoryPath) {
        Map<String, List<Model>> map = new HashMap<String, List<Model>>();
        String path = directoryPath;
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.forEach(p -> {
                File directoryName = new File(p.toString());
                if (directoryName.isDirectory()) {

                    String key = directoryName.getName();

                    //     logger.info("Key: "+directoryName.getPath());


                    if (map.containsKey(directoryName.getName())) {
                        List<File> files = Arrays.asList(directoryName.listFiles());
                        ArrayList<Model> models = new ArrayList<>();
                        files.forEach(file -> {
                            try {
                                Model model = ModelFactory.createDefaultModel();
                                model.read(new FileReader(file), null, "TURTLE");
                                models.add(model);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        });
                        map.get(key).addAll(models);

                    } else {
                        ArrayList<Model> models = new ArrayList<>();
                        List<File> files = Arrays.asList(directoryName.listFiles());
                        if (files.get(0).isFile()) {

                            files.forEach(file -> {
                                try {

                                    Model model = ModelFactory.createDefaultModel();
                                    model.read(new FileReader(file), null, "TURTLE");
                                    models.add(model);

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            });

                            map.put(key, models);
                        }
                    }

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    protected byte[] modelToBytes(Model tripleModel, String Path) {
        // byte[] data;

        String expectedAnswer = "";
        if (Path.contains("correct"))
            expectedAnswer = "true";
        else
            expectedAnswer = "false";


        StringWriter stringWriter = new StringWriter();
        tripleModel.write(stringWriter, "TURTLE");

        String dataString = expectedAnswer + ":*:" + stringWriter.toString();

        return RabbitMQUtils.writeString(dataString);
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        super.close();
    }

}