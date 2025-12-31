package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // 👈 必须导入
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`orders`") // 👈 加上反引号，防止和 SQL 关键字冲突
public class Orders {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    // 👇 这里的 value 必须和数据库表里的列名(下划线)一模一样！
    @TableField("user_id")
    private Long userId;

    @TableField("product_id") // 对应数据库的 product_id
    private Long productId;

    @TableField("total_amount") // 对应数据库的 total_amount
    private BigDecimal totalAmount;

    @TableField("status")
    private String status;

    @TableField("create_time") // 对应数据库的 create_time
    private LocalDateTime createTime;
}