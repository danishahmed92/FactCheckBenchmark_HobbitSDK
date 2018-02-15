package org.hobbit.sdk.examples.dummybenchmark;


import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.sdk.FactCheckExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;


/**
 * This code is here just for testing and debugging the SDK.
 * For your projects please use code from the https://github.com/hobbit-project/java-sdk-example
 */

public class DummySystemAdapter extends AbstractSystemAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DummySystemAdapter.class);

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...


        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter
        logger.debug("Sending SYSTEM_READY_SIGNAL");
    }

    @Override
    public void receiveGeneratedData(byte[] data) {
        // handle the incoming data as described in the benchmark description
        logger.debug("receiveGeneratedData(" + new String(data) + "): " + new String(data));

    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
        // handle the incoming task and create a result

        //TODO invoke API and process result
        /*
        RestTemplate rest = new RestTemplate();
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("taskId",taskId);
        map.add("data", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(map, headers);

        ResponseEntity<FCApi> response =
                rest.exchange("http://localhost:8080/api/execTask/"+taskId,
                        HttpMethod.POST, request, FCApi.class);

        FCApi apiResult =  response.getBody();

        try {
            logger.debug("sendResultToEvalStorage({})->{}", taskId, apiResult.getData());
            sendResultToEvalStorage(taskId, toByteArray(apiResult.getDefactoScore()));
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        int min = 0;
        int max = 100;
        String result = "";
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        double confidence = randomNum * 0.0011;

        if (Integer.parseInt(taskId) % 2 == 0)
            result = "true";
        else
            result = "false";

        result += ":*:" + String.valueOf(confidence);

        logger.debug("receiveGeneratedTask({})->{}", taskId, new String(data));

        // Send the result to the evaluation storage
        try {
            logger.debug("sendResultToEvalStorage({})->{}", taskId, result);
            sendResultToEvalStorage(taskId, result.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }
}

