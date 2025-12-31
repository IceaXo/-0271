package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mall.entity.Orders;
import com.example.mall.entity.Product;
import com.example.mall.entity.User;
import com.example.mall.mapper.OrderMapper;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Controller
public class MallController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JavaMailSender mailSender;

    // ================= [ 1. 顾客功能 ] =================

    @GetMapping("/")
    public String index(Model model) {
        List<Product> products = productMapper.selectList(null);
        model.addAttribute("productList", products);
        return "index";
    }

    @Transactional
    @PostMapping("/buy")
    public String buy(@RequestParam Long productId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Product product = productMapper.selectById(productId);
        if (product == null || product.getStock() <= 0) {
            return "redirect:/?error=no_stock";
        }

        product.setStock(product.getStock() - 1);
        productMapper.updateById(product);

        Orders order = new Orders();
        order.setUserId(user.getId());
        order.setProductId(productId);
        order.setTotalAmount(product.getPrice());
        order.setStatus("待发货");
        order.setCreateTime(LocalDateTime.now());
        
        orderMapper.insert(order);

        new Thread(() -> {
            try {
                sendEmail(user.getEmail(), user.getUsername(), product.getName(), order.getId());
            } catch (Exception e) {
                System.err.println("邮件发送小插曲：" + e.getMessage());
            }
        }).start();

        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Orders> orders;
        if ("ADMIN".equals(user.getRole())) {
            orders = orderMapper.selectList(new QueryWrapper<Orders>().orderByDesc("id"));
        } else {
            orders = orderMapper.selectList(
                new QueryWrapper<Orders>().eq("user_id", user.getId()).orderByDesc("id")
            );
        }
        
        model.addAttribute("orderList", orders);
        return "orders"; 
    }

    // ================= [ 2. 管理员入口 ] =================

    // ★★★ 这里就是你要的修复！添加了 /admin 的入口 ★★★
    @GetMapping("/admin")
    public String adminIndex() {
        // 访问 /admin 直接跳转到 /admin/stats (统计页)
        return "redirect:/admin/stats";
    }

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/admin/users")
    public String adminUserList(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("userList", userMapper.selectList(null));
        return "admin_users"; 
    }

    @GetMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        userMapper.deleteById(id);
        return "redirect:/admin/users";
    }

    // ================= [ 3. 管理员 - 订单发货 ] =================

    @GetMapping("/admin/order/ship/{id}")
    public String shipOrder(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        Orders order = orderMapper.selectById(id);
        if (order != null && "待发货".equals(order.getStatus())) {
            order.setStatus("已发货");
            orderMapper.updateById(order);

            User user = userMapper.selectById(order.getUserId());
            Product product = productMapper.selectById(order.getProductId());

            if (user != null && product != null) {
                new Thread(() -> {
                    try {
                        sendShipEmail(user.getEmail(), user.getUsername(), product.getName(), order.getId());
                    } catch (Exception e) {
                        System.err.println("发货邮件失败：" + e.getMessage());
                    }
                }).start();
            }
        }
        return "redirect:/orders";
    }

    // ================= [ 4. 管理员 - 商品管理 ] =================

    @GetMapping("/admin/products")
    public String adminProductList(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        model.addAttribute("productList", productMapper.selectList(null));
        return "admin_product_list";
    }

    @GetMapping("/admin/product/add")
    public String addProductPage(HttpSession session) { 
        if (!isAdmin(session)) return "redirect:/login";
        return "admin_product_add"; 
    }

    @PostMapping("/admin/product/add")
    public String addProduct(Product product, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        productMapper.insert(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/product/edit/{id}")
    public String editProductPage(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        Product product = productMapper.selectById(id);
        model.addAttribute("product", product);
        return "admin_product_edit";
    }

    @PostMapping("/admin/product/update")
    public String updateProduct(Product product, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        productMapper.updateById(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/product/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";
        productMapper.deleteById(id);
        return "redirect:/admin/products";
    }

    // ================= [ 5. 管理员 - 统计 ] =================

    @GetMapping("/admin/stats")
    public String statsPage(HttpSession session) { 
        if (!isAdmin(session)) return "redirect:/login";
        return "admin_stats"; 
    }

    @GetMapping("/api/admin/stats-data")
    @ResponseBody
    public List<Map<String, Object>> getRealStatsData(HttpSession session) {
        if (!isAdmin(session)) return List.of();
        try {
            return orderMapper.selectMaps(new QueryWrapper<Orders>()
                    .select("DATE_FORMAT(create_time, '%m-%d') as day", "SUM(total_amount) as total")
                    .groupBy("day")
                    .orderByAsc("day")
                    .last("LIMIT 7"));
        } catch (Exception e) {
            return List.of();
        }
    }

    // ================= [ 邮件工具 ] =================

    private void sendEmail(String toEmail, String username, String productName, Long orderId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("949147765@qq.com"); 
        message.setTo(toEmail);
        message.setSubject("✅ S.E.E.S. Mall - 订单确认通知");
        message.setText("尊敬的 " + username + "：\n\n" +
                "您购买的 [" + productName + "] 我们已收到订单！\n" +
                "订单号：#" + orderId + "\n" +
                "当前状态：待发货\n" +
                "我们会尽快为您安排配送。\n\n" +
                "S.E.E.S. 特别行动组");
        mailSender.send(message);
    }

    private void sendShipEmail(String toEmail, String username, String productName, Long orderId) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("949147765@qq.com"); 
        message.setTo(toEmail);
        message.setSubject("📦 S.E.E.S. Mall - 您的订单已发货！");
        message.setText("亲爱的 " + username + "：\n\n" +
                "好消息！您购买的战术装备 [" + productName + "] 已经从 Iwatodai 发出！\n" +
                "订单号：#" + orderId + "\n" +
                "请保持通讯畅通，准备接收包裹。\n\n" +
                "S.E.E.S. 后勤部");
        mailSender.send(message);
    }
}