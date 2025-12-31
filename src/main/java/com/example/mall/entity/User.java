package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName; // 👈 别忘了导包
import lombok.Data;

@Data
@TableName("`user`") // 👈 MJ修正：强制加上反引号，防止数据库报错！
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role;
}