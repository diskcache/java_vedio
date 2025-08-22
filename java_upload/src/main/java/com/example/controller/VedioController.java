package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.dao.VedioMapper;
import com.example.model.Vedio;
import com.example.service.impl.VedioServiceImpl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/vedios")
public class VedioController {

    @Autowired
    private VedioServiceImpl vedioService;

    @Autowired
    private VedioMapper vedioMapper;

    // @Autowired
    // private ProducerServiceImpl producerService;

    // @GetMapping("test")
    // public void test() {
    // try {
    // vedioService.release("d41d8cd98f00b204e9800998ecf8427e");
    // vedioService.init("test.mkv", "d41d8cd98f00b204e9800998ecf8427e", 21);

    // vedioService.append("d41d8cd98f00b204e9800998ecf8427e", 1);
    // vedioService.append("d41d8cd98f00b204e9800998ecf8427e", 2);
    // Object res = vedioService.get("d41d8cd98f00b204e9800998ecf8427e");
    // System.out.println(res);

    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    // @PostMapping("/upload")
    // public ResponseEntity<String> upload(
    // @RequestParam("file") MultipartFile file,
    // @RequestParam("title") String title,
    // @RequestParam("description") String description,
    // @RequestParam("uploader") String uploader) {
    // try {
    // String fileName = vedioService.savefile(file);
    // Vedio vedio = new Vedio();
    // vedio.setTitle(title);
    // vedio.setFileName(fileName);
    // vedio.setDescription(description);
    // vedio.setUploader(uploader);
    // vedio.setUploadTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    // vedioService.save(vedio);
    // return ResponseEntity.ok("视频上传成功!");
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body("视频上传失败：" + e.getMessage());
    // }
    // }
    // 先检查数据库有没有对应文件的哈希，有就可以秒传
    @PostMapping("/upload/check")
    public ResponseEntity<String> check(@RequestParam("hash") String hash) {
        List<Vedio> vedios = vedioMapper.selectList(null);
        for (Vedio vedio : vedios)
            if (vedio.getHash().equals(hash))
                return ResponseEntity.ok().body("上传成功");
        return ResponseEntity.status(HttpStatus.CREATED).body("没有上传");
    }

    // 初始化redis中的哈希数据结构，让后续分片到达可以有地方保存
    // @PostMapping("/upload/init")
    // public ResponseEntity<String> init(
    // @RequestParam("filename") String filename,
    // @RequestParam("hash") String hash,
    // @RequestParam("total") Integer total) {
    // try {
    // Object res = vedioService.get(hash);
    // if (res.equals(null)) {
    // vedioService.init(filename, hash, total);
    // return ResponseEntity.status(HttpStatus.CREATED).body("初始化成功");
    // } else
    // return ResponseEntity.ok().body("已初始化");
    // } catch (Exception e) {
    // e.printStackTrace();
    // return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("未知错误");
    // }
    // }

    @PostMapping("/chunk/upload")
    public ResponseEntity<String> chunkUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("hash") String hash,
            @RequestParam("filename") String filename,
            @RequestParam("size") Long size,
            @RequestParam("total") Long total,
            @RequestParam("index") Integer index) {
        try {
            if (!vedioService.inited(hash, filename, total))
                vedioService.init(hash, filename, total);
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
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("预处理失败");
        }
    }
}
