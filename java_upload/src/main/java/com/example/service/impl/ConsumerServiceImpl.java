package com.example.service.impl;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.Vedio;
import com.example.model.Finish;
import com.example.service.ConsumerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConsumerServiceImpl implements ConsumerService {
    @Autowired
    private VedioServiceImpl vedioService;

    @Service
    @RocketMQMessageListener(topic = "RLT_PROCESS_TOPIC", selectorExpression = "finish", consumerGroup = "Upload_Group_One")
    public class ConsumerSend implements RocketMQListener<Finish> {
        @Override
        public void onMessage(Finish message) {
            log.info("接收到消息:str={}", message);
            try {
                if (message.getStatus() == 200) {
                    String imgPath = message.getImgPath();
                    String filePath = message.getFilePath();
                    Vedio vedio = new Vedio();
                    vedio.setFileName(message.getFilename());
                    vedio.setHash(message.getHash());
                    vedio.setFilePath(filePath);
                    vedio.setImgPath(imgPath);
                    vedioService.save(vedio);
                    //删除redis的key
                    vedioService.release(message.getHash());
                    vedioService.release_init(message.getHash(), message.getFilename(), message.getTotal());
                }
            } catch (Exception e) {
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

    // @Service
    // @RocketMQMessageListener(topic = "RLT_TEST_TOPIC", selectorExpression =
    // "tag2", consumerGroup = "Con_Group_Three")
    // public class Consumer implements RocketMQListener<MessageExt> {
    // @Override
    // public void onMessage(MessageExt messageExt) {
    // byte[] body = messageExt.getBody();
    // String msg = new String(body);
    // log.info("监听到消息：msg={}", msg);
    // }
    // }

}
