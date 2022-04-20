package com.mydeveloperplanet.mygcpsubplanet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class MyGcpSubPlanetApplication {

	private static final Log LOGGER = LogFactory.getLog(MyGcpSubPlanetApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MyGcpSubPlanetApplication.class, args);
	}

	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(
			@Qualifier("myInputChannel") MessageChannel inputChannel,
			PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter =
				new PubSubInboundChannelAdapter(pubSubTemplate, "test-sub");
		adapter.setOutputChannel(inputChannel);

		return adapter;
	}

	@Bean
	public MessageChannel myInputChannel() {
		return new DirectChannel();
	}


	@ServiceActivator(inputChannel = "myInputChannel")
	public String messageReceiver(String payload) {
		LOGGER.info("Message arrived! Payload: " + payload);
		return payload;
	}

	@Bean
	@ServiceActivator(inputChannel = "myOutputChannel")
	public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
		return new PubSubMessageHandler(pubsubTemplate, "topic-test");
	}

	@MessagingGateway(defaultRequestChannel = "myOutputChannel")
	public interface PubsubOutboundGateway {
		void sendToPubsub(String text);
	}

}
