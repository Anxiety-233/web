package com.lab.equipment.controller;

import com.lab.equipment.entity.OperationLog;
import com.lab.equipment.service.OperationLogService;
import com.lab.equipment.util.CsvExportUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/log")
public class LogController {

    @Resource
    private OperationLogService operationLogService;

    /**
     * 导出操作日志（仅管理员）
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportLog(HttpServletResponse response) {
        try {
            List<OperationLog> list = operationLogService.list();
            String[] headers = {"日志ID", "用户名", "操作行为", "IP地址", "浏览器", "请求参数", "响应结果", "操作时间"};
            CsvExportUtil.exportCSV(response, "操作日志.csv", headers, list);
        } catch (Exception e) {
            // 直接写入响应，因为是void返回值
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("导出失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}