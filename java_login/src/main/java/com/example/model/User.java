package com.example.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("username")
    private String userName;

    private String password;
}
