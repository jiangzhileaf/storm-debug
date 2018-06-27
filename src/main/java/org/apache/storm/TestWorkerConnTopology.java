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
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestWorkerConnTopology {
    private static class TestSpout extends BaseRichSpout {

        SpoutOutputCollector collector;

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void nextTuple() {
            collector.emit(new Values(System.currentTimeMillis()));

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("ts"));
        }
    }

    private static class TestBlot extends BaseRichBolt {

        private final static Logger LOG = LoggerFactory.getLogger(TestBlot.class);

        @Override
        public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        }

        @Override
        public void execute(Tuple input) {
            Long l = input.getLongByField("ts");
            LOG.info("tuple: {}", l);
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {

        }
    }

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException, UnsupportedEncodingException, AuthorizationException {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("TestSpout", new TestSpout(), 1);
        builder.setBolt("TestBolt", new TestBlot(), 1);

        Config conf = new Config();
        conf.setNumAckers(0);
        conf.setNumWorkers(2);
        conf.setTopologyWorkerMaxHeapSize(255);

        StormSubmitter.submitTopology("TestWorkerConnTopology", conf, builder.createTopology());
    }
}
