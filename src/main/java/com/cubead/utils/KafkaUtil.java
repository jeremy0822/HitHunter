package com.cubead.utils;

import com.cubead.conf.Constants;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoao on 6/1/15.
 */
public class KafkaUtil {
    public static long getLastOffset() {
        String host = Constants.kAFKA_HOST();
        int port = Constants.kAFKA_PORT();
        int soTimeout = Constants.kAFKA_TIMEOUT();
        int bufferSize = Constants.kAFKA_BUFFER();
        String topic = Constants.kAFKA_TOPIC();
        int partition = 0;
        long whichTime = kafka.api.OffsetRequest.LatestTime(); // System.currentTimeMillis();
        String clientName = Constants.KAFKA_CLIENT_ID();

        SimpleConsumer consumer = new SimpleConsumer(host,port,soTimeout, bufferSize, clientName);
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
        OffsetResponse response = consumer.getOffsetsBefore(request);
        if (response.hasError()) {
            System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition) );
            return 0;
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }

//    public static void main(String[] args){
//        long t = System.currentTimeMillis();
////        SimpleConsumer sc = new SimpleConsumer("192.168.3.227", 9092, 100000, 64 * 1024, "tt");
//        Constants.kAFKA_HOST_$eq("192.168.3.227");
//        Constants.kAFKA_GROUPID_$eq("topic-test");
//        long offset = getLastOffset();
//        System.out.println("Offset:" + offset);
//    }
}