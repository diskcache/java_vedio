package com.example.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.VedioMapper;
import com.example.model.Vedio;
import com.example.model.ProcessMessage;
import com.example.service.VedioService;

@Service
public class VedioServiceImpl extends ServiceImpl<VedioMapper, Vedio> implements VedioService {
    @Autowired
    private RedisTemplate<String, Object> template;

    @Autowired
    private ProducerServiceImpl producerService;

    @Value("${upload.directory}")
    private String dict;

    @Override
    public String savefile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + file.getOriginalFilename();
        Path path = Paths.get(dict, fileName);
        // 确保目录存在
        if (!Files.exists(path.getParent()))
            Files.createDirectories(path.getParent());

        // 使用 NIO 直接保存，避免创建临时文件
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileName;
    }

    // redis和文件系统状态不一致
    @Override
    public Boolean savepart(MultipartFile part, String hash, String fileName, Integer index) throws IOException {
        boolean saved = false;
        boolean update = false;
        Path path = Paths.get(dict, fileName + "-" + index);
        // 确保目录存在
        if (!Files.exists(path.getParent()))
            Files.createDirectories(path.getParent());

        // 使用 NIO 直接保存，避免创建临时文件
        try {
            try (InputStream in = part.getInputStream()) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                saved = true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            append(hash, index);
            update = true;
        } catch (Exception e) {
            e.printStackTrace();
            if (saved && !update)
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            return false;
        }
        return true;
    }

    @Override
    public void merge(String filename, Long total, String hash) throws IOException, Exception {
        int retry = 0;
        while (retry < 5) {
            Boolean lock = template.opsForValue().setIfAbsent("mergelock:" + hash, "value", 30, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(lock)) {
                try {
                    ProcessMessage message = new ProcessMessage();
                    message.setFilename(filename);
                    message.setHash(hash);
                    message.setTotal(total);
                    producerService.send(message);
                    return;
                } finally {
                    template.delete("mergelock:" + hash);
                }
            } else {
                Thread.sleep(1000);
                retry++;
            }
        }
    }

    @Override
    public Vedio queryByFileName(String filename) throws Exception {
        return lambdaQuery()
                .eq(Vedio::getFileName, filename)
                .one();
    }

    @Override
    public void init(String hash, String filename, Long total) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("hash", hash);
        map.put("filename", filename);
        map.put("total", total);
        template.opsForHash().putAll("hash:" + hash + ":filename:" + filename + ":total:" + total, map);
        template.expire("hash:" + hash + ":filename:" + filename + ":total:" + total, 10, TimeUnit.MINUTES);
    }

    @Override
    public boolean inited(String hash, String filename, Long total) throws Exception {
        return Boolean.TRUE.equals(template.hasKey("hash:" + hash + ":filename:" + filename + ":total:" + total));
    }

    @Override
    public void release_init(String hash, String filename, Long total) throws Exception {
        template.delete("hash:" + hash + ":filename:" + filename + ":total:" + total);
    }

    @Override
    public void append(String hash, Integer index) throws Exception {
        String setKey = "hash-" + hash;
        template.opsForSet().add(setKey, index);
    }

    @Override
    public Boolean exists(String hash, Integer index) throws Exception {
        return template.opsForSet().isMember("hash-" + hash, index);
    }

    @Override
    public void release(String hash) throws Exception {
        template.delete("hash-" + hash);
    }

    @Override
    public Long size(String hash) throws Exception {
        return template.opsForSet().size("hash-" + hash);
    }
}
// mvn archetype:generate -DgroupId="com.example" -DartifactId=java_process
// -DinteractiveMode=false
