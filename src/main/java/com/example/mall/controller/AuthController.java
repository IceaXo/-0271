package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mall.entity.User;
import com.example.mall.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    // 登录页
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 处理登录
    @PostMapping("/login")
    public String login(String username, String password, HttpSession session, Model model) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username", username)
                .eq("password", password));
        if (user != null) {
            session.setAttribute("user", user); // 登录成功，存入 Session
            return "redirect:/";
        }
        model.addAttribute("error", "用户名或密码错误");
        return "login";
    }

    // 注册页
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // 处理注册
    @PostMapping("/register")
    public String register(User user, Model model) {
        // 1. 先检查用户名是否已经存在
        User existingUser = userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));

        if (existingUser != null) {
            // 如果存在，不执行插入，而是回到注册页并提示错误
            model.addAttribute("error", "用户名已存在，请换一个！");
            return "register";
        }

        // 2. 如果不存在，再执行插入
        userMapper.insert(user);
        return "redirect:/login";
    }

    // 注销
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}