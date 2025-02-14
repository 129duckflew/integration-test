package com.sz.component;

import com.sz.mqtt.config.MqttGateway;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ClientManager {

    private final MqttGateway mqttGateway;


    public void sendToMqtt(String topic,String data){
        mqttGateway.sendToTopic(data,topic);
    }


    @PostConstruct
    public void init(){
        sendToMqtt("test","hello");
    }
}
