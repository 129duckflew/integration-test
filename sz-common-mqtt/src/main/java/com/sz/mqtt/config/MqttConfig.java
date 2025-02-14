package com.sz.mqtt.config;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.converter.*;
import java.util.concurrent.Executors;


@Configuration
@IntegrationComponentScan("com.sz.mqtt.config")
@Slf4j
@ConfigurationProperties(prefix = "mqtt")
@Data
public class MqttConfig {
    private String clientIdInbound;
    private String clientIdOutbound;
    private String url;
    private String password;
    private String username;

    @Bean
    public MqttConnectionOptions mqttConnectOptions(){
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setServerURIs(new String[] { url});
        options.setUserName(username);
        options.setPassword(password.getBytes());
        options.setAutomaticReconnect(true);
        return options;
    }

    @Bean
    public SimpleMessageConverter simpleMessageConverter(){
        return new SimpleMessageConverter();
    }
    /**
     * mqttGateway把消息发送到mqttOutboundChannel通道 这个Bean是一个消息处理器 处理的逻辑是发送到Mqtt的服务器
     * @return
     */
    @Bean
    public MessageHandler mqttOutboundHandler(MqttConnectionOptions connectionOptions) {
        Mqttv5PahoMessageHandler messageHandler = new Mqttv5PahoMessageHandler(connectionOptions,clientIdOutbound);
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("defaultTopic");
        messageHandler.setDefaultQos(0);
        messageHandler.setConverter(simpleMessageConverter());
        return messageHandler;
    }

    /**
     * mqtt消息的出站流配置 从mqttGateway默认请求的通道(mqttOutboundChannel)中获取消息，然后采用执行器通道向外发出,出站处理器就是spring-integration-mqtt
     * 提供的消息适配器作为出站适配器
     * @param mqttOutboundHandler
     * @return
     */
    @Bean
    public IntegrationFlow mqttOutboundFlow(MessageHandler mqttOutboundHandler){
        return IntegrationFlow.from("mqttOutboundChannel")
                .channel(MessageChannels.executor(Executors.newFixedThreadPool(5)))
                .handle(mqttOutboundHandler)
                .get();
    }

}
