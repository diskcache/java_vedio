package com.example.model;

import lombok.Data;

@Data
public class Finish {
    private String filename;
    private String imgPath;
    private String filePath;
    private String hash;
    private Long total;
    private Integer status;
}
