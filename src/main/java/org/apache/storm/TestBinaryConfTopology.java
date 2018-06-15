package org.apache.storm;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichSpout;

public class TestBinaryConfTopology {

    private static final byte[] testBytes = getRandomByteArray(12345, 1024);

    private static class BinarySpout extends BaseRichSpout {

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            try {
                byte[] spoutBytesString = ((String) conf.get("testBytesString")).getBytes("ISO-8859-1");

                System.out.println(String.format("bytes string result: %s", compareBinary(spoutBytesString, testBytes)));

                System.out.println("original: " + Arrays.toString(testBytes));
                System.out.println("bytes string: " + Arrays.toString(spoutBytesString));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void nextTuple() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {

        }
    }

    public static boolean compareBinary(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        } else {
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
        }

        return true;
    }

    private static byte[] getRandomByteArray(int seed, int bit) {
        byte[] ret = new byte[bit];
        Random rd = new Random(seed);
        rd.nextBytes(ret);
        return ret;
    }

    public static void main( String[] args ) throws AlreadyAliveException, InvalidTopologyException, UnsupportedEncodingException, AuthorizationException {

        Config conf = new Config();

        conf.put("testBytesString", new String(testBytes, "ISO-8859-1"));

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("BinarySpout", new BinarySpout(), 1);

        StormSubmitter.submitTopology("TesBinaryConfTopology", conf, builder.createTopology());
    }
}
