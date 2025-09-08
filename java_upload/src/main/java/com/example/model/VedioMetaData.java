package com.example.model;

import lombok.Data;

@Data
public class VedioMetaData {
    private Integer userId;

    private String title;

    private Long duration;

    private Long uploadTime;

    private Long total;

    private String hash;

    private String fileName;
}
