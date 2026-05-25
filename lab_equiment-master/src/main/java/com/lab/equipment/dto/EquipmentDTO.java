package com.lab.equipment.dto;

import lombok.Data;
@Data
public class EquipmentDTO {
    private String equipmentName;
    private String equipmentCode;
    private Integer status;
    private Long userId;
    private String remark;
}
