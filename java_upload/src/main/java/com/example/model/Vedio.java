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

    @TableField("user_id")
    private Integer userId;

    private String title;

    private Long duration;

    private Long uploadTime;

    @TableField("file_name")
    private String fileName;

    @TableField("file_path")
    private String filePath;

    private String hash;

    @TableField("img_path")
    private String imgPath;
}
