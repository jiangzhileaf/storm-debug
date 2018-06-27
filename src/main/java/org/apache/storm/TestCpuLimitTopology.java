package org.apache.storm;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCpuLimitTopology {


    private static class TestSpout extends BaseRichSpout {

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {

            for (int i = 0; i < 3; i++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        while (true) {
                            i++;
                        }
                    }
                }).start();
            }

        }

        @Override
        public void nextTuple() {
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {

        }
    }

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException, UnsupportedEncodingException, AuthorizationException {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("TestSpout", new TestSpout(), 1).setMemoryLoad(1024).setCPULoad(100);

        Config conf = new Config();
        conf.setNumWorkers(1);
        conf.setNumAckers(0);
        conf.setWorkerMaxBandwidthMbps(20);
        conf.setTopologyWorkerMaxHeapSize(4096);

        StormSubmitter.submitTopology("TestCpuLimitTopology", conf, builder.createTopology());
    }
}
