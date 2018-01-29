package org.hobbit.sdk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

 public class FCApi implements Serializable {
        private final String taskId;
        private final byte[] data;
        private double defactoScore;


        @JsonCreator
        public FCApi(@JsonProperty("taskId") String taskId, @JsonProperty("data") byte[] data,
                     @JsonProperty("defactoScore")  double defactoScore) {
            this.taskId = taskId;
            this.data = data;
            this.defactoScore = defactoScore;
        }

        public void setDefactoScore(double defactoScore) {
            this.defactoScore = defactoScore;
        }

        public String getTaskId() {
            return taskId;
        }

        public byte[] getData() {
            return data;
        }

        public double getDefactoScore() {
            return defactoScore;
        }

}
