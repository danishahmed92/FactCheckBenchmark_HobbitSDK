package org.hobbit.sdk.examples.dummybenchmark;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummyEvalModule extends AbstractEvaluationModule {
    private static final Logger logger = LoggerFactory.getLogger(DummyEvalModule.class);
	private static int truePositive = 0;
    private static int falsePositive = 0;
    private static int trueNegative = 0;
    private static int falseNegative = 0;

    private long taskSentTimestamp = System.currentTimeMillis();
    private long responseReceivedTimestamp = System.currentTimeMillis();
    private long runTime = System.currentTimeMillis();
    // expectedData:    true/false
    // received Data:   true/false _percentage_
    // totalDataConsidered = trueNegative + falseNegative
    // DataIdentifiedIncorrectly = totalDataConsidered - (truePositive + falsePositive)
    // DataIdentifiedCorrectly = totalDataConsidered - DataIdentifiedIncorrectly
    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp, long responseReceivedTimestamp) throws Exception {
        // evaluate the given response and store the result, e.g., increment internal counters
        this.taskSentTimestamp = taskSentTimestamp;
        this.responseReceivedTimestamp = responseReceivedTimestamp;

        String rData = new String((receivedData));
        String eData = new String((expectedData));

        if (rData.contains(eData)) {
            if (eData.equals("true"))
                truePositive++;
            else
                trueNegative++;
        } else if (eData.equals("true") && rData.contains("false")) {
            falseNegative++;
        } else if (rData.contains("true") && eData.equals("false")) {
            falsePositive++;
        }
        runTime = taskSentTimestamp - responseReceivedTimestamp;
        logger.debug("evaluateResponse()");
        logger.debug(new String(expectedData) + ">>>>>" + new String(receivedData));
        if (receivedData.toString().contains(expectedData.toString()))
            logger.debug("CORRRECT Answer");
        else
            logger.debug("NOT QUITE Answer");
        logger.debug("Task Run Time : " + runTime);
    }
	
	public static double calculateAccuracy() {
        return (truePositive + trueNegative) / (double)(truePositive + trueNegative + falsePositive + falseNegative);
    }

    // relevantItemsRetrieved / retrievedItemsa
    public static double calculatePrecision() {
        return truePositive  / (double)(truePositive + falsePositive);
    }

    // relevantItemsRetrieved / relevantItems
    public static double calculateRecall() {
        return truePositive  / (double)(truePositive + falseNegative);
    }

    @Override
    protected Model summarizeEvaluation() throws Exception {
        logger.debug("summarizeEvaluation()");
        // All tasks/responsens have been evaluated. Summarize the results,
        // write them into a Jena model and send it to the benchmark controller.
        logger.debug("Overall accuracy of FactCheck was " + (calculateAccuracy()*100) + "%");
        Model model = createDefaultModel();
        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource , RDF.type, HOBBIT.Experiment);

        return model;
    }

    @Override
    public void close(){
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        try {
            super.close();
        }
        catch (Exception e){

        }
    }

}
