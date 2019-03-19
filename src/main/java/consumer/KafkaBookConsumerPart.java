package consumer;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Properties;

public class KafkaBookConsumerPart {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9091,localhost:9092,localhost:9093");
		props.put("group.id", "peter-partition");
		props.put("enable.auto.commit", "false");
		props.put("auto.offset.reset", "latest");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

		// 특정 파티션 직접 지정
		String topic = "peter-topic";
		TopicPartition partition0 = new TopicPartition(topic, 0);
		TopicPartition partition1 = new TopicPartition(topic, 1);
		consumer.assign(Arrays.asList(partition0, partition1));

		// 특정 오프셋
		//consumer.seek(partition0, 2);
		//consumer.seek(partition1, 2);

		try {
			while (true) {
				ConsumerRecords<String, String> recoreds = consumer.poll(100);
				for (ConsumerRecord<String, String> recored : recoreds) {
					System.out.printf("Topic : %s, Partition : %s, Offset : %d, Key: %s Value : %s\n",
							recored.topic(), recored.partition(), recored.offset(), recored.key(), recored.value());
				}
				try {
					consumer.commitAsync();
				} catch (CommitFailedException exception) {
					System.out.printf("Error", exception);
				}
			}
		} finally {
			consumer.close();
		}
	}
}
