package com.example.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.model.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}