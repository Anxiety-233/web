package com.lab.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment.dto.EquipmentDTO;
import com.lab.equipment.dto.ResultDTO;
import com.lab.equipment.entity.Equipment;
import com.lab.equipment.entity.User;
import com.lab.equipment.exception.BusinessException;
import com.lab.equipment.service.EquipmentService;
import com.lab.equipment.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {

    @Resource
    private EquipmentService equipmentService;

    @Resource
    private UserService userService;

    /**
     * 分页查询设备（所有用户可查看）
     */
    @GetMapping("/view/list")
    public ResultDTO<Page<Equipment>> getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        // 普通用户只能查看自己的设备
        if (!"admin".equals(username)) {
            wrapper.eq(Equipment::getUserId, getCurrentUserId());
        }
        Page<Equipment> page = equipmentService.page(new Page<>(pageNum, pageSize), wrapper);
        return ResultDTO.success(page);
    }

    /**
     * 根据ID查询设备
     */
    @GetMapping("/view/{id}")
    public ResultDTO<Equipment> getById(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return ResultDTO.error("设备不存在");
        }
        // 普通用户校验归属
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"admin".equals(username) && !equipment.getUserId().equals(getCurrentUserId())) {
            return ResultDTO.error("无权查看他人设备");
        }
        return ResultDTO.success(equipment);
    }

    /**
     * 新增设备（仅管理员）
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultDTO<?> add(@RequestBody EquipmentDTO dto) {
        // 新增：校验用户是否存在
        if (dto.getUserId() != null && userService.getById(dto.getUserId()) == null) {
            return ResultDTO.error("关联的用户不存在");
        }

        Equipment equipment = new Equipment();
        equipment.setEquipmentName(dto.getEquipmentName());
        equipment.setEquipmentCode(dto.getEquipmentCode());
        equipment.setStatus(dto.getStatus());
        equipment.setUserId(dto.getUserId());
        equipment.setRemark(dto.getRemark());
        equipment.setCreateTime(LocalDateTime.now());
        equipment.setUpdateTime(LocalDateTime.now());
        equipmentService.save(equipment);
        return ResultDTO.success();
    }

    /**
     * 修改设备（仅管理员）
     */
    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultDTO<?> edit(@PathVariable Long id, @RequestBody EquipmentDTO dto) {
        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return ResultDTO.error("设备不存在");
        }

        // 新增：校验用户是否存在
        if (dto.getUserId() != null && userService.getById(dto.getUserId()) == null) {
            return ResultDTO.error("关联的用户不存在");
        }

        equipment.setEquipmentName(dto.getEquipmentName());
        equipment.setEquipmentCode(dto.getEquipmentCode());
        equipment.setStatus(dto.getStatus());
        equipment.setUserId(dto.getUserId());
        equipment.setRemark(dto.getRemark());
        equipment.setUpdateTime(LocalDateTime.now());
        equipmentService.updateById(equipment);
        return ResultDTO.success();
    }

    /**
     * 删除设备（仅管理员）
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultDTO<?> delete(@PathVariable Long id) {
        if (!equipmentService.removeById(id)) {
            return ResultDTO.error("删除失败，设备不存在");
        }
        return ResultDTO.success();
    }

    /**
     * 控制设备状态（仅管理员）
     */
    @PatchMapping("/control/{id}/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResultDTO<?> controlStatus(@PathVariable Long id, @PathVariable Integer status) {
        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return ResultDTO.error("设备不存在");
        }
        // 新增：校验状态合法性
        if (status != 0 && status != 1) { // 假设0=禁用，1=启用
            return ResultDTO.error("状态值只能是0（禁用）或1（启用）");
        }
        equipment.setStatus(status);
        equipment.setUpdateTime(LocalDateTime.now());
        equipmentService.updateById(equipment);
        return ResultDTO.success();
    }

    /**
     * 获取当前登录用户ID（修复：用户不存在抛异常）
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("当前登录用户不存在");
        }
        return user.getId();
    }
}