package com.agtinternational.hobbit.sdk.examples.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
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

/**
 * 
 * @author Fahad Anwar
 * @since 7 / December / 2017
 *
 */

public class FactBenchDataGenerator extends AbstractDataGenerator {
	final static Logger logger = LoggerFactory.getLogger(FactBenchDataGenerator.class);
	/* taskGenerator byte array */
	// private byte[] task;

	// private Semaphore generateTasks = new Semaphore(0);

	@Override
	public void init() throws Exception {
		super.init();

		// long seedId = getGeneratorId();
		// long totalGenerators = getNumberOfGenerators();

	}

	@SuppressWarnings("unused")
	@Override
	protected void generateData() throws Exception {
		logger.info("Generating Data");

		int dataGeneratorId = getGeneratorId();
		int numberOfGenerators = getNumberOfGenerators();

		String path = "E:/ProjectWorkspace/FactBench/test/correct/";
		Map<String, List<Model>> correct = readFiles(path);

		
		path = "E:/ProjectWorkspace/FactBench/test/wrong/";
		Map<String, List<Model>> wrong = readFiles(path);
		
		//Sending Data
		for (Entry<String, List<Model>> entry : correct.entrySet()) {
//			System.out.println("Key = " + entry.getKey() + " Size : " + entry.getValue().size());
			entry.getValue().forEach(model->{
				try {
					 byte[] data = modelToBytes(model);
					 sendDataToTaskGenerator(data);
					 sendDataToSystemAdapter(data);
					 } catch (IOException e) {
					 e.printStackTrace();
					 }
			});
		}
		
		for (Entry<String, List<Model>> entry : wrong.entrySet()) {
//			System.out.println("Key = " + entry.getKey() + " Size : " + entry.getValue().size());
			entry.getValue().forEach(model->{
				try {
					 byte[] data = modelToBytes(model);
					 sendDataToTaskGenerator(data);
					 sendDataToSystemAdapter(data);
					 } catch (IOException e) {
					 e.printStackTrace();
					 }
			});
		}

		// TODO CREATE dataGeneratorId && numberOfGenerators

		// logger.info("Fetching correct models");
		// List<Model> correct =
		// readModels("E:/ProjectWorkspace/FactBench/test/correct/");

		// logger.info("Fetching wrong models");
		// List<Model> wrong = readModels("E:/ProjectWorkspace/FactBench/test/wrong/");

		// logger.info("Correct: " + correct.size() + " Wrong: " + wrong.size());

		// logger.info("Sending Correct Models to TaskGenerator and SystemAdaptor");
		// correct.forEach(model -> {
		// try {
		// byte[] data = modelToBytes(model);
		// sendDataToTaskGenerator(data);
		// sendDataToSystemAdapter(data);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// });
		//
		// logger.info("Sending Incorrect Models to Datagenerator and Task Generator");
		// wrong.forEach(model -> {
		// try {
		// byte[] data = modelToBytes(model);
		// sendDataToTaskGenerator(data);
		// sendDataToSystemAdapter(data);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// });

		// if file is large then you need to break it into chunks
		// sample at:
		// https://github.com/hobbit-project/faceted-benchmark/blob/master/data-generator/src/main/java/org/hobbit/SampleDataGenerator.java

	}

	/**
	 * 
	 * @param directoryPath
	 *            String
	 * @return
	 * @return List<Model>
	 * @throws IOException
	 */
	private Map<String, List<Model>> readFiles(String directoryPath) throws IOException {

		Map<String, List<Model>> map = new HashMap<String, List<Model>>();
		String path = directoryPath;
		try (Stream<Path> paths = Files.walk(Paths.get(path))) {
			paths.forEach(p -> {
				File directoryName = new File(p.toString());
				if (directoryName.isDirectory()) {
					String key = directoryName.getName();
					if (map.containsKey(directoryName.getName())) {
						List<File> files = (List<File>) Arrays.asList(directoryName.listFiles());
						ArrayList<Model> models = new ArrayList<>();
						files.forEach(file -> {
							try {
//								System.out.println(file);
//								System.out.println(file.getPath());
								Model model = ModelFactory.createDefaultModel();
								model.read(new FileReader(file), null, "TURTLE");
								models.add(model);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
						map.get(key).addAll(models);

					} else {
						ArrayList<Model> models = new ArrayList<>();
						List<File> files = (List<File>) Arrays.asList(directoryName.listFiles());
						if (files.get(0).isFile()) {

							files.forEach(file -> {
								try {

									Model model = ModelFactory.createDefaultModel();
									
									model.read(new FileReader(file), null, "TURTLE");
									models.add(model);

								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block

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

		// List<Model> models = new ArrayList<Model>();

		// TODO store files directory wise.
		// Files.walk(Paths.get(directoryPath)).filter(Files::isRegularFile).forEach(path
		// -> {
		// try {
		// Model model = ModelFactory.createDefaultModel();
		// model.read(new FileReader(path.toString()), null, "TURTLE");
		// models.add(model);
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }
		//
		// });
		// return models;

		return map;
	}

	protected void createStatements(List<Statement> statementList) {

	}

	protected byte[] modelToBytes(Model tripleModel) {
		// byte[] data;

		StringWriter stringWriter = new StringWriter();
		tripleModel.write(stringWriter, "TURTLE");

		String dataString = stringWriter.toString();

		return RabbitMQUtils.writeString(dataString);
	}

	
}
