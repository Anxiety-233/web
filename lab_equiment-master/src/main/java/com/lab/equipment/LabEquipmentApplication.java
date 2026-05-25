package com.lab.equipment;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.lab.equipment.mapper")  // 扫描Mapper接口
@EnableAspectJAutoProxy  // 开启AOP
@EnableCaching  // 开启Redis缓存
public class LabEquipmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabEquipmentApplication.class, args);
    }

}