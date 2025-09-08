package com.example.service.impl;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.Finish;
import com.example.model.ProcessMessage;
import com.example.service.ConsumerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConsumerServiceImpl implements ConsumerService {
    @Autowired
    private VedioServiceImpl vedioService;

    @Autowired
    private ProducerServiceImpl producerService;

    @Service
    @RocketMQMessageListener(topic = "RLT_UPLOAD_TOPIC", selectorExpression = "process", consumerGroup = "Process_Group_One")
    public class ConsumerSend implements RocketMQListener<ProcessMessage> {
        @Override
        public void onMessage(ProcessMessage message) {
            log.info("接收到消息:str={}", message);
            try {
                vedioService.merge(message.getFilename(), message.getTotal());

                String imgPath = vedioService.shot(message.getFilename());

                vedioService.clean(message.getFilename(), message.getTotal());

                String filePath = vedioService.process(message.getFilename());

                Finish finish = new Finish();

                finish.setFilename(message.getFilename());

                finish.setFilePath(filePath);

                finish.setImgPath(imgPath);

                finish.setHash(message.getHash());

                finish.setTotal(message.getTotal());

                finish.setStatus(200);

                producerService.send(finish);
            } catch (Exception e) {
                Finish finish = new Finish();

                finish.setFilename(message.getFilename());
                finish.setFilePath("");
                finish.setImgPath("");

                finish.setStatus(500);

                producerService.send(finish);
                e.printStackTrace();
            }
        }
    }

    // @Service
    // @RocketMQMessageListener(topic = "RLT_TEST_TOPIC", consumerGroup =
    // "Con_Group_Two")
    // public class ConsumerSend2 implements RocketMQListener<String> {
    // @Override
    // public void onMessage(String str) {
    // log.info("监听到消息：str={}", str);
    // }
    // }

    @Service
    @RocketMQMessageListener(topic = "RLT_UPLOAD_TOPIC", selectorExpression = "tag2", consumerGroup = "Process_Group_Three")
    public class Consumer implements RocketMQListener<MessageExt> {
        @Override
        public void onMessage(MessageExt messageExt) {
            byte[] body = messageExt.getBody();
            String msg = new String(body);
            log.info("监听到消息：msg={}", msg);
        }
    }

}
