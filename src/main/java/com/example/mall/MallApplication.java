package com.example.mall;

import org.mybatis.spring.annotation.MapperScan; // 必须导入这个
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.mall.mapper") // 告诉程序数据库接口在这里
public class MallApplication {
	public static void main(String[] args) {
		SpringApplication.run(MallApplication.class, args);
	}
}