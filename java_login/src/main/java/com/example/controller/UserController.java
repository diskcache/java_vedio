package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.User;
import com.example.service.impl.UserServiceImpl;
import com.example.util.JwtUtil;
import com.example.util.Md5;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("username") String username,
            @RequestParam("password") String password) {
        try {
            logger.info("Login attempt for user: {}", username);
            User user = userService.authenticate(username, password);
            if (user != null) {
                String token = jwtUtil.generateToken(user.getUserName(), user.getId());
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("userId", user.getId());
                response.put("username", user.getUserName());
                response.put("message", "登录成功");
                logger.info("User {} logged in successfully", username);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Failed login for user: {}", username);
                return ResponseEntity.status(401).body("Invalid credentials");
            }
        } catch (Exception e) {
            logger.warn("Failed login for user: {},err: {}", username, e.getMessage());
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam("username") String username,
            @RequestParam("password") String password) {
        logger.info("Register attempt for user: {}", username);
        try {
            if (username == null || password == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码为空");
            User user = new User();
            user.setUserName(username);
            user.setPassword(Md5.md5(password));
            userService.save(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "注册成功，请登录");
            logger.info("Register success for user: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Register failed for user: {} err: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("未知错误");
        }
    }

}
