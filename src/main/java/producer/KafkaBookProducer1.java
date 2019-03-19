package producer;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

public class KafkaBookProducer1 {

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9091,localhost:9092,localhost:9093");
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		// 추가 옵션 지정
		props.put("acks", "1");
		props.put("compression.type", "gzip");


		// 1) 메시지를 보내고 확인하지 않기
		// 메시지 손실 가능성이 있기 때문에 일반적인 서비스 환경에서는 사용하지 않는다.
		/*Producer<String, String> producer = new KafkaProducer<>(props);
		try {
			producer.send(new ProducerRecord<String, String>("peter-topic", "Apache Kafka is a distributed streaming platform"));
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			producer.close();
		}*/

		// 2) 동기 전송
		// Future의 get() 메소드를 사용해 send()가 성공했는지 실패했는지 확인한다.
		/*Producer<String, String> producer = new KafkaProducer<>(props);
		try {
			RecordMetadata metadata = producer.send(new ProducerRecord<String, String>("peter-topic", "Apache Kafka is a distributed streaming platform"))
					.get();
			System.out.printf("Partition: %d, Offset: %d", metadata.partition(), metadata.offset());
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			producer.close();
		}*/

		// 3) 비동기 전송
		// 비동기적으로 전송한다면 응답을 기다리지 않기 때문에 더욱 빠른 전송이 가능하다.
		// 또한 메시지를 보내지 못했을 때 예외를 처리하게 해 에러를 기록하거나 향후 분석을 위해 에러 로그 등에 기록할 수 있다.
		Producer<String, String> producer = new KafkaProducer<>(props);
		try {
			producer.send(new ProducerRecord<String, String>("peter-topic", "Apache Kafka is a distributed streaming platform"),
					new PeterCallback());
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			producer.close();
		}

	}
}

class PeterCallback implements Callback {
	@Override public void onCompletion(RecordMetadata metadata, Exception exception) {
		if (metadata != null) {
			System.out.println("Partition : " + metadata.partition() + ", Offset : " + metadata.offset() + "");
		} else {
			exception.printStackTrace();
		}

	}
}
