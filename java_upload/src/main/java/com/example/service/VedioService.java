package com.example.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.model.Vedio;
import com.example.model.VedioMetaData;

public interface VedioService {
    public String savefile(MultipartFile vedioFile) throws IOException;

    public Boolean savepart(MultipartFile part, String hash, String fileName, Integer index) throws IOException;

    public void merge(String filename, Long total, String hash) throws IOException, Exception;

    public void setMetaData(VedioMetaData vedioMetaData) throws Exception;

    public Boolean existsMetaData(VedioMetaData vedioMetaData) throws Exception;

    public Vedio getMetaData(String filename, String hash, Long total) throws Exception;

    public void deleteMetaData(String filename, String hash, Long total) throws Exception;

    // public void init(String hash, String filename, Long total) throws Exception;

    // public boolean inited(String hash, String filename, Long total) throws
    // Exception;

    public void append(String hash, Integer index) throws Exception;

    public void release(String hash) throws Exception;

    // public void release_init(String hash, String filename, Long total) throws
    // Exception;

    public Boolean exists(String hash, Integer index) throws Exception;

    public Long size(String hash) throws Exception;

    public Vedio queryByFileName(String filename) throws Exception;

    public List<Vedio> queryByUser(Integer userid) throws Exception;

    public List<Vedio> queryByKey(String key) throws Exception;
}
