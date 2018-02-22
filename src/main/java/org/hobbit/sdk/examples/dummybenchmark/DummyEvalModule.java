package org.hobbit.sdk.examples.dummybenchmark;

import mloss.roc.Curve;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFhtml;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


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
    private Property accuracyProperty = null;
    private Property runTimeProperty = null;
    private long runTime = System.currentTimeMillis();
    private  ArrayList<Integer> trueLabels= new ArrayList<>();
    private  ArrayList<Double> confidenceScores = new ArrayList<>();

    // expectedData:    true/false
    // received Data:   true/false _percentage_
    // totalDataConsidered = trueNegative + falseNegative
    // DataIdentifiedIncorrectly = totalDataConsidered - (truePositive + falsePositive)
    // DataIdentifiedCorrectly = totalDataConsidered - DataIdentifiedIncorrectly
    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp, long responseReceivedTimestamp) throws Exception {
        setRunTime( responseReceivedTimestamp - taskSentTimestamp );
        // evaluate the given response and store the result, e.g., increment internal counters

        String []receivedResponse = (new String(receivedData)).split(":\\*:");
        String expectedResponse = new String(expectedData);

        if (receivedResponse[0].contains(expectedResponse)) {
            if (expectedResponse.equals("true"))
                truePositive++;
            else
                trueNegative++;
        } else if (expectedResponse.equals("true") && receivedResponse[0].contains("false")) {
            falseNegative++;
        } else if (receivedResponse[0].contains("true") && expectedResponse.equals("false")) {
            falsePositive++;
        }
        runTime = taskSentTimestamp - responseReceivedTimestamp;

        //Update accumulators for ROC/AUC calculation
        confidenceScores.add(Double.parseDouble(receivedResponse[1]));

        if(expectedResponse.equals("true"))
            trueLabels.add(1);
        else
            trueLabels.add(0);

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

        Model model = createDefaultModel();
        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource , RDF.type, HOBBIT.Experiment);
        /*//print keys that environment has... to see
        what they are and where they are....*/


        //Calculate AUC and obtain the points for the ROC curve
        Curve rocCurve = new Curve.PrimitivesBuilder().predicteds(confidenceScores).actuals(trueLabels).build();
        double [][]rocPoints = rocCurve.rocPoints();

        //TODO add ROC/AUC values to the model
        logger.debug("ROC/AUC: "+rocCurve.rocArea());

        double experimentAccuracy = calculateAccuracy() * (double)100;
        Literal accuracyLiteral = model.createTypedLiteral(experimentAccuracy, XSDDatatype.XSDdouble);
        Map<String, String> env = System.getenv();
        if (!env.containsKey("accuracy")) {
            //throw new IllegalArgumentException("Couldn't get \"" + "accuracy" + "\" from the environment. Aborting.");
        }
        try {
            env.put("accuracy", "Accuracy");
            model.createProperty(env.get("accuracy"));
            setAccuracyProperty(model.getProperty("accuracy"));
            model.add(experimentResource, getAccuracyProperty(), accuracyLiteral);
        }catch(Exception e) {
        }
        logger.debug("Overall accuracy of FactCheck was " + (calculateAccuracy()*100) + "%");



        Literal runtimeLiteral = model.createTypedLiteral(getRunTime(), XSDDatatype.XSDlong);
        if (!env.containsKey("runtime")) {
            //throw new IllegalArgumentException("Couldn't get \"" + "runtime" + "\" from the environment. Aborting.");
        }
        try {
            env.put("runtime", "Runtime");
            model.createProperty(env.get("runtime"));
            setRunTimeProperty(model.getProperty("runtime"));
            model.add(experimentResource, getRunTimeProperty(), runtimeLiteral);
        }catch(Exception e) {
        }
        logger.debug("Overall runtime of the experiment was" + getRunTime() + "ms");


        return model;
    }

    public Property getRunTimeProperty() {
        return runTimeProperty;
    }

    public void setRunTimeProperty(Property runTimeProperty) {
        this.runTimeProperty = runTimeProperty;
    }

    public Property getAccuracyProperty() {
        return accuracyProperty;
    }

    public void setAccuracyProperty(Property accuracyProperty) {
        this.accuracyProperty = accuracyProperty;
    }

    public long getRunTime() {
        return runTime;
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
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
