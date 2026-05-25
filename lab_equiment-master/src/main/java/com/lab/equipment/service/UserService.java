package com.lab.equipment.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lab.equipment.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends IService<User>, UserDetailsService {}
