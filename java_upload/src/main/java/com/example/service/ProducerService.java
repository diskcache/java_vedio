package com.example.service;

import org.apache.rocketmq.client.producer.SendResult;

import com.example.model.ProcessMessage;

public interface ProducerService {
    public void send(ProcessMessage message);

    public SendResult sendMsg();

    public void sendAsyncMsg();

    public void sendDelayMsg();

    public void sendOneWayMsg();

    public SendResult sendTagMsg();
}
