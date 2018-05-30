package org.apache.storm;

import java.util.Arrays;
import java.util.List;

import org.apache.storm.daemon.worker.Worker;

public class WorkerIDEA {

    public static void main(String[] args) throws Exception {


       List<String> params = Arrays.asList(
               "yylive_dur_calc_client_test-1-1527594303",
               "ff743b5d-c077-4e82-949c-6f0643e89e72-127.0.0.1",
               "6628",
               "6701",
               "699370ba-19e7-4aeb-ac5f-3ec5a872bedd");

        Worker.main(params.toArray(new String[0]));


    }
}
