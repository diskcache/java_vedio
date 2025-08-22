package com.example.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("vedio")
public class Vedio {
    @TableId(type = IdType.AUTO)
    private Integer id;
    // private String title;
    // private String description;
    // private String uploader;
    @TableField("file_name")
    private String fileName;

    @TableField("file_path")
    private String filePath;

    private String hash;
    // private String vedioInfo;
    // private String uploadTime;
    @TableField("img_path")
    private String imgPath;
}
