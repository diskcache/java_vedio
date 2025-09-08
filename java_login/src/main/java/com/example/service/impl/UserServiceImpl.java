package com.example.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.UserMapper;
import com.example.model.User;
import com.example.service.UserService;
import com.example.util.Md5;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public User authenticate(String username, String password) {
        User user = lambdaQuery()
                .eq(User::getUserName, username)
                .one();
        if (user != null && user.getPassword().equals(Md5.md5(password)))
            return user;
        return null;
    }

}
