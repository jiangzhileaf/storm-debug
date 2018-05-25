package org.apache.storm;

import java.util.Map;
import java.util.Random;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestResourceTopology {

    public static class RandomSentenceSpout extends BaseRichSpout {
        private static final Logger LOG = LoggerFactory.getLogger(RandomSentenceSpout.class);

        SpoutOutputCollector collector;
        Random rand;


        @Override
        public void open(Map<String, Object> conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
            this.rand = new Random();
        }

        @Override
        public void nextTuple() {
            Utils.sleep(100);
            String[] sentences = new String[]{
                    sentence("the cow jumped over the moon"),
                    sentence("an apple a day keeps the doctor away"),
                    sentence("four score and seven years ago"),
                    sentence("snow white and the seven dwarfs"),
                    sentence("i am at two with nature")};
            final String sentence = sentences[rand.nextInt(sentences.length)];

            LOG.debug("Emitting tuple: {}", sentence);

            collector.emit(new Values(sentence));
        }

        protected String sentence(String input) {
            return input;
        }

        @Override
        public void ack(Object id) {
        }

        @Override
        public void fail(Object id) {
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word"));
        }
    }

    public static class WordCountBolt extends BaseBasicBolt {

        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            String sentence = tuple.getString(0);
            int count = sentence.split(" ").length;
            System.out.println(count);
            collector.emit(new Values(count));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("count"));
        }
    }

    /**
     * main.
     */
    public static final void main(String[] args) throws Exception {


        Config conf = new Config();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new RandomSentenceSpout(), 1).setMemoryLoad(64).setCPULoad(50);
        builder.setBolt("count", new WordCountBolt(), 1).shuffleGrouping("spout").setMemoryLoad(64).setCPULoad(50);

        conf.setDebug(true);

        conf.setNumAckers(0);
        conf.setNumWorkers(1);
        conf.setTopologyWorkerMaxHeapSize(768);
        conf.setWorkerMaxBandwidthMbps(20);
        //conf.put(Config.TOPOLOGY_SUBMITTER_USER, "jiangzhileaf");

        StormSubmitter.submitTopology("word-count", conf, builder.createTopology());
    }
}
