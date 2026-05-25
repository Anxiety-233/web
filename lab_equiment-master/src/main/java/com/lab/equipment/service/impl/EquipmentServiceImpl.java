package com.lab.equipment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.mapper.EquipmentMapper;
import com.lab.equipment.service.EquipmentService;
import org.springframework.stereotype.Service;

/**
 * 设备服务实现类
 */
@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

}