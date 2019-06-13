package consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public class KafkaWebhook {
	public static void main(String[] args) throws IOException {
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9091");
		props.put("group.id", "peter-consumer");
		// 자동 커밋 (5초마다 컨슈머는 poll()을 호출할 때 가장 마지막 오프셋을 커밋한다.
		props.put("enable.auto.commit", "true");
		props.put("auto.offset.reset", "latest");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Arrays.asList("log-tomcat"));

		ObjectMapper objectMapper = new ObjectMapper();
		final String url = "비밀";

		try {
			while (true) {
				ConsumerRecords<String, String> recoreds = consumer.poll(100);
				for (ConsumerRecord<String, String> recored : recoreds) {
					if (!Objects.isNull(recored.value())) {
						TomcatLog result = objectMapper.readValue(recored.value(), TomcatLog.class);
						if (ErrorLevel.ERROR == ErrorLevel.valueOf(result.getLevel())) {
							send(url, result.getType(), result.getMessage());
						}
					}
				}
			}
		} finally {
			consumer.close();
		}
	}

	static void send(String webhook_url, String title, String message) {
		try {
			URL url = new URL(webhook_url);
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			JSONObject json = new JSONObject();
			json.put("title", title);
			json.put("text", message);
			json.put("themeColor", "FF0000");

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(json.toString());
			wr.flush();
			wr.close();

			int status = 0;
			if (null != conn) {
				status = conn.getResponseCode();
			}

			if (status != 0) {
				if (status == 200) {
					//SUCCESS message
					BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					System.out.println("Android Notification Response : " + reader.readLine());

				} else {
					System.out.println("test");
				}
			}
		} catch (MalformedURLException mlfexception) {
			// Prototcal Error
			System.out.println("Error occurred while sending push Notification!.." + mlfexception.getMessage());
		} catch (IOException mlfexception) {
			//URL problem
			System.out.println("Reading URL, Error occurred while sending push Notification!.." + mlfexception.getMessage());

		} catch (JSONException jsonexception) {
			//Message format error
			System.out.println("Message Format, Error occurred while sending push Notification!.." + jsonexception.getMessage());

		} catch (Exception exception) {
			//General Error or exception.
			System.out.println("Error occurred while sending push Notification!.." + exception.getMessage());
		}
	}

	@Getter
	@Setter
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	class TomcatLog {
		private String message;
		private String level;
		private String key;
		private String type;
		private String source;
	}

	enum ErrorLevel {
		INFO, WARN, ERROR
	}
}

