package com.example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.dao.VedioMapper;
import com.example.model.Vedio;
import com.example.model.VedioMetaData;
import com.example.service.impl.VedioServiceImpl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RestController
@RequestMapping("/vedios")
public class VedioController {
    private final Logger logger = LoggerFactory.getLogger(VedioController.class);

    @Autowired
    private VedioServiceImpl vedioService;

    @Autowired
    private VedioMapper vedioMapper;

    @Autowired
    private RedisTemplate<String, String> template;

    // 先检查数据库有没有对应文件的哈希，有就可以秒传
    @PostMapping("/upload/check")
    public ResponseEntity<String> check(@RequestParam("hash") String hash) {
        logger.info("Check attempt for file: {}", hash);
        List<Vedio> vedios = vedioMapper.selectList(null);
        for (Vedio vedio : vedios)
            if (vedio.getHash().equals(hash))
                return ResponseEntity.ok().body("上传成功");
        return ResponseEntity.status(HttpStatus.CREATED).body("没有上传");
    }

    // 初始化redis中的哈希数据结构，让后续分片到达可以有地方保存
    @PostMapping("/upload/metadata")
    public ResponseEntity<String> setMetaData(
            @RequestParam("userid") String userid,
            @RequestParam("title") String title,
            @RequestParam("duration") String duration,
            @RequestParam("uploadtime") String uploadTime,
            @RequestParam("total") Long total,
            @RequestParam("hash") String hash,
            @RequestParam("filename") String filename) {
        try {
            logger.info("MetaData attempt for file: {}", hash);
            VedioMetaData vedioMetaData = new VedioMetaData();
            vedioMetaData.setUserId(Integer.parseInt(userid));
            vedioMetaData.setTitle(title);
            vedioMetaData.setDuration(Long.parseLong(duration));
            vedioMetaData.setUploadTime(Long.parseLong(uploadTime));
            vedioMetaData.setTotal(total);
            vedioMetaData.setHash(hash);
            vedioMetaData.setFileName(filename);
            if (!vedioService.existsMetaData(vedioMetaData)) {
                vedioService.setMetaData(vedioMetaData);
                logger.info("MetaData init for file: {}", hash);
                return ResponseEntity.status(HttpStatus.CREATED).body("保存元数据");
            } else
                return ResponseEntity.ok("已初始化");
        } catch (Exception e) {
            logger.warn("MetaData init failed for file: {}", hash);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("未知错误");
        }
    }

    @PostMapping("/chunk/upload")
    public ResponseEntity<String> chunkUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("hash") String hash,
            @RequestParam("filename") String filename,
            @RequestParam("size") Long size,
            @RequestParam("total") Long total,
            @RequestParam("index") Integer index) {
        try {
            if (vedioService.exists(hash, index))
                return ResponseEntity.ok().body("分片" + index + "已存在，跳过上传");
            if (size.equals(file.getSize())) {
                if (!vedioService.savepart(file, hash, filename, index))
                    return ResponseEntity.status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).body("保存文件分片失败");
            } else
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("分片" + index + "损坏");
            if (total.equals(vedioService.size(hash)))
                vedioService.merge(filename, total, hash);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("未知错误");
        }
        return ResponseEntity.ok().body("success");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Vedio>> getAll() {
        try {
            List<Vedio> vedios = vedioMapper.selectList(null);
            if (vedios != null)
                return ResponseEntity.ok(vedios);
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/play")
    public ResponseEntity<String> play(@RequestParam("filename") String filename) {
        try {
            Vedio vedio = vedioService.queryByFileName(filename);
            String url = "http://127.0.0.1:8000" + vedio.getFilePath();
            String hot = "vedio:hot:" + filename;
            template.opsForZSet().incrementScore("rank", hot, 1);

            template.opsForZSet().removeRange("rank", 10, -1);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("预处理失败");
        }
    }

    @GetMapping("/hot")
    public ResponseEntity<List<Map<String, Object>>> getHotRank() {
        Set<ZSetOperations.TypedTuple<String>> rankSet = template.opsForZSet()
                .reverseRangeWithScores("rank", 0, 9);// 获取前10名

        List<Map<String, Object>> result = new ArrayList<>();
        if (rankSet != null)
            for (ZSetOperations.TypedTuple<String> tuple : rankSet) {
                Map<String, Object> item = new HashMap<>();
                String hot = tuple.getValue();
                String filename = hot.substring(hot.lastIndexOf(":") + 1);
                item.put("filename", filename);
                item.put("hot", tuple.getScore());
                result.add(item);
            }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/user")
    public ResponseEntity<List<Vedio>> getByUser(@RequestParam("userId") Integer userId) {
        try {
            List<Vedio> vedios = vedioService.queryByUser(userId);
            return ResponseEntity.ok(vedios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
        }
    }

    @PostMapping("/search")
    public ResponseEntity<List<Vedio>> search(@RequestParam("key") String key) {
        try {
            List<Vedio> vedios = vedioService.queryByKey(key);
            return ResponseEntity.ok(vedios);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
        }
    }

}
