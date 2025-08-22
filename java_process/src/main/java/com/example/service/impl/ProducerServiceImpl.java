package com.example.service.impl;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.example.model.Finish;
import com.example.service.ProducerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProducerServiceImpl implements ProducerService {
    @Value("${rocketmq.producer.send-message-timeout}")
    private Integer timeout;

    private static final String topic = "RLT_PROCESS_TOPIC";

    @Autowired
    private RocketMQTemplate template;

    @Override
    public void send(Finish message) {
        template.convertAndSend(topic + ":finish", message);
    }

    @Override
    public SendResult sendMsg() {
        SendResult sendResult = template.syncSend(topic, MessageBuilder.withPayload("sendMsg").build());
        log.info("sendMsg sendResult={}", JSON.toJSONString(sendResult));
        return sendResult;
    }

    @Override
    public void sendAsyncMsg() {
        template.asyncSend(topic, MessageBuilder.withPayload("sendAsyncMsg").build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println("success");
            }

            @Override
            public void onException(Throwable throwable) {
                System.out.println("failed");
            }
        });
    }

    @Override
    public void sendDelayMsg() {
        template.syncSend(topic, MessageBuilder.withPayload("sendDelayMsg").build(), timeout, 10);
    }

    @Override
    public void sendOneWayMsg() {
        template.sendOneWay(topic, MessageBuilder.withPayload("sendOneWayMsg").build());
    }

    @Override
    public SendResult sendTagMsg() {
        return template.syncSend(topic + ":tag2", MessageBuilder.withPayload("sendTagMsg").build());
    }

}
