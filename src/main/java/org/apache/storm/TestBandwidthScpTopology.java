package org.apache.storm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichSpout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBandwidthScpTopology {

    private static final String SCP_SOURCE = "scpSource";
    private static final String SCP_TARGET = "scpTarget";

    public static class TcSpout extends BaseRichSpout {
        private static final Logger LOG = LoggerFactory.getLogger(TcSpout.class);

        SpoutOutputCollector collector;
        List<String> cmds;

        @Override
        public void open(Map<String, Object> conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
            String scpSource = (String) conf.get(SCP_SOURCE);
            String scpTarget = (String) conf.get(SCP_TARGET);
            cmds = Arrays.asList("/usr/bin/scp", scpSource, scpTarget);
        }

        @Override
        public void nextTuple() {

            try {

                ProcessBuilder builder = new ProcessBuilder(cmds);
                builder.redirectErrorStream(true);
                final Process p;
                p = builder.start();
                p.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }

    /**
     * main.
     */
    public static final void main(String[] args) throws Exception {

        Config conf = new Config();

        conf.put(SCP_SOURCE, args[0]);
        conf.put(SCP_TARGET, args[1]);

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new TcSpout(), 1);
        conf.setNumWorkers(1);
        conf.setNumAckers(0);
        conf.setWorkerMaxBandwidthMbps(10);
        conf.setTopologyWorkerMaxHeapSize(768);

        StormSubmitter.submitTopology("tc", conf, builder.createTopology());
    }
}
