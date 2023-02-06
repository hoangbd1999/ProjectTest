package com.elcom.metacen.vsat.collector.config.kafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.common.record.InvalidRecordException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author hanh props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,
 * VsatPartitioner.class.getName());
 * org.apache.kafka.clients.producer.RoundRobinPartitioner
 */
public class VsatPartitioner implements Partitioner {

    @Override
    public void configure(Map<String, ?> configs) {
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        
        //if ( topic.equals(KAFKA_AIS_TOPIC_IMPORT) || topic.equals(KAFKA_AIS_TOPIC_IMPORT_R) ) { // partition strategy for AIS
        if ( "VSAT_AIS_RAW".equals(topic) || "AIS_RAW".equals(topic) ) { // partition strategy for AIS
            int numPartitions = partitions.size();
            if ((keyBytes == null) || (!(key instanceof String)))
                throw new InvalidRecordException("We expect all messages to have key");
            
            return Math.abs(Utils.murmur2(keyBytes)) % (numPartitions - 1);
        } else {
            int partitionNumberValue = 0;
            if( partitions.size() > 1 )
                partitionNumberValue = (new Random()).nextInt(partitions.size() - 1);
            
            return partitionNumberValue;
        }
    }
    
    @Override
    public void close() {
    }

//    public static void main(String[] args) {
//        int numPartitions = 10;
//        byte[] keyBytes = "25635.127".getBytes();
//        System.out.println("partition no: [ " + (Math.abs(Utils.murmur2(keyBytes)) % (numPartitions - 1)) + " ]");
//    }
}
