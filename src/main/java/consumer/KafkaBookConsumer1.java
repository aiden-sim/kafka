package consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class KafkaBookConsumer1 {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9091,localhost:9092,localhost:9093");
		props.put("group.id", "peter-consumer");
		props.put("enable.auto.commit", "true");
		props.put("auto.offset.reset", "latest");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Arrays.asList("peter-topic"));
		try {
			while (true) {
				ConsumerRecords<String, String> recoreds = consumer.poll(100);
				for (ConsumerRecord<String, String> recored : recoreds) {
					System.out.printf("Topic : %s, Partition : %s, Offset : %d, Key: %s Value : %s\n",
							recored.topic(), recored.partition(), recored.offset(), recored.key(), recored.value());
				}
			}
		} finally {
			consumer.close();
		}
	}
}
