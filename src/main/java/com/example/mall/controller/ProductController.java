package com.example.mall.controller;

import com.example.mall.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    @GetMapping("/old-products") // 访问首页展示商品
    public String index(Model model) {
        model.addAttribute("productList", productMapper.selectList(null));
        return "index";
    }
}