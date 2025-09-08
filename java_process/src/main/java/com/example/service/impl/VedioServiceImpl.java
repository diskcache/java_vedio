package com.example.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.service.VedioService;

@Service
public class VedioServiceImpl implements VedioService {
    @Value("${upload.directory}")
    private String dict;

    private static final Logger logger = LoggerFactory.getLogger(VedioServiceImpl.class);

    @Override
    public void merge(String filename, Long total) throws IOException {
        // 创建最终文件路径
        Path outputPath = Paths.get(dict, filename);

        // 创建输出通道
        try (FileChannel outputChannel = FileChannel.open(
                outputPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            // 按索引顺序合并所有分片
            for (int i = 1; i <= total; i++) {
                logger.info("合成分片-" + i);
                Path chunkPath = Paths.get(dict, filename + "-" + i);
                try (FileChannel inputChannel = FileChannel.open(
                        chunkPath,
                        StandardOpenOption.READ)) {
                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                }
            }
        }
    }

    @Override
    public String shot(String filename) throws IOException, InterruptedException {
        String cmd = String.format("ffmpeg -i %s -ss 00:00:01 -vframes 1 -y %s", dict + filename,
                dict + "thumb_" + filename + ".jpg");
        // Process process = Runtime.getRuntime().exec("ffmpeg -i " + dict + filename +
        // " -ss 00:00:01 -vframes 1 -y "
        // + dict + "thumb_" + filename + ".jpg");
        logger.info("生成缩略图");
        ProcessBuilder processBuilder = new ProcessBuilder(cmd.split(" "));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        // 循环读取输出，防止缓冲区满导致阻塞
        String line;
        while ((line = reader.readLine()) != null)
            System.out.println(line);
        int exit = process.waitFor();
        if (exit != 0)
            throw new IOException("生成缩略图失败");
        return dict + "thumb_" + filename + ".jpg";
    }

    // 清理分片文件
    @Override
    public void clean(String filename, Long total) throws IOException {
        for (int i = 1; i <= total; i++) {
            logger.info("清理分片-" + i);
            Path chunkPath = Paths.get(dict, filename + "-" + i);
            if (Files.exists(chunkPath))
                Files.delete(chunkPath);
        }
    }

    @Override
    public String process(String filename) throws IOException, InterruptedException {
        String cmd = String.format("ffmpeg -i %s -codec: copy -start_number 0 -hls_time 10 -hls_list_size 0 -f hls %s",
                dict + filename, dict + filename + ".m3u8");
        ProcessBuilder processBuilder = new ProcessBuilder(cmd.split(" "));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null)
            System.out.println(line);
        int exit = process.waitFor();
        if (exit == 0) {
            System.out.println("视频处理成功");
            return dict + filename + ".m3u8";
        } else {
            System.out.println("视频转换失败");
            return null;
        }
    }
}
