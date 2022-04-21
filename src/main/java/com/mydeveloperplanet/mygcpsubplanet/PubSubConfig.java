package com.mydeveloperplanet.mygcpsubplanet;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gcp.pubsub.core.PubSubOperations;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Configuration
@Slf4j
public class PubSubConfig {
    /*
     *   Message sender code
     * */
    @Bean
    @ServiceActivator(inputChannel = "pubSubOutputChannel")
    public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
        PubSubMessageHandler adapter =
                new PubSubMessageHandler(pubsubTemplate, "topic-test");
        adapter.setPublishCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("There was an error sending the message.");
            }

            @Override
            public void onSuccess(String result) {
                log.info("Message was sent successfully.");
            }
        });

        return adapter;
    }

    @MessagingGateway(defaultRequestChannel = "pubSubOutputChannel")
    public interface PubSubOutboundGateway {
        void sendToPubSub(String text);
    }

    /*
     *   Message receiver code
     * */
    @Bean
    public MessageChannel pubsubInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter messageChannelAdapter(
            @Qualifier("pubsubInputChannel") MessageChannel inputChannel,
            PubSubOperations pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "test-sub");
        adapter.setOutputChannel(inputChannel);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    @ServiceActivator(inputChannel = "pubsubInputChannel")
    public void messageReceiver(String payload, @Header(GcpPubSubHeaders.ACKNOWLEDGEMENT) AckReplyConsumer ackReplyConsumer) {
        log.info("Message arrived! Payload: " + payload);
        ackReplyConsumer.ack();
    }
}
