package org.hobbit.sdk.examples.dummybenchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.log4j.BasicConfigurator;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyDataGenerator extends AbstractDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DummyDataGenerator.class);
	private final String factBenchPath = "/Users/oshando/test/wrong";//"src/main/resources/factbench/test/correct";

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

		logger.info("Loading models");
		Map<String, List<Model>> factBenchModels = readFiles(factBenchPath);

		// Sending Data
		logger.info("Sending Models to TaskGenerator");

		//For each model, send it's data and expected result to the TaskGenerator
		for (Map.Entry<String, List<Model>> entry : factBenchModels.entrySet()) {

			entry.getValue().forEach(model -> {
				try {
					sendDataToTaskGenerator(modelToBytes(model, entry.getKey()));

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

					logger.debug("Key: " + directoryName.getPath());


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

	//Converts model and expected answer to bytes
	protected byte[] modelToBytes(Model tripleModel, String modelPath) {

		String expectedAnswer = "";

		if (modelPath.contains("correct"))
			expectedAnswer = "true";
		else
			expectedAnswer = "false";

		StringWriter stringWriter = new StringWriter();
		tripleModel.write(stringWriter, "TURTLE");

		String dataString = expectedAnswer + ":*:" + stringWriter.toString();

		return RabbitMQUtils.writeString(dataString);
	}

	
}
