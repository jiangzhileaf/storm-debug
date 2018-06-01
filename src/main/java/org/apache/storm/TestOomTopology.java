package org.apache.storm;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichSpout;

public class TestOomTopology {

    static class OOMObject {

    }

    private static class TestSpout extends BaseRichSpout {

        List<OOMObject> list = new ArrayList<>();

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        }

        @Override
        public void nextTuple() {
            while (true) {
                list.add(new OOMObject());
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {

        }
    }

    public static void main( String[] args ) throws AlreadyAliveException, InvalidTopologyException, UnsupportedEncodingException, AuthorizationException {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("TestSpout", new TestSpout(), 1);

        Config conf = new Config();

        StormSubmitter.submitTopology("TestOomTopology", conf, builder.createTopology());
    }
}
