package com.example.model;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.example.service.impl.VedioServiceImpl;

@Component
public class KeyExpireListener extends KeyExpirationEventMessageListener {
    @Autowired
    private VedioServiceImpl vedioService;

    public KeyExpireListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Value("${upload.directory}")
    private String dict;

    @Override
    public void doHandleMessage(@NonNull Message message) {
        // 过期的 key
        String key = new String(message.getBody(), StandardCharsets.UTF_8);
        clean(key);
    }

    public void clean(String key) {
        try {
            String[] keys = key.split(":");//
            String filename = keys[1];
            Integer total = Integer.parseInt(keys[5]);
            String hash = keys[3];
            vedioService.release(hash);//删除已上传的分片的键
            for (int i = 1; i <= total; i++) {
                System.out.println("清理分片" + i);
                Path chunkPath = Paths.get(dict, filename + "-" + i);
                if (Files.exists(chunkPath))
                    Files.delete(chunkPath);
            }
        } catch (FileNotFoundException e) {
            System.out.println("文件找不到");
        } catch (NoSuchFileException e) {
            System.out.println("没有此文件");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
