package producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * 지정된 파티션으로 데이터 보내기
 */
public class KafkaBookProducerKey {

	public static void main(String[] args) {
		Properties props = new Properties();
		// 처음 연결을 하기 위한 호스트와 포트 정보로 구성된 리스트
		props.put("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
		// 프로듀서가 카프카 토픽의 리더에게 메시지를 보낸 후 요청을 완료하기 전 승인의 수
		props.put("acks", "1");
		// 압축타입 non, gzip, snappy, lz4 등
		props.put("compression.type", "gzip");
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer = new KafkaProducer<>(props);
		String testTopic = "peter-topic3";
		String oddKey = "1";
		String evenKey = "2";

		for (int i = 1; i < 11; i++) {
			if (i % 2 == 1) {
				producer.send(new ProducerRecord<String, String>(testTopic, oddKey,
						String.format("%d - Apache Kafka is a distributed streaming platform - key=" + oddKey, i)));
			} else {
				producer.send(new ProducerRecord<String, String>(testTopic, evenKey,
						String.format("%d - Apache Kafka is a distributed streaming platform - key=" + evenKey, i)));
			}
		}

		producer.close();
	}
}
